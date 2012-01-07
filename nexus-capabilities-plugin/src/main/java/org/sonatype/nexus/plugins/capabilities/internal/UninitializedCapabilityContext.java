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
package org.sonatype.nexus.plugins.capabilities.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;

/**
 * A {@link CapabilityContext} that is not yet initialized.
 *
 * @since 2.0
 */
class UninitializedCapabilityContext
    implements CapabilityContext
{

    private final CapabilityIdentity id;

    private final CapabilityType type;

    private final CapabilityDescriptor descriptor;

    UninitializedCapabilityContext( final CapabilityIdentity id,
                                    final CapabilityType type,
                                    final CapabilityDescriptor descriptor )
    {
        this.id = checkNotNull( id );
        this.type = checkNotNull( type );
        this.descriptor = checkNotNull( descriptor );
    }

    @Override
    public CapabilityIdentity id()
    {
        return id;
    }

    @Override
    public CapabilityType type()
    {
        return type;
    }

    @Override
    public CapabilityDescriptor descriptor()
    {
        return descriptor;
    }

    @Override
    public String notes()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public Map<String, String> properties()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public boolean isEnabled()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public boolean isActive()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public boolean hasFailure()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public Exception failure()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public String stateDescription()
    {
        throw new IllegalStateException( "Capability context is not yet initialized" );
    }

    @Override
    public String toString()
    {
        return "Uninitialized";
    }

}
