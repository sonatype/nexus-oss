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

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.security.config.PreconfiguredSecurityConfigurationSource;
import org.sonatype.nexus.security.config.SecurityConfiguration;
import org.sonatype.nexus.security.config.SecurityConfigurationSource;
import org.sonatype.nexus.security.realm.RealmConfiguration;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.test.NexusTestSupport;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.util.ThreadContext;

public abstract class AbstractSecurityTest
    extends NexusTestSupport
    implements Module
{
  @Override
  protected void customizeModules(List<Module> modules) {
    super.customizeModules(modules);
    modules.add(new TestSecurityModule());
    modules.add(this);
  }

  @Override
  public void configure(final Binder binder) {
    binder.bind(SecurityConfigurationSource.class).annotatedWith(Names.named("default"))
        .toInstance(new PreconfiguredSecurityConfigurationSource(initialSecurityConfiguration()));

    RealmConfiguration realmConfiguration = new RealmConfiguration();
    realmConfiguration.setRealmNames(Arrays.asList("MockRealmA", "MockRealmB"));
    binder.bind(RealmConfiguration.class).annotatedWith(Names.named("initial")).toInstance(realmConfiguration);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    lookup(Injector.class).injectMembers(this);

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

  protected SecuritySystem getSecuritySystem() {
    return lookup(SecuritySystem.class);
  }

  protected UserManager getUserManager() {
    return lookup(UserManager.class);
  }

  protected SecurityConfiguration initialSecurityConfiguration() {
    return BaseSecurityConfig.get();
  }

  protected final SecurityConfiguration getSecurityConfiguration() {
    return lookup(SecurityConfigurationSource.class, "default").getConfiguration();
  }
}
