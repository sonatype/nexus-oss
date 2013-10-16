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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.sonatype.nexus.log.LogConfiguration;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.logging.LoggerContributor;
import org.sonatype.nexus.logging.model.LevelXO;
import org.sonatype.nexus.logging.model.LoggerXO;
import org.sonatype.nexus.plugin.internal.SharedTemplateEngineProvider;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.litmus.testsupport.hamcrest.DiffMatchers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

/**
 * {@link LoggingConfiguratorImpl} UTs.
 *
 * @since 2.7
 */

public class LoggingConfiguratorImplTest
    extends TestSupport
{

  private TemplateEngine templateEngine;

  @Mock
  private LogConfiguration logConfiguration;

  @Mock
  private LogManager logManager;

  @Mock
  private LoggerContributor loggerContributor;


  private LoggingConfiguratorImpl underTest;

  @Before
  public void prepare() throws IOException {
    File logbackXml = util.createTempFile();
    FileUtils.write(logbackXml, IOUtils.toString(getClass().getResourceAsStream("logback-expected.xml")));

    templateEngine = new SharedTemplateEngineProvider().get();
    when(logConfiguration.getRootLoggerLevel()).thenReturn("DEBUG");
    when(logManager.getConfiguration()).thenReturn(logConfiguration);
    when(logManager.getLogOverridesConfigFile()).thenReturn(logbackXml);
    when(loggerContributor.getLoggers()).thenReturn(Sets.newHashSet("contributed"));

    underTest = new LoggingConfiguratorImpl(
        logManager, templateEngine, Arrays.asList(loggerContributor)
    );
  }

  /**
   * Verify that initial list of loggers contains the ROOT, user loggers and contributed loggers with effective level
   * calculated.
   */
  @Test
  public void initialListOfLoggers() throws Exception {
    Collection<String> actual = convertToStringList(underTest.getLoggers());
    String contributedLevel = underTest.levelOf(LoggerFactory.getLogger("contributed")).name();
    assertThat(actual, hasItems("ROOT|DEBUG", "contributed|" + contributedLevel, "foo|ERROR", "bar|INFO"));
  }

  /**
   * Verify that setting the level of a user logger wil be reflected in returned loggers.
   */
  @Test
  public void setLevelOfUserLogger() throws Exception {
    underTest.setLevel("foo", LevelXO.DEBUG);
    Collection<String> actual = convertToStringList(underTest.getLoggers());
    String contributedLevel = underTest.levelOf(LoggerFactory.getLogger("contributed")).name();
    assertThat(actual, hasItems("ROOT|DEBUG", "contributed|" + contributedLevel, "foo|DEBUG", "bar|INFO"));
  }

  /**
   * Verify that setting the level of a contributed logger wil be reflected in returned loggers.
   */
  @Test
  public void setLevelOfContributedLogger() throws Exception {
    underTest.setLevel("contributed", LevelXO.OFF);
    Collection<String> actual = convertToStringList(underTest.getLoggers());
    assertThat(actual, hasItems("ROOT|DEBUG", "contributed|OFF", "foo|ERROR", "bar|INFO"));
  }

  /**
   * Verify that setting the level of ROOT logger wil be reflected in returned loggers.
   */
  @Test
  public void setLevelOfRootLogger() throws Exception {
    underTest.setLevel("ROOT", LevelXO.OFF);
    Collection<String> actual = convertToStringList(underTest.getLoggers());
    String contributedLevel = underTest.levelOf(LoggerFactory.getLogger("contributed")).name();
    assertThat(actual, hasItems("ROOT|OFF", "contributed|" + contributedLevel, "foo|ERROR", "bar|INFO"));
  }

  /**
   * Verify that loggers are written in an expected logback format.
   */
  @Test
  public void writeLogbackXml() throws Exception {
    File logbackXml = util.createTempFile();
    underTest.writeLogbackXml(
        Arrays.asList(
            new LoggerXO().withName("foo").withLevel(LevelXO.ERROR),
            new LoggerXO().withName("bar").withLevel(LevelXO.INFO)
        ),
        logbackXml,
        templateEngine
    );
    String expected = IOUtils.toString(getClass().getResourceAsStream("logback-expected.xml"));
    String actual = FileUtils.readFileToString(logbackXml);
    assertThat(actual, DiffMatchers.equalTo(expected));
  }

  /**
   * Verify that loggers are read from logback format.
   */
  @Test
  public void readLogbackXml() throws Exception {
    File logbackXml = util.createTempFile();
    FileUtils.write(logbackXml, IOUtils.toString(getClass().getResourceAsStream("logback-expected.xml")));
    Collection<String> actual = convertToStringList(underTest.readLogbackXml(logbackXml));
    assertThat(actual, contains("foo|ERROR", "bar|INFO"));
  }

  private Collection<String> convertToStringList(final Collection<LoggerXO> loggers) {
    return Collections2.transform(loggers, new Function<LoggerXO, String>()
    {
      @Nullable
      @Override
      public String apply(@Nullable final LoggerXO input) {
        if (input == null) {
          return null;
        }
        return input.getName() + "|" + input.getLevel().name();
      }
    });
  }

}
