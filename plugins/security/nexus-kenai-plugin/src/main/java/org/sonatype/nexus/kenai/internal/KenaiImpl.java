/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilityReference;
import org.sonatype.nexus.capability.CapabilityReferenceFilterBuilder;
import org.sonatype.nexus.capability.CapabilityRegistry;
import org.sonatype.nexus.kenai.Kenai;
import org.sonatype.nexus.kenai.KenaiConfiguration;
import org.sonatype.nexus.security.realm.RealmManager;
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
  private final RealmManager realmManager;

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public KenaiImpl(final RealmManager realmManager,
                   final CapabilityRegistry capabilityRegistry)
  {
    this.realmManager = checkNotNull(realmManager);
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
  }

  @Override
  public Kenai configure(final KenaiConfiguration config) throws IOException {
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
    return realmManager.isRealmEnabled(KenaiRealm.ROLE);
  }

  @Override
  public void setRealmActive(boolean realmActive) {
    log.debug("Realm active: {}", realmActive);

    realmManager.enableRealm(KenaiRealm.ROLE, realmActive);
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
