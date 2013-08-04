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
import java.io.FileNotFoundException;
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
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.nexus.LimitedInputStream;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.log.DefaultLogConfiguration;
import org.sonatype.nexus.log.DefaultLogManagerMBean;
import org.sonatype.nexus.log.LogConfiguration;
import org.sonatype.nexus.log.LogConfigurationParticipant;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.io.ByteStreams;
import com.google.inject.Injector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO configuration operations should be locking

/**
 * @author cstamas
 * @author juven
 * @author adreghiciu@gmail.com
 */
@Component(role = LogManager.class)
public class LogbackLogManager
    implements LogManager, Disposable
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

  @Requirement(role = LogConfigurationParticipant.class)
  private List<LogConfigurationParticipant> logConfigurationParticipants;

  @Requirement
  private Injector injector;

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  private ObjectName jmxName;

  public LogbackLogManager() {
    try {
      jmxName = ObjectName.getInstance(JMX_DOMAIN, "name", LogManager.class.getSimpleName());
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.registerMBean(new DefaultLogManagerMBean(this), jmxName);
    }
    catch (Exception e) {
      jmxName = null;
      // FIXME: when switched over SISU, this will be a non issue
      // sadly, plexus does field injection and logger not yet avail
      // getLogger().warn( "Problem registering MBean for: " + getClass().getName(), e );
    }
  }

  @Override
  public void dispose() {
    if (null != jmxName) {
      try {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(jmxName);
      }
      catch (final Exception e) {
        logger.warn("Problem unregistering MBean for: " + getClass().getName(), e);
      }
    }
  }

  public Set<File> getLogFiles() {
    HashSet<File> files = new HashSet<File>();

    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

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

  public File getLogFile(String filename) {
    Set<File> logFiles = getLogFiles();

    for (File logFile : logFiles) {
      if (logFile.getName().equals(filename)) {
        return logFile;
      }
    }

    return null;
  }

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

    response.setFromByte(from);

    response.setBytesCount(count);

    response.setInputStream(new LimitedInputStream(new FileInputStream(log), from, count));

    return response;
  }

  @Override
  public void configure() {
    // TODO maybe do some optimization that if participants does not change, do not reconfigure
    prepareConfigurationFiles();
    reconfigure();
  }

  private Properties loadConfigurationProperties()
      throws IOException
  {
    prepareConfigurationFiles();
    String logConfigDir = getLogConfigDir();
    File logConfigPropsFile = new File(logConfigDir, LOG_CONF_PROPS);
    InputStream in = null;
    try {
      in = new FileInputStream(logConfigPropsFile);

      Properties properties = new Properties();
      properties.load(in);

      return properties;
    }
    finally {
      IOUtil.close(in);
    }
  }

  private void saveConfigurationProperties(final Properties properties)
      throws FileNotFoundException, IOException
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

    if (StringUtils.isEmpty(logConfigDir)) {
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

        FileUtils.copyURLToFile(configUrl, logConfigPropsFile);
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
                  ByteStreams.copy(in, output);
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
                out.println(String.format("  <include file='${nexus.log-config-dir}/%s'/>",
                    participant.getName()));
              }
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

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

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
  }

  private void injectAppenders() {
    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

    for (Logger l : ctx.getLoggerList()) {
      ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) l;
      Iterator<Appender<ILoggingEvent>> it = log.iteratorForAppenders();

      while (it.hasNext()) {
        Appender<ILoggingEvent> ap = it.next();
        injector.injectMembers(ap);
      }
    }
  }

}
