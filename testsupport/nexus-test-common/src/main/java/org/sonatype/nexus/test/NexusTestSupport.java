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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.bean.LifecycleModule;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
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

  private Module bootModule;

  private Injector testInjector;

  private Properties testProperties;

  private Properties sysPropsBackup;

  private File testHomeDir = null;

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

    // simply to make sure customizeProperties is handled before anything else
    getTestInjector();

    testHomeDir.mkdirs();
    appsHomeDir.mkdirs();
    workHomeDir.mkdirs();
    confHomeDir.mkdirs();
    baseHomeDir.mkdirs();
    nexusappHomeDir.mkdirs();
    tempDir.mkdirs();
  }

  protected void setupTestInjector() {
    testProperties = new Properties();
    testProperties.put("basedir", getBasedir());

    customizeProperties(testProperties);

    final boolean hasTestHome = testProperties.contains("test.home");
    if (!hasTestHome) {
      final File f = getTestFile("target/test-home");
      if (!f.isDirectory()) {
        f.mkdir();
      }

      testProperties.put("test.home", f.getAbsolutePath());
    }

    bootModule = new AbstractModule()
    {
      private final MutableBeanLocator beanLocator = new DefaultBeanLocator();

      private final LifecycleModule lifecycleModule = new LifecycleModule();

      @Override
      protected void configure() {
        bind(MutableBeanLocator.class).toInstance(beanLocator);
        bind(ParameterKeys.PROPERTIES).toInstance(getTestProperties());
        install(lifecycleModule);
      }
    };

    try {
      final List<Module> modules = Lists.newLinkedList();

      modules.add(bootModule());
      modules.add(spaceModule());
      customizeModules(modules);

      testInjector = Guice.createInjector(new WireModule(modules));
    }
    catch (final Exception e) {
      e.printStackTrace();
      Assert.fail("Failed to create test injector.");
    }
  }

  protected Module bootModule() {
    return bootModule;
  }

  protected Module spaceModule() {
    return new SpaceModule(new URLClassSpace(getClassLoader()), BeanScanning.INDEX);
  }

  protected void customizeModules(final List<Module> modules) {
    // empty
  }

  protected void customizeProperties(final Properties ctx) {
    testHomeDir = new File(
        getBasedir(), "target/test-home-" + new Random(System.currentTimeMillis()).nextLong()
    );
    appsHomeDir = new File(testHomeDir, "apps");
    workHomeDir = new File(testHomeDir, "nexus-work");
    confHomeDir = new File(workHomeDir, "etc");
    baseHomeDir = new File(testHomeDir, "nexus-base");
    nexusappHomeDir = new File(testHomeDir, "nexus-app");
    tempDir = new File(workHomeDir, "tmp");

    ctx.put(WORK_CONFIGURATION_KEY, workHomeDir.getAbsolutePath());
    ctx.put(APPS_CONFIGURATION_KEY, appsHomeDir.getAbsolutePath());
    ctx.put(CONF_DIR_KEY, confHomeDir.getAbsolutePath());
    ctx.put(NEXUS_BASE_CONFIGURATION_KEY, baseHomeDir.getAbsolutePath());
    ctx.put(NEXUS_APP_CONFIGURATION_KEY, nexusappHomeDir.getAbsolutePath());
    ctx.put("java.io.tmpdir", tempDir.getAbsolutePath());
  }

  protected Properties getTestProperties() {
    return testProperties;
  }

  protected Injector getTestInjector() {
    if (testInjector == null) {
      setupTestInjector();
    }
    return testInjector;
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
      if (testInjector != null) {
        testInjector.getInstance(BeanManager.class).unmanage();
      }
    }
    finally {
      bootModule = null;
      testInjector = null;

      System.setProperties(sysPropsBackup);
    }
    cleanDir(testHomeDir);
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

  protected InputStream getResourceAsStream(final String resource) {
    return getClass().getResourceAsStream(resource);
  }

  protected ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  // ----------------------------------------------------------------------
  // Container access
  // ----------------------------------------------------------------------

  public <T> T lookup(final Class<T> componentClass) {
    return getTestInjector().getInstance(BeanLocator.class).locate( //
        Key.get(componentClass)).iterator().next().getValue();
  }

  public <T> T lookup(final Class<T> componentClass, final String roleHint) {
    return getTestInjector().getInstance(BeanLocator.class).locate( //
        Key.get(componentClass, Names.named(roleHint))).iterator().next().getValue();
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
