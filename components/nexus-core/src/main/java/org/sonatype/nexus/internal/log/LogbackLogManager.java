/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.io.FileSupport;
import org.sonatype.nexus.common.io.LimitedInputStream;
import org.sonatype.nexus.common.io.StreamSupport;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.configuration.ApplicationDirectories;
import org.sonatype.nexus.log.LogConfigurationCustomizer;
import org.sonatype.nexus.log.LogConfigurationCustomizer.Configuration;
import org.sonatype.nexus.log.LogConfigurationParticipant;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;

/**
 * Logback {@link LogManager}.
 */
@Named
@Singleton
public class LogbackLogManager
    extends StateGuardLifecycleSupport
    implements LogManager
{
  private static final String F_LOGBACK_XML = "logback.xml";

  private static final String F_LOGBACK_PROPERTIES = "logback.properties";

  private static final String PAX_BUNDLE_CONTEXT_KEY = "org.ops4j.pax.logging.logback.bundlecontext";

  /**
   * System variable, dynamically set to config dir, used for rooting include files.
   */
  private static final String V_CONFIG_DIR = "nexus.log-config-dir";

  private final Injector injector;

  private final List<LogConfigurationParticipant> participants;

  private final List<LogConfigurationCustomizer> customizers;

  private final EventBus eventBus;

  private final Map<String, LoggerLevel> overrides;

  private final Map<String, LoggerLevel> customisations;

  private final CustomizationContextListener customizationContextListener;

  private final File configDir;

  private final File overridesFile;

  @Inject
  public LogbackLogManager(final Injector injector,
                           final ApplicationDirectories applicationDirectories,
                           final List<LogConfigurationParticipant> participants,
                           final List<LogConfigurationCustomizer> customizers,
                           final EventBus eventBus)
  {
    this.injector = checkNotNull(injector);
    checkNotNull(applicationDirectories);
    this.participants = checkNotNull(participants);
    this.customizers = checkNotNull(customizers);
    this.eventBus = checkNotNull(eventBus);
    this.overrides = new HashMap<>();
    this.customisations = new HashMap<>();
    this.customizationContextListener = new CustomizationContextListener();

    this.configDir = applicationDirectories.getWorkDirectory("etc");
    log.debug("Config dir: {}", configDir);

    // needed to allow included files to root properly
    System.setProperty(V_CONFIG_DIR, configDir.getAbsolutePath());

    this.overridesFile = new File(configDir, "logback-overrides.xml");
    log.debug("Overrides file: {}", overridesFile);

    eventBus.register(this);
  }

  @Subscribe
  public void on(final NexusInitializedEvent event) throws Exception {
    start();
  }

  @Override
  protected void doStart() throws Exception {
    maybeCreateDefaultConfigurationFiles();

    readCustomisations();

    overrides.clear();
    if (overridesFile.exists()) {
      overrides.putAll(LogbackOverrides.read(overridesFile));
    }

    maybeInstallCustomizationContextListener();

    File file = new File(configDir, F_LOGBACK_XML);
    log.debug("Configuring from: {}", file);

    LoggerContext context = getLoggerContext();
    Object bundleContext = context.getObject(PAX_BUNDLE_CONTEXT_KEY);
    NOPAppender nopAppender = new NOPAppender();
    nopAppender.setContext(context);
    nopAppender.start();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();

      // placeholder to avoid 'No appenders present' while reconfiguring
      context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(nopAppender);

      // restore persisted setting so pax-logging can reload
      context.putObject(PAX_BUNDLE_CONTEXT_KEY, bundleContext);

      context.getStatusManager().clear();
      installLevelChangePropagator();
      configurator.doConfigure(file);
    }
    catch (JoranException e) {
      // logging failed, fallback to SYSERR to inform user of failure
      synchronized (System.err) {
        System.err.println("Failed to apply logging configuration: " + e);
        e.printStackTrace();
      }
    }

    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    injectAppenders();
  }

  @Override
  protected void doStop() throws Exception {
    eventBus.unregister(this);
  }

  @Override
  @Guarded(by = STARTED)
  public Set<File> getLogFiles() {
    HashSet<File> files = new HashSet<>();

    LoggerContext ctx = getLoggerContext();
    for (Logger l : ctx.getLoggerList()) {
      ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) l;

      Iterator<Appender<ILoggingEvent>> iter = log.iteratorForAppenders();
      while (iter.hasNext()) {
        Appender<ILoggingEvent> appender = iter.next();
        if (appender instanceof FileAppender) {
          String path = ((FileAppender) appender).getFile();
          files.add(new File(path));
        }
      }
    }

    return files;
  }

  @Override
  @Nullable
  @Guarded(by = STARTED)
  public File getLogFile(final String fileName) {
    Set<File> files = getLogFiles();
    for (File file : files) {
      if (file.getName().equals(fileName)) {
        return file;
      }
    }
    return null;
  }

  /**
   * Retrieves a stream to the requested log file. This method ensures that the file is rooted in the log folder to
   * prevent browsing of the file system.
   *
   * @param fileName path of the file to retrieve
   * @return InputStream to the file or null if the file is not allowed or doesn't exist.
   */
  @Override
  @Nullable
  @Guarded(by = STARTED)
  public InputStream getLogFileStream(String fileName, long from, long count) throws IOException {
    log.debug("Retrieving log file: {}", fileName);

    if (fileName.contains(File.pathSeparator)) {
      log.warn("Nexus refuses to retrieve log files with path separators in its name.");
      return null;
    }

    File file = getLogFile(fileName);
    if (file == null || !file.exists()) {
      log.warn("Log file does not exist: {}", fileName);
      return null;
    }

    InputStream input = new BufferedInputStream(new FileInputStream(file));
    if (count >= 0) {
      return new LimitedInputStream(input, from, count);
    }
    else {
      return input;
    }
  }

  /**
   * @since 2.7
   */
  @Override
  @Guarded(by = STARTED)
  public Map<String, LoggerLevel> getLoggers() {
    Map<String, LoggerLevel> loggers = new HashMap<>();

    LoggerContext ctx = getLoggerContext();
    for (ch.qos.logback.classic.Logger logger : ctx.getLoggerList()) {
      String name = logger.getName();
      Level level = logger.getLevel();
      // only include loggers which explicit levels configured
      if (level != null) {
        loggers.put(name, convert(level));
      }
    }

    for (Entry<String, LoggerLevel> entry : customisations.entrySet()) {
      if (LoggerLevel.DEFAULT.equals(entry.getValue()) && !loggers.containsKey(entry.getKey())) {
        loggers.put(entry.getKey(), getLoggerEffectiveLevel(entry.getKey()));
      }
    }

    return loggers;
  }

  @Override
  @Guarded(by = STARTED)
  public void resetLoggers() {
    log.debug("Resetting loggers");

    for (Map.Entry<String, LoggerLevel> entry : overrides.entrySet()) {
      if (!Logger.ROOT_LOGGER_NAME.equals(entry.getKey())) {
        setLogbackLoggerLevel(entry.getKey(), null);
      }
    }
    overrides.clear();
    LogbackOverrides.write(overridesFile, overrides);
    setLoggerLevel(Logger.ROOT_LOGGER_NAME, LoggerLevel.DEFAULT);
    applyCustomisations();

    log.debug("Loggers reset to their default levels");
  }

  private Properties loadConfigurationProperties() throws IOException {
    maybeCreateDefaultConfigurationFiles();

    File file = new File(configDir, F_LOGBACK_PROPERTIES);
    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      Properties properties = new Properties();
      properties.load(in);
      return properties;
    }
  }

  private void saveConfigurationProperties(final Properties properties) throws IOException {
    final File file = new File(configDir, F_LOGBACK_PROPERTIES);
    log.debug("Saving configuration: {}", file);
    final FileReplacer fileReplacer = new FileReplacer(file);
    // we save this file many times, don't litter backups
    fileReplacer.setDeleteBackupFile(true);
    fileReplacer.replace(new ContentWriter()
    {
      @Override
      public void write(final BufferedOutputStream output) throws IOException {
        properties.store(output, "Saved by Nexus");
      }
    });
  }

  /**
   * Create default configuration files if needed.
   */
  private void maybeCreateDefaultConfigurationFiles() {
    log.debug("Preparing configuration files");

    // generate default properties
    File propertiesFile = new File(configDir, F_LOGBACK_PROPERTIES);
    if (!propertiesFile.exists()) {
      try {
        URL url = getClass().getResource(F_LOGBACK_PROPERTIES);
        try (InputStream is = url.openStream()) {
          FileSupport.copy(is, propertiesFile.toPath());
        }
      }
      catch (IOException e) {
        throw new IllegalStateException("Could not create logback.properties:" + propertiesFile.getAbsolutePath());
      }
      log.info("Created: {}", propertiesFile);
    }

    // generate participant files
    if (participants != null) {
      for (final LogConfigurationParticipant participant : participants) {
        final String name = participant.getName();
        final File participantFile = new File(configDir, name);
        if (participant instanceof LogConfigurationParticipant.NonEditable || !participantFile.exists()) {
          try {
            final FileReplacer fileReplacer = new FileReplacer(participantFile);
            // we save this file many times, don't litter backups
            fileReplacer.setDeleteBackupFile(true);
            fileReplacer.replace(new ContentWriter()
            {
              @Override
              public void write(final BufferedOutputStream output) throws IOException {
                try (final InputStream in = participant.getConfiguration()) {
                  StreamSupport.copy(in, output, StreamSupport.BUFFER_SIZE);
                }
              }
            });
            log.info("Created: {}", participantFile);
          }
          catch (IOException e) {
            throw new IllegalStateException(
                String.format("Could not create %s as %s", name, participantFile.getAbsolutePath()), e);
          }
        }
      }
    }

    // generate logback.xml
    final File logbackFile = new File(configDir, F_LOGBACK_XML);
    try {
      final FileReplacer fileReplacer = new FileReplacer(logbackFile);
      // we save this file many times, don't litter backups
      fileReplacer.setDeleteBackupFile(true);
      fileReplacer.replace(new ContentWriter()
      {
        @Override
        public void write(final BufferedOutputStream output) throws IOException {
          try (final PrintWriter out = new PrintWriter(output)) {
            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.println();
            out.println("<!--");
            out.println(
                "    DO NOT EDIT - This file aggregates log configuration from Nexus and its plugins, and is automatically generated.");
            out.println("-->");
            out.println();
            out.println("<configuration scan='true'>");

            // include properties
            out.format("  <property file='${%s}/logback.properties'/>%n", V_CONFIG_DIR);

            // include participant files
            if (participants != null) {
              for (LogConfigurationParticipant participant : participants) {
                out.format("  <include file='${%s}/%s'/>%n", V_CONFIG_DIR, participant.getName());
              }
            }

            // include overrides file
            if (overridesFile.exists()) {
              out.format("  <include file='${%s}/%s'/>%n", V_CONFIG_DIR, overridesFile.getName());
            }

            out.write("</configuration>");
          }
        }
      });
      log.info("Created: {}", logbackFile);
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not create logback.xml as " + logbackFile.getAbsolutePath());
    }
  }

  /**
   * Installs JUL {@link LevelChangePropagator} in context.
   *
   * @since 2.8
   */
  private void installLevelChangePropagator() {
    LoggerContext context = getLoggerContext();
    final LevelChangePropagator propagator = new LevelChangePropagator();
    propagator.setResetJUL(true);
    propagator.setContext(context);
    context.addListener(propagator);
  }

  private void injectAppenders() {
    LoggerContext ctx = getLoggerContext();

    for (Logger l : ctx.getLoggerList()) {
      ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) l;
      Iterator<Appender<ILoggingEvent>> iter = log.iteratorForAppenders();
      while (iter.hasNext()) {
        Appender<ILoggingEvent> appender = iter.next();
        injector.injectMembers(appender);
      }
    }
  }

  //
  // Logger levels
  //

  /**
   * Convert a Logback {@link Level} into a {@link LoggerLevel}.
   */
  private static LoggerLevel convert(final Level level) {
    switch (level.toInt()) {

      case Level.ERROR_INT:
        return LoggerLevel.ERROR;

      case Level.WARN_INT:
        return LoggerLevel.WARN;

      case Level.INFO_INT:
        return LoggerLevel.INFO;

      case Level.DEBUG_INT:
        return LoggerLevel.DEBUG;

      case Level.OFF_INT:
        return LoggerLevel.OFF;

      default:
        return LoggerLevel.TRACE;
    }
  }

  /**
   * Convert a {@link LoggerLevel} into a Logback {@link Level}.
   */
  private Level convert(final LoggerLevel level) {
    return Level.valueOf(level.name());
  }

  @Override
  @Guarded(by = STARTED)
  public void setLoggerLevel(final String name, final @Nullable LoggerLevel level) {
    if (level == null) {
      unsetLoggerLevel(name);
      return;
    }

    log.debug("Set logger level: {}={}", name, level);
    LoggerLevel calculated = null;
    if (Logger.ROOT_LOGGER_NAME.equals(name)) {
      try {
        calculated = LoggerLevel.DEFAULT.equals(level) ? LoggerLevel.INFO : level;
        Properties props = loadConfigurationProperties();
        props.setProperty("root.level", calculated.name());
        saveConfigurationProperties(props);
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    else {
      if (LoggerLevel.DEFAULT.equals(level)) {
        boolean customizedByUser = overrides.containsKey(name) && !customisations.containsKey(name);
        unsetLoggerLevel(name);
        if (customizedByUser) {
          overrides.put(name, calculated = getLoggerEffectiveLevel(name));
          LogbackOverrides.write(overridesFile, overrides);
        }
        else {
          LoggerLevel customizedLevel = customisations.get(name);
          if (customizedLevel != null && !LoggerLevel.DEFAULT.equals(customizedLevel)) {
            calculated = customizedLevel;
          }
        }
      }
      else {
        overrides.put(name, calculated = level);
        LogbackOverrides.write(overridesFile, overrides);
      }
    }
    if (calculated != null) {
      setLogbackLoggerLevel(name, convert(calculated));
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void unsetLoggerLevel(final String name) {
    log.debug("Unset logger level: {}", name);

    if (overrides.remove(name) != null) {
      LogbackOverrides.write(overridesFile, overrides);
    }
    if (Logger.ROOT_LOGGER_NAME.equals(name)) {
      setLoggerLevel(name, LoggerLevel.DEFAULT);
    }
    else {
      setLogbackLoggerLevel(name, null);
    }
  }

  @Override
  @Nullable
  @Guarded(by = STARTED)
  public LoggerLevel getLoggerLevel(final String name) {
    Level level = getLoggerContext().getLogger(name).getLevel();
    if (level != null) {
      return convert(level);
    }
    return null;
  }

  @Override
  @Guarded(by = STARTED)
  public LoggerLevel getLoggerEffectiveLevel(final String name) {
    return convert(getLoggerContext().getLogger(name).getEffectiveLevel());
  }

  private void setLogbackLoggerLevel(final String name, final Level level) {
    getLoggerContext().getLogger(name).setLevel(level);
  }

  //
  // Customizations
  //

  /**
   * Installs {@link CustomizationContextListener} if not already present in context.
   */
  private void maybeInstallCustomizationContextListener() {
    LoggerContext context = getLoggerContext();
    if (!context.getCopyOfListenerList().contains(customizationContextListener)) {
      context.addListener(customizationContextListener);
      log.debug("Nexus logger context listener installed");
    }
  }

  private void readCustomisations() {
    Configuration config = new Configuration()
    {
      @Override
      public void setLoggerLevel(final String name, final LoggerLevel level) {
        customisations.put(checkNotNull(name), checkNotNull(level));
      }
    };

    customisations.clear();
    for (LogConfigurationCustomizer customizer : customizers) {
      customizer.customize(config);
    }
  }

  private void applyCustomisations() {
    for (Entry<String, LoggerLevel> entry : customisations.entrySet()) {
      if (!LoggerLevel.DEFAULT.equals(entry.getValue())) {
        setLogbackLoggerLevel(entry.getKey(), convert(entry.getValue()));
      }
    }
  }

  /**
   * Logback context-listener to apply customizations on reset.
   */
  private class CustomizationContextListener
      implements LoggerContextListener
  {
    @Override
    public boolean isResetResistant() {
      return true;
    }

    @Override
    public void onStart(final LoggerContext context) {
      // do nothing
    }

    @Override
    public void onReset(final LoggerContext context) {
      applyCustomisations();
    }

    @Override
    public void onStop(final LoggerContext context) {
      // do nothing
    }

    @Override
    public void onLevelChange(final ch.qos.logback.classic.Logger logger, final Level level) {
      // do nothing
    }
  }

  //
  // Helpers
  //

  private static LoggerContext getLoggerContext() {
    ILoggerFactory factory = LoggerFactory.getILoggerFactory();
    if (factory instanceof LoggerContext) {
      return (LoggerContext) factory;
    }
    // temporary workaround for situations where SLF4j is not backed by logback
    return (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
  }
}
