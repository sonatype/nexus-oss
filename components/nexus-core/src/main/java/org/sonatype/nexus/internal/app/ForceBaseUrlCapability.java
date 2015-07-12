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
package org.sonatype.nexus.internal.app;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.capability.CapabilitySupport;
import org.sonatype.nexus.common.app.BaseUrlManager;
import org.sonatype.nexus.common.app.SystemState;
import org.sonatype.nexus.common.app.SystemStatus;

import static com.google.common.base.Preconditions.checkNotNull;

// NOTE: Could simplify this to be a checkbox on the baseurl capability, but need to find a way to let folks know this is edge-case only

/**
 * Force Base-URL capability.
 *
 * @since 3.0
 */
@Named(ForceBaseUrlCapabilityDescriptor.TYPE_ID)
public class ForceBaseUrlCapability
    extends CapabilitySupport<ForceBaseUrlCapabilityConfiguration>
{
  private final BaseUrlManager baseUrlManager;

  private final Provider<SystemStatus> systemStatusProvider;

  @Inject
  public ForceBaseUrlCapability(final BaseUrlManager baseUrlManager,
                                final Provider<SystemStatus> systemStatusProvider)
  {
    this.baseUrlManager = checkNotNull(baseUrlManager);
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
  }

  @Override
  protected ForceBaseUrlCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new ForceBaseUrlCapabilityConfiguration();
  }

  @Override
  protected void onActivate(final ForceBaseUrlCapabilityConfiguration config) throws Exception {
    if (!baseUrlManager.isForce()) {
      baseUrlManager.setForce(true);
    }
  }

  @Override
  protected void onPassivate(final ForceBaseUrlCapabilityConfiguration config) throws Exception {
    if (SystemState.STOPPING != systemStatusProvider.get().getState()) {
      baseUrlManager.setForce(false);
    }
  }
}
