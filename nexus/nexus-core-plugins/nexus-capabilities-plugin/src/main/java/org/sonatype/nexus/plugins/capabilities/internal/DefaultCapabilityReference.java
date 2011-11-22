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

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;

/**
 * Default {@link CapabilityReference} implementation.
 *
 * @since 1.10.0
 */
class DefaultCapabilityReference
    extends AbstractLoggingComponent
    implements CapabilityReference
{

    private final Capability capability;

    private boolean active;

    DefaultCapabilityReference( final Capability capability )
    {
        this.capability = checkNotNull( capability );
    }

    @Override
    public Capability capability()
    {
        return capability;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public void activate()
    {
        if ( !isActive() )
        {
            try
            {
                capability().activate();
                active = true;
            }
            catch ( Exception e )
            {
                getLogger().error(
                    "Could not activate capability with id '{}' ({})", new Object[]{ capability.id(), capability, e }
                );
            }
        }
    }

    @Override
    public void passivate()
    {
        if ( isActive() )
        {
            try
            {
                active = false;
                capability().passivate();
            }
            catch ( Exception e )
            {
                getLogger().error(
                    "Could not passivate capability with id '{}' ({})", new Object[]{ capability.id(), capability, e }
                );
            }
        }
    }
}
