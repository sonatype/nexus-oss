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

package org.sonatype.nexus.rapture.internal.capability;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.rapture.Rapture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Rapture Settings capability.
 *
 * @since 2.8
 */
@Named(RaptureSettingsCapabilityDescriptor.TYPE_ID)
public class RaptureSettingsCapability
    extends CapabilitySupport<RaptureSettingsCapabilityConfiguration>
{

  private final Rapture rapture;

  @Inject
  public RaptureSettingsCapability(final Rapture rapture) {
    this.rapture = checkNotNull(rapture, "rapture");
  }

  @Override
  protected RaptureSettingsCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new RaptureSettingsCapabilityConfiguration(properties);
  }

  @Override
  protected void onActivate(final RaptureSettingsCapabilityConfiguration config) throws Exception {
    rapture.setSettings(config);
  }

  @Override
  protected void onPassivate(final RaptureSettingsCapabilityConfiguration config) throws Exception {
    rapture.resetSettings();
  }

  @Override
  protected void onRemove(final RaptureSettingsCapabilityConfiguration config) throws Exception {
    rapture.resetSettings();
  }

  @Override
  public Condition activationCondition() {
    return conditions().capabilities().passivateCapabilityDuringUpdate();
  }

}
