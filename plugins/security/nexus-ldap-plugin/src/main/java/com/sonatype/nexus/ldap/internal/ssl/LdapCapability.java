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

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.sonatype.nexus.ldap.internal.capabilities.LdapConditions;
import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;

import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ldap.model.LdapTrustStoreKey.ldapTrustStoreKey;

/**
 * LDAP capability (enables Nexus SSL Trust Store / LDAP server).
 *
 * @since 2.4
 */
@Named(LdapCapabilityDescriptor.TYPE_ID)
public class LdapCapability
    extends CapabilitySupport<LdapCapabilityConfiguration>
{

  private final TrustStore trustStore;

  private final LdapConfigurationManager ldapConfigurationManager;

  private final LdapConditions ldapConditions;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("%s")
    String description(String description);
  }

  private static final Messages messages = I18N.create(Messages.class);

  @Inject
  public LdapCapability(final TrustStore trustStore,
                        final LdapConditions ldapConditions,
                        final @Nullable LdapConfigurationManager ldapConfigurationManager)
  {
    this.trustStore = checkNotNull(trustStore);
    this.ldapConditions = checkNotNull(ldapConditions);
    this.ldapConfigurationManager = ldapConfigurationManager;
  }

  @Override
  protected LdapCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new LdapCapabilityConfiguration(properties);
  }

  @Override
  protected void onActivate(final LdapCapabilityConfiguration config) {
    trustStore.enableFor(ldapTrustStoreKey(config.getLdapServerId()));
  }

  @Override
  protected void onPassivate(final LdapCapabilityConfiguration config) {
    trustStore.disableFor(ldapTrustStoreKey(config.getLdapServerId()));
  }

  @Override
  public Condition activationCondition() {
    return conditions().logical().and(
        conditions().nexus().active(),
        conditions().capabilities().passivateCapabilityDuringUpdate()
    );
  }

  @Override
  public Condition validityCondition() {
    return ldapConditions.ldapServerExists(new LdapConditions.LdapServerId()
    {
      @Override
      public String get() {
        return isConfigured() ? getConfig().getLdapServerId() : null;
      }
    });
  }

  @Override
  protected String renderDescription() {
    if (ldapConfigurationManager != null) {
      try {
        return messages.description(
            ldapConfigurationManager.getLdapServerConfiguration(getConfig().getLdapServerId()).getName()
        );
      }
      catch (Exception ignore) {
        // not a problem that we cannot get LDAP server name
      }
    }
    return messages.description(getConfig().getLdapServerId());
  }

}
