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
package org.sonatype.security.realms.kenai.internal.capability;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilityReference;
import org.sonatype.nexus.capability.CapabilityRegistry;
import org.sonatype.nexus.capability.support.CapabilityReferenceFilterBuilder;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;
import org.sonatype.security.realms.kenai.config.model.Configuration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link KenaiCapability} based {@link KenaiRealmConfiguration}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class KenaiRealmConfigurationImpl
    extends ComponentSupport
    implements KenaiRealmConfiguration
{

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public KenaiRealmConfigurationImpl(final CapabilityRegistry capabilityRegistry) {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
  }

  @Override
  public Configuration getConfiguration() {
    Collection<? extends CapabilityReference> refs = capabilityRegistry.get(
        CapabilityReferenceFilterBuilder.capabilities().withType(KenaiCapabilityDescriptor.TYPE)
    );
    if (refs.size() > 0) {
      return refs.iterator().next().capabilityAs(KenaiCapability.class).getConfig();
    }
    log.error("Kenai is not configured (Kenai capability not found)");
    throw new IllegalStateException("Kenai is not configured (Kenai capability not found)");
  }

}
