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

import static java.lang.String.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;

/**
 * Default {@link CapabilityRegistry} implementation.
 */
@Singleton
@Named
class DefaultCapabilityRegistry
    implements CapabilityRegistry
{

    // TODO temporary. To be replaced when new container inplace
    @Requirement( role = CapabilityFactory.class )
    private Map<String, CapabilityFactory> factories;

    private final Map<String, CapabilityReference> capabilities;

    DefaultCapabilityRegistry()
    {
        capabilities = new HashMap<String, CapabilityReference>();
    }

    public CapabilityReference get( final String capabilityId )
    {
        return capabilities.get( capabilityId );
    }

    @Override
    public Collection<CapabilityReference> getAll()
    {
        return capabilities.values();
    }

    public CapabilityReference create( final String capabilityId, final String capabilityType )
    {
        assert capabilityId != null : "Capability id cannot be null";

        final CapabilityFactory factory = factories.get( capabilityType );
        if ( factory == null )
        {
            throw new RuntimeException( format( "No factory found for a capability of type %s", capabilityType ) );
        }

        final Capability capability = factory.create( capabilityId );

        return new DefaultCapabilityReference( capability );
    }

    public CapabilityReference remove( final String capabilityId )
    {
        return capabilities.remove( capabilityId );
    }

}
