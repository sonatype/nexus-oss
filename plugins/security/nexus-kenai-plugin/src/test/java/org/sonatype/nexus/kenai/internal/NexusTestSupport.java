/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.kenai.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.sisu.litmus.testsupport.TestUtil;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

// HACK: Duplicated to isolate to few places where used, pending removal

/**
 * Abstract test case for nexus tests.
 *
 * @deprecated UT should not use injection, unless in very rare cases.  This class will be removed in near future.
 */
@Deprecated
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
  public final void setUpJunit() throws Exception {
    setUp();
  }

  /**
   * Helper to call old JUnit 3x style {@link #tearDown()}
   */
  @After
  public final void tearDownJunit() throws Exception {
    tearDown();
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

  private void setupTestInjector() {
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
      customizeModules(modules);
      modules.add(spaceModule());

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

  private Properties getTestProperties() {
    return testProperties;
  }

  private Injector getTestInjector() {
    if (testInjector == null) {
      setupTestInjector();
    }
    return testInjector;
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

  private void cleanDir(File dir) {
    if (dir != null) {
      try {
        DirSupport.delete(dir.toPath());
      }
      catch (IOException e) {
        // couldn't delete directory, too bad
      }
    }
  }

  private ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  // ----------------------------------------------------------------------
  // Container access
  // ----------------------------------------------------------------------

  public <T> T lookup(final Class<T> componentClass) {
    return getTestInjector().getInstance(BeanLocator.class).locate(
        Key.get(componentClass)).iterator().next().getValue();
  }

  public <T> T lookup(final Class<T> componentClass, final String roleHint) {
    return getTestInjector().getInstance(BeanLocator.class).locate(
        Key.get(componentClass, Names.named(roleHint))).iterator().next().getValue();
  }

  private File getTestFile(final String path) {
    return new File(getBasedir(), path);
  }

  private String getBasedir() {
    return util.getBaseDir().getAbsolutePath();
  }
}
