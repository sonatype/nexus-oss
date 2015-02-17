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
package org.sonatype.nexus.kenai.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.capability.CapabilityReference;
import org.sonatype.nexus.capability.CapabilityRegistry;
import org.sonatype.nexus.capability.support.CapabilityReferenceFilterBuilder;
import org.sonatype.nexus.kenai.Kenai;
import org.sonatype.nexus.kenai.KenaiConfiguration;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link Kenai} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class KenaiImpl
    extends ComponentSupport
    implements Kenai
{

  private final CapabilityRegistry capabilityRegistry;

  private final SecuritySystem securitySystem;

  @Inject
  public KenaiImpl(final SecuritySystem securitySystem,
                   final CapabilityRegistry capabilityRegistry)
  {
    this.securitySystem = checkNotNull(securitySystem);
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
  }

  @Override
  public Kenai configure(final KenaiConfiguration config) throws InvalidConfigurationException, IOException {
    CapabilityReference ref = capabilityReference();
    if (ref != null) {
      capabilityRegistry.update(
          ref.context().id(),
          ref.context().isEnabled(),
          ref.context().notes(),
          config.asMap()
      );
    }
    else {
      capabilityRegistry.add(
          KenaiCapabilityDescriptor.TYPE,
          true,
          null,
          config.asMap()
      );
    }
    return this;
  }

  @Override
  public KenaiConfiguration getConfiguration() throws IOException {
    CapabilityReference ref = capabilityReference();
    if (ref != null) {
      return ref.capabilityAs(KenaiCapability.class).getConfig();
    }
    return null;
  }

  @Override
  public boolean isEnabled() {
    final CapabilityReference ref = capabilityReference();
    return ref != null && ref.context().isActive();
  }

  @Override
  public void setEnabled(final boolean enable) {
    log.debug("Enabled: {}", enable);

    try {
      final CapabilityReference ref = capabilityReference();
      checkState(ref != null, "Crowd capability was not yet configured");
      if (enable) {
        capabilityRegistry.enable(ref.context().id());
      }
      else {
        capabilityRegistry.disable(ref.context().id());
      }
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public boolean isRealmActive() {
    return securitySystem.getRealms().contains(KenaiRealm.ROLE);
  }

  @Override
  public void setRealmActive(boolean realmActive) {
    log.debug("Realm active: {}", realmActive);

    List<String> realms = securitySystem.getRealms();
    boolean currentlyActive = realms.contains(KenaiRealm.ROLE);

    try {
      if (realmActive && !currentlyActive) {
        realms.add(KenaiRealm.ROLE);
        log.info("Activating Kenai security realm");
        securitySystem.setRealms(realms);
      }
      else if (!realmActive && currentlyActive) {
        realms.remove(KenaiRealm.ROLE);
        log.info("Deactivating Kenai security realm");
        securitySystem.setRealms(realms);
      }
    }
    catch (InvalidConfigurationException e) {
      throw Throwables.propagate(e);
    }
  }

  private CapabilityReference capabilityReference() {
    Collection<? extends CapabilityReference> refs = capabilityRegistry.get(
        CapabilityReferenceFilterBuilder.capabilities().withType(KenaiCapabilityDescriptor.TYPE));
    if (refs.isEmpty()) {
      return null;
    }
    return refs.iterator().next();
  }

}
