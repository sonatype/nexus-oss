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
package com.sonatype.nexus.ldap.internal.ssl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.ssl.plugin.spi.CapabilityManager;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;

import org.sonatype.nexus.capability.CapabilityReference;
import org.sonatype.nexus.capability.CapabilityRegistry;
import org.sonatype.nexus.capability.support.CapabilityReferenceFilterBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.capability.support.CapabilityReferenceFilterBuilder.capabilities;

/**
 * {@link LdapCapability} manager.
 *
 * @since 2.4
 */
@Named("ldap")
@Singleton
public class LdapCapabilityManager
    extends ComponentSupport
    implements CapabilityManager
{

  private final CapabilityRegistry capabilityRegistry;

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapCapabilityManager(final CapabilityRegistry capabilityRegistry,
                               final LdapConfigurationManager ldapConfigurationManager)
  {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
  }

  @Override
  public CapabilityReference get(final String id) {
    final CapabilityReferenceFilterBuilder.CapabilityReferenceFilter filter = capabilities()
        .withType(LdapCapabilityDescriptor.TYPE)
        .withProperty(LdapCapabilityConfiguration.LDAP_SERVER_ID, id);

    final Collection<? extends CapabilityReference> capabilities = capabilityRegistry.get(filter);
    if (capabilities != null && !capabilities.isEmpty()) {
      return capabilities.iterator().next();
    }
    return null;
  }

  @Override
  public CapabilityReference enable(final String id, final boolean enabled)
      throws Exception
  {
    CapabilityReference reference = get(id);
    if (reference == null) {
      reference = capabilityRegistry.add(
          LdapCapabilityDescriptor.TYPE,
          enabled,
          null,
          new LdapCapabilityConfiguration().withLdapServerId(id).asMap()
      );
    }
    else {
      reference = capabilityRegistry.update(
          reference.context().id(),
          enabled,
          reference.context().notes(),
          reference.context().properties()
      );
    }

    try {
      ldapConfigurationManager.clearCache();
    }
    catch (Exception e) {
      log.warn("Failed to clear LDAP cache", e);
    }

    return reference;
  }

}
