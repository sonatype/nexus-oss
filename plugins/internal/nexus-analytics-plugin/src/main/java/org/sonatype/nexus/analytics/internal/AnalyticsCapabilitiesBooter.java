/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.analytics.internal;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityBooterSupport;
import org.sonatype.nexus.util.Tokens;
import org.sonatype.sisu.goodies.crypto.RandomBytesGenerator;

import com.google.common.collect.ImmutableMap;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Automatically creates bootstrap capabilities if needed on startup.
 *
 * @since 2.8
 */
@Named
@EagerSingleton
public class AnalyticsCapabilitiesBooter
    extends CapabilityBooterSupport
{
  private final RandomBytesGenerator randomBytesGenerator;

  @Inject
  public AnalyticsCapabilitiesBooter(final RandomBytesGenerator randomBytesGenerator) {
    this.randomBytesGenerator = checkNotNull(randomBytesGenerator);
  }

  @Override
  protected void boot(final CapabilityRegistry registry) throws Exception {
    // automatically add collection capability (disabled w/ random salt)
    maybeAddCapability(registry, CollectionCapabilityDescriptor.TYPE, false, null,
        ImmutableMap.of(
            CollectionCapabilityConfiguration.HOST_ID, randomHostId(),
            CollectionCapabilityConfiguration.SALT, randomSalt()
        )
    );

    // automatically add auto-submit capability (disabled)
    maybeAddCapability(registry, AutoSubmitCapabilityDescriptor.TYPE, false, null, null);
  }

  /**
   * Generate a new random host-id.
   */
  private String randomHostId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generate a new random salt.
   */
  private String randomSalt() {
    // 264 bit salt (256 would leave base64 padding)
    return Tokens.encodeBase64String(randomBytesGenerator.generate(33));
  }
}
