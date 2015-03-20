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
package org.sonatype.nexus.security;

import java.io.File;
import java.util.Properties;

import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.nexus.security.config.PreconfiguredSecurityConfigurationSource;
import org.sonatype.nexus.security.config.SecurityConfiguration;
import org.sonatype.nexus.security.config.SecurityConfigurationSource;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.sisu.litmus.testsupport.TestUtil;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import net.sf.ehcache.CacheManager;
import org.eclipse.sisu.launch.InjectedTestCase;
import org.eclipse.sisu.space.BeanScanning;

public abstract class AbstractSecurityTestCase
    extends InjectedTestCase
{
  // FIXME: Convert to junit4

  protected final TestUtil util = new TestUtil(this);

  private File PLEXUS_HOME = util.resolveFile("target/plexus_home");

  private File CONFIG_DIR = new File(PLEXUS_HOME, "etc");

  @Override
  public void configure(Properties properties) {
    properties.put("application-conf", CONFIG_DIR.getAbsolutePath());
    super.configure(properties);
  }

  @Override
  public void configure(final Binder binder) {
    binder.install(new TestSecurityModule());

    binder.bind(SecurityConfigurationSource.class)
        .annotatedWith(Names.named("default"))
        .toInstance(new PreconfiguredSecurityConfigurationSource(getSecurityModelConfig()));
  }

  protected SecurityConfiguration getSecurityModelConfig() {
    return AbstractSecurityTestCaseSecurity.securityModel();
  }

  @Override
  public BeanScanning scanning() {
    return BeanScanning.INDEX;
  }

  @Override
  protected void setUp() throws Exception {
    DirSupport.deleteIfExists(PLEXUS_HOME.toPath());
    super.setUp();
    CONFIG_DIR.mkdirs();
    lookup(SecuritySystem.class).start();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      lookup(SecuritySystem.class).stop();
    }
    catch (Exception e) {
      util.getLog().warn("Failed to stop security-system", e);
    }

    try {
      lookup(CacheManager.class).shutdown();
    }
    catch (Exception e) {
      util.getLog().warn("Failed to shutdown cache-manager", e);
    }

    super.tearDown();
  }

  protected SecuritySystem getSecuritySystem() {
    return lookup(SecuritySystem.class);
  }

  protected UserManager getUserManager() {
    return lookup(UserManager.class);
  }

  protected SecurityConfiguration getSecurityConfiguration() {
    return lookup(SecurityConfigurationSource.class, "default").getConfiguration();
  }
}
