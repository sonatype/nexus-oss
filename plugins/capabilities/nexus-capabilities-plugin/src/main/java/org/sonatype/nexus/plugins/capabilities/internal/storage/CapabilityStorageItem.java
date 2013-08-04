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

package org.sonatype.nexus.plugins.capabilities.internal.storage;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;

import static com.google.common.base.Preconditions.checkNotNull;

public class CapabilityStorageItem
{
  private final int version;

  private final CapabilityIdentity id;

  private final CapabilityType type;

  private final boolean enabled;

  private final String notes;

  private final Map<String, String> properties;

  public CapabilityStorageItem(final int version,
                               final CapabilityIdentity id,
                               final CapabilityType type,
                               final boolean enabled,
                               final String notes,
                               final Map<String, String> properties)
  {
    this.version = version;
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.enabled = enabled;
    this.notes = notes;
    this.properties = properties;
  }

  public int version() {
    return version;
  }

  public CapabilityIdentity id() {
    return id;
  }

  public CapabilityType type() {
    return type;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String notes() {
    return notes;
  }

  public Map<String, String> properties() {
    return properties;
  }

}
