/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;

public class CapabilityStorageItem
{
    private final int version;

    private final CapabilityIdentity id;

    private final CapabilityType type;

    private final boolean enabled;

    private final String notes;

    private final Map<String, String> properties;

    public CapabilityStorageItem( final int version,
                                  final CapabilityIdentity id,
                                  final CapabilityType type,
                                  final boolean enabled,
                                  final String notes,
                                  final Map<String, String> properties )
    {
        this.version = version;
        this.id = checkNotNull( id );
        this.type = checkNotNull( type );
        this.enabled = enabled;
        this.notes = notes;
        this.properties = properties;
    }

    public int version()
    {
        return version;
    }

    public CapabilityIdentity id()
    {
        return id;
    }

    public CapabilityType type()
    {
        return type;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String notes()
    {
        return notes;
    }

    public Map<String, String> properties()
    {
        return properties;
    }

}
