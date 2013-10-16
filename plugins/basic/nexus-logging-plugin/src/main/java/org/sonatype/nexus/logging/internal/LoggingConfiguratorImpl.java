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

package org.sonatype.nexus.logging.internal;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.SAXParserFactory;

import org.sonatype.nexus.log.DefaultLogConfiguration;
import org.sonatype.nexus.log.LogConfiguration;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.logging.LoggerContributor;
import org.sonatype.nexus.logging.LoggingConfigurator;
import org.sonatype.nexus.logging.model.LevelXO;
import org.sonatype.nexus.logging.model.LoggerXO;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link LoggingConfigurator} implementation.
 *
 * @since 2.7
 */
@Named
@Singleton
public class LoggingConfiguratorImpl
    implements LoggingConfigurator
{

  public static final String ROOT = "ROOT";

  private final LogManager logManager;

  private final TemplateEngine templateEngine;

  private final ReadWriteLock lock;

  private final List<LoggerContributor> contributors;

  private final Map<String, LoggerXO> userLoggers;


  @Inject
  public LoggingConfiguratorImpl(final LogManager logManager,
                                 final TemplateEngine templateEngine,
                                 final List<LoggerContributor> contributors)
  {
    this.logManager = checkNotNull(logManager);
    this.templateEngine = checkNotNull(templateEngine);
    this.contributors = contributors;

    lock = new ReentrantReadWriteLock();
    userLoggers = getUserLoggers();
  }

  @Override
  public Collection<LoggerXO> getLoggers() {
    Map<String, LoggerXO> loggers = Maps.newHashMap();

    // include all runtime loggers which have explicit levels
    loggers.putAll(getRuntimeLoggers());

    // include all contributed loggers
    loggers.putAll(getContributedLoggers());

    try {
      lock.readLock().lock();
      // include all custom loggers added by users
      loggers.putAll(userLoggers);
    }
    finally {
      lock.readLock().unlock();
    }
    return loggers.values();
  }

  @Override
  public void setLevel(final String name, final LevelXO level) {
    checkNotNull(name, "name");
    checkNotNull(level, "level");

    try {
      lock.writeLock().lock();
      LoggerXO logger = userLoggers.get(name);
      if (logger == null) {
        userLoggers.put(name, logger = new LoggerXO().withName(name).withLevel(level));
      }
      logger.setLevel(level);
      configure();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void remove(final String name) {
    checkNotNull(name, "name");
    checkArgument(!ROOT.equals(name), ROOT + " logger cannot be removed");

    try {
      lock.writeLock().lock();
      userLoggers.remove(name);
      configure();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  private void configure() {
    try {
      writeLoggers();
      logManager.setConfiguration(getLogConfiguration());
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private void writeLoggers() throws IOException {
    final FileReplacer fileReplacer = new FileReplacer(
        logManager.getLogConfigFile(LoggingLogConfigurationParticipant.NAME)
    );
    fileReplacer.setDeleteBackupFile(true);
    fileReplacer.replace(new ContentWriter()
    {
      @Override
      public void write(final BufferedOutputStream output)
          throws IOException
      {
        URL template = this.getClass().getResource("logback-dynamic.vm"); //NON-NLS
        Map<String, LoggerXO> toWrite = Maps.newHashMap(userLoggers);
        toWrite.remove(ROOT);
        String content = templateEngine.render(
            this,
            template,
            new TemplateParameters().set("loggers", toWrite.values())
        );
        try (final InputStream in = new ByteArrayInputStream(content.getBytes())) {
          ByteStreams.copy(in, output);
        }
      }
    });
  }

  private LogConfiguration getLogConfiguration() throws IOException {
    LogConfiguration configuration = logManager.getConfiguration();
    String rootLoggerLevel = userLoggers.get(ROOT).getLevel().toString();
    if (!configuration.getRootLoggerLevel().equals(rootLoggerLevel)) {
      DefaultLogConfiguration newConfiguration = new DefaultLogConfiguration(configuration);
      newConfiguration.setRootLoggerLevel(rootLoggerLevel);
      configuration = newConfiguration;
    }
    return configuration;
  }

  private Map<String, LoggerXO> getUserLoggers() {
    try {
      final Map<String, LoggerXO> loggers = Maps.newHashMap();
      loggers.clear();
      String rootLevel = logManager.getConfiguration().getRootLoggerLevel();
      loggers.put(ROOT, new LoggerXO().withName(ROOT).withLevel(LevelXO.valueOf(rootLevel)));

      File dynamicLoggersFile = logManager.getLogConfigFile(LoggingLogConfigurationParticipant.NAME);
      if (dynamicLoggersFile.exists()) {
        try {
          SAXParserFactory spf = SAXParserFactory.newInstance();
          spf.setValidating(false);
          spf.setNamespaceAware(true);
          spf.newSAXParser().parse(dynamicLoggersFile, new DefaultHandler()
          {
            @Override
            public void startElement(final String uri,
                                     final String localName,
                                     final String qName,
                                     final Attributes attributes) throws SAXException
            {
              if ("logger".equals(localName)) {
                String name = attributes.getValue("name");
                String level = attributes.getValue("level");
                loggers.put(
                    name,
                    new LoggerXO().withName(name).withLevel(LevelXO.valueOf(level))
                );
              }
            }
          });
        }
        catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }

      return loggers;
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Return mapping of existing runtime loggers which have explicit levels configured.
   */
  private Map<String, LoggerXO> getRuntimeLoggers() {
    return Maps.transformEntries(logManager.getLoggers(), new EntryTransformer<String, LoggerLevel, LoggerXO>()
    {
      @Override
      public LoggerXO transformEntry(final String key, final LoggerLevel value) {
        return new LoggerXO().withName(key).withLevel(LevelXO.valueOf(value.name()));
      }
    });
  }

  private Map<String, LoggerXO> getContributedLoggers() {
    Map<String, LoggerXO> loggers = Maps.newHashMap();
    for (LoggerContributor contributor : contributors) {
      Set<String> contributedLoggers = contributor.getLoggers();
      if (contributedLoggers != null) {
        for (String loggerName : contributedLoggers) {
          Logger logger = LoggerFactory.getLogger(loggerName);
          LevelXO level = levelOf(logger);
          loggers.put(loggerName, new LoggerXO().withName(loggerName).withLevel(level));
        }
      }
    }
    return loggers;
  }

  /**
   * Get the level of a Slf4j {@link Logger}.
   */
  private LevelXO levelOf(final Logger logger) {
    if (logger.isTraceEnabled()) {
      return LevelXO.TRACE;
    }
    else if (logger.isDebugEnabled()) {
      return LevelXO.DEBUG;
    }
    else if (logger.isInfoEnabled()) {
      return LevelXO.INFO;
    }
    else if (logger.isWarnEnabled()) {
      return LevelXO.WARN;
    }
    else if (logger.isErrorEnabled()) {
      return LevelXO.ERROR;
    }
    return LevelXO.OFF;
  }

}
