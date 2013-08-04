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

package org.sonatype.nexus.plugins.capabilities.support;

import java.util.ArrayList;
import java.util.Collection;

import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;

public class CompositeCapability
    extends CapabilitySupport
{

  private final Collection<Capability> capabilities;

  public CompositeCapability() {
    capabilities = new ArrayList<Capability>();
  }

  public void add(final Capability capability) {
    capabilities.add(capability);
  }

  public void remove(final Capability capability) {
    capabilities.remove(capability);
  }

  @Override
  public void init(final CapabilityContext context) {
    super.init(context);
    for (final Capability capability : capabilities) {
      capability.init(context);
    }
  }

  @Override
  public void onCreate()
      throws Exception
  {
    for (final Capability capability : capabilities) {
      capability.onCreate();
    }
  }

  @Override
  public void onLoad()
      throws Exception
  {
    for (final Capability capability : capabilities) {
      capability.onLoad();
    }
  }

  @Override
  public void onUpdate()
      throws Exception
  {
    for (final Capability capability : capabilities) {
      capability.onUpdate();
    }
  }

  @Override
  public void onRemove()
      throws Exception
  {
    for (final Capability capability : capabilities) {
      capability.onRemove();
    }
  }

  @Override
  public void onActivate()
      throws Exception
  {
    for (final Capability capability : capabilities) {
      capability.onActivate();
    }
  }

  @Override
  public void onPassivate()
      throws Exception
  {
    for (final Capability capability : capabilities) {
      capability.onPassivate();
    }
  }

}