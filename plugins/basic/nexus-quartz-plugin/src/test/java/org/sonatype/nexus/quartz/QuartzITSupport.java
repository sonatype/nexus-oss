/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.quartz;

import java.io.File;
import java.util.Properties;

import javax.inject.Inject;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.quartz.internal.QuartzSupportImpl;
import org.sonatype.nexus.scheduling.NexusTaskScheduler;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * IT support: this beast brings up real SISU container and complete Quartz environment.
 */
public abstract class QuartzITSupport
    extends TestSupport
    implements Module
{
  @Inject
  protected Injector injector;

  @Inject
  protected MutableBeanLocator locator;

  @Inject
  protected NexusTaskScheduler nexusTaskScheduler;

  @Inject
  protected QuartzSupportImpl quartzSupport;


  // ==

  @Mock
  protected ApplicationDirectories applicationDirectories;

  @Mock
  protected ApplicationConfiguration applicationConfiguration;

  @Before
  public void prepare() throws Exception {
    final Module testModule = new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(ApplicationDirectories.class).toInstance(applicationDirectories);
      }
    };
    Guice.createInjector(new WireModule(new SetUpModule(), spaceModule()));

    quartzSupport.start();
  }

  @After
  public void tearDown()
      throws Exception
  {
    quartzSupport.stop();
    locator.clear();
  }

  final class SetUpModule
      implements Module
  {
    public void configure(final Binder binder)
    {
      binder.install(QuartzITSupport.this);

      final Properties properties = new Properties();
      properties.put("basedir", util.getBaseDir());

      QuartzITSupport.this.configure(properties);

      final File workDir = util.createTempDir(util.getTargetDir(), "workdir");
      log("Workdir: {}", workDir);
      when(applicationDirectories.getWorkDirectory(anyString())).thenReturn(workDir);
      binder.bind(ApplicationDirectories.class).toInstance(applicationDirectories);

      when(applicationConfiguration.getConfigurationDirectory()).thenReturn(new File(workDir, "conf"));
      binder.bind(ApplicationConfiguration.class).toInstance(applicationConfiguration);

      binder.bind(ParameterKeys.PROPERTIES).toInstance(properties);

      binder.requestInjection(QuartzITSupport.this);
    }
  }

  public SpaceModule spaceModule()
  {
    return new SpaceModule(space(), scanning());
  }

  public ClassSpace space()
  {
    return new URLClassSpace(getClass().getClassLoader());
  }

  public BeanScanning scanning()
  {
    return BeanScanning.CACHE;
  }

  /**
   * Custom injection bindings.
   *
   * @param binder The Guice binder
   */
  @Override
  public void configure(final Binder binder)
  {
    // place any per-test bindings here...
  }

  /**
   * Custom property values.
   *
   * @param properties The test properties
   */
  public void configure(final Properties properties)
  {
    // put any per-test properties here...
  }
}
