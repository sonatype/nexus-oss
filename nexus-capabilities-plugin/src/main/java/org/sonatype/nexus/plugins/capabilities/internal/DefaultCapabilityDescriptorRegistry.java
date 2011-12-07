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

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;

@Named
@Singleton
class DefaultCapabilityDescriptorRegistry
    implements CapabilityDescriptorRegistry
{

    private final List<CapabilityDescriptor> descriptors;

    @Inject
    DefaultCapabilityDescriptorRegistry( final List<CapabilityDescriptor> descriptors )
    {
        this.descriptors = checkNotNull( descriptors );
    }

    public CapabilityDescriptor get( final String capabilityDescriptorId )
    {
        for ( final CapabilityDescriptor descriptor : descriptors )
        {
            if ( descriptor.id().equals( capabilityDescriptorId ) )
            {
                return descriptor;
            }
        }
        return null;
    }

    public CapabilityDescriptor[] getAll()
    {
        return descriptors.toArray( new CapabilityDescriptor[descriptors.size()] );
    }
}
