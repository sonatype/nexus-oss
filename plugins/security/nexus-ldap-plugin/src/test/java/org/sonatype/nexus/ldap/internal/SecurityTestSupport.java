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
package org.sonatype.nexus.ldap.internal;

import java.util.List;

import org.sonatype.nexus.security.config.MemorySecurityConfiguration;
import org.sonatype.nexus.security.config.PreconfiguredSecurityConfigurationSource;
import org.sonatype.nexus.security.config.SecurityConfigurationSource;
import org.sonatype.nexus.security.settings.PreconfiguredSecuritySettingsSource;
import org.sonatype.nexus.security.settings.SecuritySettings;
import org.sonatype.nexus.security.settings.SecuritySettingsSource;
import org.sonatype.nexus.test.NexusTestSupport;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

public abstract class SecurityTestSupport
    extends NexusTestSupport
{

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);
    final SecuritySettings securityConfig = getSecurityConfig();
    if (securityConfig != null) {
      modules.add(new AbstractModule()
      {
        @Override
        protected void configure() {
          bind(SecuritySettingsSource.class)
              .annotatedWith(Names.named("default"))
              .toInstance(new PreconfiguredSecuritySettingsSource(securityConfig));
        }
      });
    }
    final MemorySecurityConfiguration securityModelConfig = getSecurityModelConfig();
    if (securityModelConfig != null) {
      modules.add(new AbstractModule()
      {
        @Override
        protected void configure() {
          bind(SecurityConfigurationSource.class)
              .annotatedWith(Names.named("default"))
              .toInstance(new PreconfiguredSecurityConfigurationSource(securityModelConfig));
        }
      });
    }
  }

  protected SecuritySettings getSecurityConfig() {
    return null;
  }

  protected MemorySecurityConfiguration getSecurityModelConfig() {
    return null;
  }

}
