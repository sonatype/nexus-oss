/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.log.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.nexus.LimitedInputStream;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.log.DefaultLogConfiguration;
import org.sonatype.nexus.log.DefaultLogManagerMBean;
import org.sonatype.nexus.log.LogConfiguration;
import org.sonatype.nexus.log.LogConfigurationCustomizer;
import org.sonatype.nexus.log.LogConfigurationCustomizer.Configuration;
import org.sonatype.nexus.log.LogConfigurationParticipant;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.util.file.FileSupport;
import org.sonatype.nexus.util.io.StreamSupport;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

//TODO configuration operations should be locking

/**
 * @author cstamas
 * @author juven
 * @author adreghiciu@gmail.com
 */
@Singleton
@Named
public class LogbackLogManager
    implements LogManager
{
  private static final String JMX_DOMAIN = "org.sonatype.nexus.log";

  private static final String KEY_APPENDER_FILE = "appender.file";

  private static final String KEY_APPENDER_PATTERN = "appender.pattern";

  private static final String KEY_ROOT_LEVEL = "root.level";

  private static final String KEY_LOG_CONFIG_DIR = "nexus.log-config-dir";

  private static final String LOG_CONF = "logback.xml";

  private static final String LOG_CONF_PROPS = "logback.properties";

  private static final String LOG_CONF_PROPS_RESOURCE = "/META-INF/log/" + LOG_CONF_PROPS;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Injector injector;

  private final ApplicationConfiguration applicationConfiguration;

  private final List<LogConfigurationParticipant> logConfigurationParticipants;

  private final List<LogConfigurationCustomizer> logConfigurationCustomizers;

  private final EventBus eventBus;

  private final Map<String, LoggerLevel> overrides;

  private final Set<String> contributed;

  private ObjectName jmxName;

  @Inject
  public LogbackLogManager(final Injector injector,
                           final ApplicationConfiguration applicationConfiguration,
                           final List<LogConfigurationParticipant> logConfigurationParticipants,
                           final List<LogConfigurationCustomizer> logConfigurationCustomizers,
                           final EventBus eventBus)
  {
    this.injector = checkNotNull(injector);
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
    this.logConfigurationParticipants = checkNotNull(logConfigurationParticipants);
    this.logConfigurationCustomizers = checkNotNull(logConfigurationCustomizers);
    this.eventBus = checkNotNull(eventBus);
    this.overrides = Maps.newHashMap();
    this.contributed = Sets.newHashSet();
    try {
      jmxName = ObjectName.getInstance(JMX_DOMAIN, "name", LogManager.class.getSimpleName());
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.registerMBean(new DefaultLogManagerMBean(this), jmxName);
    }
    catch (Exception e) {
      jmxName = null;
      logger.warn("Problem registering MBean for: " + getClass().getName(), e);
    }
    eventBus.register(this);
  }

  @Subscribe
  public void on(final NexusInitializedEvent evt) {
    configure();
  }

  private LoggerContext getLoggerContext() {
    return (LoggerContext) LoggerFactory.getILoggerFactory();
  }

  @Override
  public synchronized void configure() {
    // TODO maybe do some optimization that if participants does not change, do not reconfigure
    prepareConfigurationFiles();
    overrides.clear();
    File logOverridesConfigFile = getLogOverridesConfigFile();
    if (logOverridesConfigFile.exists()) {
      overrides.putAll(LogbackOverrides.read(logOverridesConfigFile));
    }
    reconfigure();
  }

  @Override
  public synchronized void shutdown() {
    if (null != jmxName) {
      try {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(jmxName);
      }
      catch (final Exception e) {
        logger.warn("Problem unregistering MBean for: " + getClass().getName(), e);
      }
    }
    eventBus.unregister(this);
  }

  /**
   * @since 2.7
   */
  @Override
  public File getLogConfigFile(final String name) {
    return new File(getLogConfigDir(), name);
  }

  /**
   * @since 2.7
   */
  @Override
  public File getLogOverridesConfigFile() {
    return getLogConfigFile("logback-overrides.xml");
  }

  /**
   * @since 2.7
   */
  @Override
  public Map<String, LoggerLevel> getLoggers() {
    Map<String, LoggerLevel> loggers = Maps.newHashMap();

    LoggerContext loggerContext = getLoggerContext();
    for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList()) {
      String name = logger.getName();
      Level level = logger.getLevel();
      // only include loggers which explicit levels configured
      if (level != null) {
        loggers.put(name, convert(level));
      }
    }

    for (String name : contributed) {
      if (!loggers.containsKey(name)) {
        loggers.put(name, getLoggerEffectiveLevel(name));
      }
    }

    return loggers;
  }

  @Override
  public Set<File> getLogFiles() {
    HashSet<File> files = new HashSet<File>();

    LoggerContext ctx = getLoggerContext();

    for (Logger l : ctx.getLoggerList()) {
      ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) l;
      Iterator<Appender<ILoggingEvent>> it = log.iteratorForAppenders();

      while (it.hasNext()) {
        Appender<ILoggingEvent> ap = it.next();

        if (ap instanceof FileAppender<?> || ap instanceof RollingFileAppender<?>) {
          FileAppender<?> fileAppender = (FileAppender<?>) ap;
          String path = fileAppender.getFile();
          files.add(new File(path));
        }
      }
    }

    return files;
  }

  @Override
  public File getLogFile(String filename) {
    Set<File> logFiles = getLogFiles();

    for (File logFile : logFiles) {
      if (logFile.getName().equals(filename)) {
        return logFile;
      }
    }

    return null;
  }

  @Override
  public LogConfiguration getConfiguration()
      throws IOException
  {
    Properties logProperties = loadConfigurationProperties();
    DefaultLogConfiguration configuration = new DefaultLogConfiguration();

    configuration.setRootLoggerLevel(logProperties.getProperty(KEY_ROOT_LEVEL));
    // TODO
    configuration.setRootLoggerAppenders("console,file");
    configuration.setFileAppenderPattern(logProperties.getProperty(KEY_APPENDER_PATTERN));
    configuration.setFileAppenderLocation(logProperties.getProperty(KEY_APPENDER_FILE));

    return configuration;
  }

  @Override
  public void setConfiguration(LogConfiguration configuration)
      throws IOException
  {
    Properties logProperties = loadConfigurationProperties();

    logProperties.setProperty(KEY_ROOT_LEVEL, configuration.getRootLoggerLevel());
    String pattern = configuration.getFileAppenderPattern();

    if (pattern == null) {
      pattern = getDefaultProperties().getProperty(KEY_APPENDER_PATTERN);
    }

    logProperties.setProperty(KEY_APPENDER_PATTERN, pattern);

    saveConfigurationProperties(logProperties);
    // TODO this will do a reconfiguration but would be just enough to "touch" logback.xml"
    reconfigure();
  }

  private Properties getDefaultProperties()
      throws IOException
  {
    Properties properties = new Properties();
    final InputStream stream = this.getClass().getResourceAsStream(LOG_CONF_PROPS_RESOURCE);
    try {
      properties.load(stream);
    }
    finally {
      stream.close();
    }
    return properties;
  }

  @Override
  public Collection<NexusStreamResponse> getApplicationLogFiles()
      throws IOException
  {
    logger.debug("List log files.");

    Set<File> files = getLogFiles();

    ArrayList<NexusStreamResponse> result = new ArrayList<NexusStreamResponse>(files.size());

    for (File file : files) {
      NexusStreamResponse response = new NexusStreamResponse();

      response.setName(file.getName());

      // TODO:
      response.setMimeType("text/plain");

      response.setSize(file.length());

      response.setInputStream(null);

      result.add(response);
    }

    return result;
  }

  /**
   * Retrieves a stream to the requested log file. This method ensures that the file is rooted in the log folder to
   * prevent browsing of the file system.
   *
   * @param logFile path of the file to retrieve
   * @returns InputStream to the file or null if the file is not allowed or doesn't exist.
   */
  @Override
  public NexusStreamResponse getApplicationLogAsStream(String logFile, long from, long count)
      throws IOException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Retrieving " + logFile + " log file.");
    }

    if (logFile.contains(File.pathSeparator)) {
      logger.warn("Nexus refuses to retrieve log files with path separators in its name.");

      return null;
    }

    File log = getLogFile(logFile);

    if (log == null || !log.exists()) {
      logger.warn("Log file does not exist: [" + logFile + "]");

      return null;
    }

    NexusStreamResponse response = new NexusStreamResponse();

    response.setName(logFile);

    response.setMimeType("text/plain");

    response.setSize(log.length());

    if (count >= 0) {
      response.setFromByte(from);
      response.setBytesCount(count);
    }
    else {
      response.setBytesCount(Math.abs(count));
      response.setFromByte(Math.max(0, response.getSize() - response.getBytesCount()));
    }

    response.setInputStream(
        new LimitedInputStream(new FileInputStream(log), response.getFromByte(), response.getBytesCount())
    );

    return response;
  }

  private Properties loadConfigurationProperties()
      throws IOException
  {
    prepareConfigurationFiles();
    String logConfigDir = getLogConfigDir();
    File logConfigPropsFile = new File(logConfigDir, LOG_CONF_PROPS);
    try (final InputStream in = new FileInputStream(logConfigPropsFile)) {
      Properties properties = new Properties();
      properties.load(in);
      return properties;
    }
  }

  private void saveConfigurationProperties(final Properties properties)
      throws IOException
  {
    final File configurationFile = new File(getLogConfigDir(), LOG_CONF_PROPS);
    logger.debug("Saving configuration: {}", configurationFile);
    final FileReplacer fileReplacer = new FileReplacer(configurationFile);
    // we save this file many times, don't litter backups
    fileReplacer.setDeleteBackupFile(true);
    fileReplacer.replace(new ContentWriter()
    {
      @Override
      public void write(final BufferedOutputStream output)
          throws IOException
      {
        properties.store(output, "Saved by Nexus");
      }
    });
  }

  private String getLogConfigDir() {
    String logConfigDir = System.getProperty(KEY_LOG_CONFIG_DIR);

    if (Strings.isNullOrEmpty(logConfigDir)) {
      logConfigDir = applicationConfiguration.getConfigurationDirectory().getAbsolutePath();

      System.setProperty(KEY_LOG_CONFIG_DIR, logConfigDir);
    }

    return logConfigDir;
  }

  private void prepareConfigurationFiles() {
    String logConfigDir = getLogConfigDir();

    File logConfigPropsFile = new File(logConfigDir, LOG_CONF_PROPS);
    if (!logConfigPropsFile.exists()) {
      try {
        URL configUrl = this.getClass().getResource(LOG_CONF_PROPS_RESOURCE);
        try (final InputStream is = configUrl.openStream()) {
          FileSupport.copy(is, logConfigPropsFile.toPath());
        }
      }
      catch (IOException e) {
        throw new IllegalStateException("Could not create logback.properties as "
            + logConfigPropsFile.getAbsolutePath());
      }
    }

    if (logConfigurationParticipants != null) {
      for (final LogConfigurationParticipant participant : logConfigurationParticipants) {
        final String name = participant.getName();
        final File logConfigFile = new File(logConfigDir, name);
        if (participant instanceof LogConfigurationParticipant.NonEditable || !logConfigFile.exists()) {
          try {
            final FileReplacer fileReplacer = new FileReplacer(logConfigFile);
            // we save this file many times, don't litter backups
            fileReplacer.setDeleteBackupFile(true);
            fileReplacer.replace(new ContentWriter()
            {
              @Override
              public void write(final BufferedOutputStream output)
                  throws IOException
              {
                try (final InputStream in = participant.getConfiguration()) {
                  StreamSupport.copy(in, output);
                }
              }
            });
          }
          catch (IOException e) {
            throw new IllegalStateException(String.format("Could not create %s as %s", name,
                logConfigFile.getAbsolutePath()), e);
          }
        }
      }
    }
    final File logConfigFile = new File(logConfigDir, LOG_CONF);
    try {
      final FileReplacer fileReplacer = new FileReplacer(logConfigFile);
      // we save this file many times, don't litter backups
      fileReplacer.setDeleteBackupFile(true);
      fileReplacer.replace(new ContentWriter()
      {
        @Override
        public void write(final BufferedOutputStream output)
            throws IOException
        {
          try (final PrintWriter out = new PrintWriter(output)) {
            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.println();
            out.println("<!--");
            out.println(
                "    DO NOT EDIT - This file aggregates log configuration from Nexus and its plugins, and is automatically generated.");
            out.println("-->");
            out.println();
            out.println("<configuration scan='true'>");
            out.println("  <property file='${nexus.log-config-dir}/logback.properties'/>");
            if (logConfigurationParticipants != null) {
              for (LogConfigurationParticipant participant : logConfigurationParticipants) {
                out.println(String.format(
                    "  <include file='${nexus.log-config-dir}/%s'/>", participant.getName())
                );
              }
            }
            File logOverridesConfigFile = getLogOverridesConfigFile();
            if (logOverridesConfigFile.exists()) {
              out.println(String.format(
                  "  <include file='${nexus.log-config-dir}/%s'/>", logOverridesConfigFile.getName())
              );
            }
            out.write("</configuration>");
          }
        }
      });
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not create logback.xml as " + logConfigFile.getAbsolutePath());
    }
  }

  private void reconfigure() {
    String logConfigDir = getLogConfigDir();

    LoggerContext lc = getLoggerContext();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      lc.getStatusManager().clear();
      configurator.doConfigure(new File(logConfigDir, LOG_CONF));
    }
    catch (JoranException je) {
      je.printStackTrace();
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    injectAppenders();

    Configuration customizerConfiguration = new Configuration()
    {
      @Override
      public void setLoggerLevel(final String name, final LoggerLevel level) {
        if (LoggerLevel.DEFAULT.equals(checkNotNull(level, "level"))) {
          contributed.add(name);
        }
        else if (getLoggerLevel(name) == null) {
          getLoggerContext().getLogger(name).setLevel(convert(level));
        }
      }
    };
    contributed.clear();
    for (LogConfigurationCustomizer customizer : logConfigurationCustomizers) {
      customizer.customize(customizerConfiguration);
    }
  }

  private void injectAppenders() {
    LoggerContext ctx = getLoggerContext();

    for (Logger l : ctx.getLoggerList()) {
      ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) l;
      Iterator<Appender<ILoggingEvent>> it = log.iteratorForAppenders();

      while (it.hasNext()) {
        Appender<ILoggingEvent> ap = it.next();
        injector.injectMembers(ap);
      }
    }
  }

  /**
   * Convert a Logback {@link Level} into a {@link LoggerLevel}.
   */
  private LoggerLevel convert(final Level level) {
    switch (level.toInt()) {

      case Level.ERROR_INT:
        return LoggerLevel.ERROR;

      case Level.WARN_INT:
        return LoggerLevel.WARN;

      case Level.INFO_INT:
        return LoggerLevel.INFO;

      case Level.DEBUG_INT:
        return LoggerLevel.DEBUG;

      case Level.TRACE_INT:
        return LoggerLevel.TRACE;

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
  public void setLoggerLevel(final String name, final @Nullable LoggerLevel level) {
    if (level == null) {
      unsetLoggerLevel(name);
      return;
    }

    if (Logger.ROOT_LOGGER_NAME.equals(name)) {
      try {
        LoggerLevel calculated = LoggerLevel.DEFAULT.equals(level) ? LoggerLevel.INFO : level;
        Properties logProperties = loadConfigurationProperties();
        logProperties.setProperty(KEY_ROOT_LEVEL, calculated.name());
        saveConfigurationProperties(logProperties);
        // we need to reconfigure as just settings ROOT logger level results in some loggers to be unset
        reconfigure();
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    else {
      LoggerLevel calculated = null;
      if (LoggerLevel.DEFAULT.equals(level)) {
        boolean customizedByUser = overrides.containsKey(name) && !contributed.contains(name);
        unsetLoggerLevel(name);
        if (customizedByUser) {
          overrides.put(name, calculated = getLoggerEffectiveLevel(name));
        }
      }
      else {
        overrides.put(name, calculated = level);
      }
      LogbackOverrides.write(getLogOverridesConfigFile(), overrides);
      if (calculated != null) {
        getLoggerContext().getLogger(name).setLevel(convert(calculated));
      }
    }
  }

  @Override
  public void unsetLoggerLevel(final String name) {
    if (overrides.remove(name) != null) {
      LogbackOverrides.write(getLogOverridesConfigFile(), overrides);
    }
    if (Logger.ROOT_LOGGER_NAME.equals(name)) {
      setLoggerLevel(name, LoggerLevel.DEFAULT);
    }
    else {
      getLoggerContext().getLogger(name).setLevel(null);
    }
  }

  @Override
  @Nullable
  public LoggerLevel getLoggerLevel(final String name) {
    Level level = getLoggerContext().getLogger(name).getLevel();
    if (level != null) {
      return convert(level);
    }
    return null;
  }

  @Override
  public LoggerLevel getLoggerEffectiveLevel(final String name) {
    return convert(getLoggerContext().getLogger(name).getEffectiveLevel());
  }

}
