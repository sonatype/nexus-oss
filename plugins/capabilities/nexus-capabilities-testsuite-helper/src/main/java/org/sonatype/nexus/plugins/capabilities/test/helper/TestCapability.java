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

package org.sonatype.nexus.plugins.capabilities.test.helper;

import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;

public abstract class TestCapability
    extends CapabilitySupport
    implements Capability
{

  @Override
  public void onCreate() {
    getLogger().info("Create capability with id {} and properties {}", context().id(), context().properties());
  }

  @Override
  public void onUpdate() {
    getLogger().info("Update capability with id {} and properties {}", context().id(), context().properties());
  }

  @Override
  public void onLoad() {
    getLogger().info("Load capability with id {} and properties {}", context().id(), context().properties());
  }

  @Override
  public void onRemove() {
    getLogger().info("Remove capability with id {}", context().id());
  }

  @Override
  public String status() {
    return "<h3>I'm well. Thanx!</h3>";
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}
