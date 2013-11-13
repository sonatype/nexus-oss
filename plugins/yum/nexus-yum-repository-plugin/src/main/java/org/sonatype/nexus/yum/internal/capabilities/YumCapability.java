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

package org.sonatype.nexus.yum.internal.capabilities;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.yum.YumRegistry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * YUM capability.
 *
 * @since yum 3.0
 */
@Named(YumCapabilityDescriptor.TYPE_ID)
public class YumCapability
    extends CapabilitySupport<YumCapabilityConfiguration>
{
  private final YumRegistry yumRegistry;

  @Inject
  public YumCapability(final YumRegistry yumRegistry) {
    this.yumRegistry = checkNotNull(yumRegistry);
  }

  @Override
  protected YumCapabilityConfiguration createConfig(final Map<String, String> properties) throws Exception {
    return new YumCapabilityConfiguration(properties);
  }

  @Override
  protected void onActivate(final YumCapabilityConfiguration config) throws Exception {
    yumRegistry.setMaxNumberOfParallelThreads(config.maxNumberParallelThreads());
  }
}
