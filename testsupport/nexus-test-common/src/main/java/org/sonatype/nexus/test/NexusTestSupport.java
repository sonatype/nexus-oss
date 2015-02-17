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
package org.sonatype.nexus.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.sonatype.sisu.litmus.testsupport.TestUtil;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Abstract test case for nexus tests.
 */
public abstract class NexusTestSupport
{
  public static final String WORK_CONFIGURATION_KEY = "nexus-work";

  public static final String APPS_CONFIGURATION_KEY = "apps";

  public static final String CONF_DIR_KEY = "application-conf";

  public static final String NEXUS_BASE_CONFIGURATION_KEY = "nexus-base";

  public static final String NEXUS_APP_CONFIGURATION_KEY = "nexus-app";

  protected final TestUtil util = new TestUtil(this);

  // HACK: Force user.basedir to the detected directory by TestUtil for better IDE integration for execution of tests
  // HACK: Many tests assume this directory is the maven module directory, when it may not be.
  {
    System.setProperty("user.dir", util.getBaseDir().getAbsolutePath());
  }

  private PlexusContainer container;

  private Properties sysPropsBackup;

  private File plexusHomeDir = null;

  private File appsHomeDir = null;

  private File workHomeDir = null;

  private File confHomeDir = null;

  private File baseHomeDir = null;

  private File nexusappHomeDir = null;

  private File tempDir = null;

  /**
   * Helper to call old JUnit 3x style {@link #setUp()}
   */
  @Before
  final public void setUpJunit() throws Exception {
    setUp();
  }

  protected void setUp() throws Exception {
    sysPropsBackup = System.getProperties();

    // simply to make sure customizeContext is handled before anything else
    getContainer();

    plexusHomeDir.mkdirs();
    appsHomeDir.mkdirs();
    workHomeDir.mkdirs();
    confHomeDir.mkdirs();
    baseHomeDir.mkdirs();
    nexusappHomeDir.mkdirs();
    tempDir.mkdirs();
  }

  protected void setupContainer() {
    final DefaultContext context = new DefaultContext();
    context.put("basedir", getBasedir());
    customizeContext(context);

    final boolean hasPlexusHome = context.contains("plexus.home");
    if (!hasPlexusHome) {
      final File f = getTestFile("target/plexus-home");
      if (!f.isDirectory()) {
        f.mkdir();
      }

      context.put("plexus.home", f.getAbsolutePath());
    }

    final String config = getCustomConfigurationName();
    final ContainerConfiguration containerConfiguration =
        new DefaultContainerConfiguration().setName("test").setContext(context.getContextData());

    if (config != null) {
      containerConfiguration.setContainerConfiguration(config);
    }
    else {
      final String resource = getConfigurationName(null);
      containerConfiguration.setContainerConfiguration(resource);
    }

    customizeContainerConfiguration(containerConfiguration);

    try {
      List<Module> modules = Lists.newLinkedList();
      Module[] customModules = getTestCustomModules();
      if (customModules != null) {
        modules.addAll(Lists.newArrayList(customModules));
      }
      customizeModules(modules);

      container = new DefaultPlexusContainer(containerConfiguration, modules.toArray(new Module[modules.size()]));
    }
    catch (final PlexusContainerException e) {
      e.printStackTrace();
      Assert.fail("Failed to create plexus container.");
    }
  }

  /**
   * @deprecated Use {@link #customizeModules(List)} instead.
   */
  @Deprecated
  protected Module[] getTestCustomModules() {
    return null;
  }

  protected void customizeModules(final List<Module> modules) {
    // empty
  }

  /**
   * @deprecated Avoid usage of Plexus apis.
   */
  @Deprecated
  protected void customizeContainerConfiguration(final ContainerConfiguration containerConfiguration) {
    containerConfiguration.setAutoWiring(true);
    containerConfiguration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);
  }

  /**
   * @deprecated Avoid usage of Plexus apis.
   */
  @Deprecated
  protected void customizeContext(final Context ctx) {
    plexusHomeDir = new File(
        getBasedir(), "target/plexus-home-" + new Random(System.currentTimeMillis()).nextLong()
    );
    appsHomeDir = new File(plexusHomeDir, "apps");
    workHomeDir = new File(plexusHomeDir, "nexus-work");
    confHomeDir = new File(workHomeDir, "etc");
    baseHomeDir = new File(plexusHomeDir, "nexus-base");
    nexusappHomeDir = new File(plexusHomeDir, "nexus-app");
    tempDir = new File(workHomeDir, "tmp");

    ctx.put(WORK_CONFIGURATION_KEY, workHomeDir.getAbsolutePath());
    ctx.put(APPS_CONFIGURATION_KEY, appsHomeDir.getAbsolutePath());
    ctx.put(CONF_DIR_KEY, confHomeDir.getAbsolutePath());
    ctx.put(NEXUS_BASE_CONFIGURATION_KEY, baseHomeDir.getAbsolutePath());
    ctx.put(NEXUS_APP_CONFIGURATION_KEY, nexusappHomeDir.getAbsolutePath());
    ctx.put("java.io.tmpdir", tempDir.getAbsolutePath());
  }

  /**
   * Helper to call old JUnit 3x style {@link #tearDown()}
   */
  @After
  final public void tearDownJunit() throws Exception {
    tearDown();
  }

  protected void tearDown() throws Exception {
    try {
      if (container != null) {
        container.dispose();

        container = null;
      }
    }
    finally {
      System.setProperties(sysPropsBackup);
    }

    cleanDir(plexusHomeDir);
  }

  protected void cleanDir(File dir) {
    if (dir != null) {
      try {
        FileUtils.deleteDirectory(dir);
      }
      catch (IOException e) {
        // couldn't delete directory, too bad
      }
    }
  }

  /**
   * @deprecated Avoid usage of Plexus apis.
   */
  @Deprecated
  protected PlexusContainer getContainer() {
    if (container == null) {
      setupContainer();
    }

    return container;
  }

  /**
   * @deprecated Avoid usage of Plexus apis (this is used to access plexus xml configuration files).
   */
  @Deprecated
  protected InputStream getConfiguration() throws Exception {
    return getConfiguration(null);
  }

  /**
   * @deprecated Avoid usage of Plexus apis (this is used to access plexus xml configuration files).
   */
  @Deprecated
  protected InputStream getConfiguration(final String subname) throws Exception {
    return getResourceAsStream(getConfigurationName(subname));
  }

  /**
   * @deprecated Avoid usage of Plexus apis (this is used to access plexus xml configuration files).
   */
  @Deprecated
  protected String getCustomConfigurationName() {
    return null;
  }

  /**
   * @deprecated Avoid usage of Plexus apis (this is used to access plexus xml configuration files).
   */
  @Deprecated
  protected String getConfigurationName(final String subname) {
    return getClass().getName().replace('.', '/') + ".xml";
  }

  protected InputStream getResourceAsStream(final String resource) {
    return getClass().getResourceAsStream(resource);
  }

  protected ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  // ----------------------------------------------------------------------
  // Container access
  // ----------------------------------------------------------------------

  /**
   * @deprecated Avoid usage of Plexus apis (string role/hint lookup is plexus specific).
   */
  protected Object lookup(final String componentKey) throws Exception {
    return getContainer().lookup(componentKey);
  }

  /**
   * @deprecated Avoid usage of Plexus apis (string role/hint lookup is plexus specific).
   */
  @Deprecated
  protected Object lookup(final String role, final String roleHint) throws Exception {
    return getContainer().lookup(role, roleHint);
  }

  protected <T> T lookup(final Class<T> componentClass) throws Exception {
    return getContainer().lookup(componentClass);
  }

  protected <T> T lookup(final Class<T> componentClass, final String roleHint) throws Exception {
    return getContainer().lookup(componentClass, roleHint);
  }

  /**
   * @deprecated Avoid usage of Plexus apis.
   */
  protected void release(final Object component) throws Exception {
    getContainer().release(component);
  }

  // ----------------------------------------------------------------------
  // Helper methods for sub classes
  // ----------------------------------------------------------------------

  public File getTestFile(final String path) {
    return new File(getBasedir(), path);
  }

  public File getTestFile(final String basedir, final String path) {
    File basedirFile = new File(basedir);

    if (!basedirFile.isAbsolute()) {
      basedirFile = getTestFile(basedir);
    }

    return new File(basedirFile, path);
  }

  public String getBasedir() {
    return util.getBaseDir().getAbsolutePath();
  }

  /**
   * @deprecated Avoid usage of Plexus apis.
   */
  protected LoggerManager getLoggerManager() throws ComponentLookupException {
    LoggerManager loggerManager = getContainer().lookup(LoggerManager.class);
    // system property helps configure logger - see NXCM-3230
    String testLogLevel = System.getProperty("test.log.level");
    if (testLogLevel != null) {
      if (testLogLevel.equalsIgnoreCase("DEBUG")) {
        loggerManager.setThresholds(Logger.LEVEL_DEBUG);
      }
      else if (testLogLevel.equalsIgnoreCase("INFO")) {
        loggerManager.setThresholds(Logger.LEVEL_INFO);
      }
      else if (testLogLevel.equalsIgnoreCase("WARN")) {
        loggerManager.setThresholds(Logger.LEVEL_WARN);
      }
      else if (testLogLevel.equalsIgnoreCase("ERROR")) {
        loggerManager.setThresholds(Logger.LEVEL_ERROR);
      }
    }
    return loggerManager;
  }

  // ========================= CUSTOM NEXUS =====================

  /**
   * @deprecated Use {@link org.hamcrest.MatcherAssert} directly instead.
   */
  @Deprecated
  protected void assertEquals(String message, Object expected, Object actual) {
    // don't use junit framework Assert due to autoboxing bug
    MatcherAssert.assertThat(message, actual, Matchers.equalTo(expected));
  }

  /**
   * @deprecated Use {@link org.hamcrest.MatcherAssert} directly instead.
   */
  @Deprecated
  protected void assertEquals(Object expected, Object actual) {
    // don't use junit framework Assert
    MatcherAssert.assertThat(actual, Matchers.equalTo(expected));
  }

  protected boolean contentEquals(File f1, File f2) throws IOException {
    return contentEquals(new FileInputStream(f1), new FileInputStream(f2));
  }

  /**
   * Both s1 and s2 will be closed.
   */
  protected boolean contentEquals(InputStream s1, InputStream s2) throws IOException {
    try (InputStream in1 = s1;
         InputStream in2 = s2) {
      return IOUtils.contentEquals(in1, in2);
    }
  }

  public File getWorkHomeDir() {
    return workHomeDir;
  }

  public File getConfHomeDir() {
    return confHomeDir;
  }

  protected String getNexusConfiguration() {
    return new File(confHomeDir, "nexus.xml").getAbsolutePath();
  }

  protected void copyDefaultConfigToPlace() throws IOException {
    this.copyResource("/META-INF/nexus/default-oss-nexus.xml", getNexusConfiguration());
  }

  protected void copyResource(String resource, String dest) throws IOException {
    try (InputStream in = getClass().getResourceAsStream(resource);
         FileOutputStream out = new FileOutputStream(dest)) {
      IOUtils.copy(in, out);
    }
  }
}
