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
package com.sonatype.nexus.ssl.plugin.internal.smtp;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.capability.support.WithoutConfiguration;
import org.sonatype.nexus.plugins.capabilities.Condition;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ssl.model.SMTPTrustStoreKey.smtpTrustStoreKey;
import static org.sonatype.nexus.capability.support.WithoutConfiguration.WITHOUT_CONFIGURATION;

/**
 * SMTP capability (enables Nexus SSL Trust Store / SMTP).
 *
 * @since ssl 1.0
 */
@Named(SMTPCapabilityDescriptor.TYPE_ID)
public class SMTPCapability
    extends CapabilitySupport<WithoutConfiguration>
{

  private final TrustStore trustStore;

  @Inject
  public SMTPCapability(final TrustStore trustStore)
  {
    this.trustStore = checkNotNull(trustStore);
  }

  @Override
  protected WithoutConfiguration createConfig(final Map<String, String> properties) {
    return WITHOUT_CONFIGURATION;
  }

  @Override
  protected void onActivate(final WithoutConfiguration config) {
    trustStore.enableFor(smtpTrustStoreKey());
  }

  @Override
  protected void onPassivate(final WithoutConfiguration config) {
    trustStore.disableFor(smtpTrustStoreKey());
  }

  @Override
  public Condition activationCondition() {
    return conditions().logical().and(
        conditions().nexus().active(),
        conditions().capabilities().passivateCapabilityDuringUpdate()
    );
  }

}
