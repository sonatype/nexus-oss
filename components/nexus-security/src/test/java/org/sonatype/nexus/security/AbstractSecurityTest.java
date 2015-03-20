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
import java.util.Arrays;
import java.util.Properties;

import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.nexus.security.realm.RealmConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestUtil;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.sisu.launch.InjectedTestCase;
import org.eclipse.sisu.space.BeanScanning;

public abstract class AbstractSecurityTest
    extends InjectedTestCase
{
  // FIXME: Convert to junit4

  protected final TestUtil util = new TestUtil(this);

  private File PLEXUS_HOME = util.resolveFile("target/plexus-home/");

  private File APP_CONF = new File(PLEXUS_HOME, "etc");

  @Override
  public void configure(Properties properties) {
    properties.put("application-conf", APP_CONF.getAbsolutePath());
    super.configure(properties);
  }

  @Override
  public void configure(final Binder binder) {
    binder.install(new TestSecurityModule());

    RealmConfiguration realmConfiguration = new RealmConfiguration();
    realmConfiguration.setRealmNames(Arrays.asList(
        "MockRealmA",
        "MockRealmB"
    ));
    binder.bind(RealmConfiguration.class)
        .annotatedWith(Names.named("initial"))
        .toInstance(realmConfiguration);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    DirSupport.deleteIfExists(PLEXUS_HOME.toPath());

    getSecuritySystem().start();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      getSecuritySystem().stop();
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

    ThreadContext.remove();
    super.tearDown();
  }

  @Override
  public BeanScanning scanning() {
    return BeanScanning.INDEX;
  }

  protected SecuritySystem getSecuritySystem() throws Exception {
    return this.lookup(SecuritySystem.class);
  }
}
