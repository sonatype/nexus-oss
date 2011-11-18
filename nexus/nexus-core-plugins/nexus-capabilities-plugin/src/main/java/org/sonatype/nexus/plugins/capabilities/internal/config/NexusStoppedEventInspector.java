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
package org.sonatype.nexus.plugins.capabilities.internal.config;

import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.Event;

@Singleton
public class NexusStoppedEventInspector
    extends AbstractLoggingComponent
    implements EventInspector
{

    private final CapabilityRegistry registry;

    @Inject
    public NexusStoppedEventInspector( final CapabilityRegistry registry )
    {
        this.registry = registry;
    }

    public boolean accepts( final Event<?> evt )
    {
        return evt != null
            && evt instanceof NexusStoppedEvent;
    }

    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }
        for ( final Capability capability : new ArrayList<Capability>( registry.getAll() ) )
        {
            if ( capability instanceof Capability.LifeCycle )
            {
                try
                {
                    ( (Capability.LifeCycle) capability ).passivate();
                }
                catch ( Exception e )
                {
                    getLogger().error(
                        "Could not passivate capability with id '{}' ({})",
                        new Object[]{ capability.id(), capability, e }
                    );
                }
            }
        }
    }

}
