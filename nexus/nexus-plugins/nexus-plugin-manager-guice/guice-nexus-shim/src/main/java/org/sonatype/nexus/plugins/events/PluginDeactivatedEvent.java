/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.events;

import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginDescriptor;
import org.sonatype.plexus.appevents.AbstractEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * This {@link Event} is triggered when a Nexus plugin is de-activated.
 */
public final class PluginDeactivatedEvent
    extends AbstractEvent<NexusPluginManager>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PluginDescriptor descriptor;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PluginDeactivatedEvent( final NexusPluginManager component, final PluginDescriptor descriptor )
    {
        super( component );

        this.descriptor = descriptor;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PluginDescriptor getPluginDescriptor()
    {
        return descriptor;
    }

    public NexusPluginManager getNexusPluginManager()
    {
        return getEventSender();
    }
}
