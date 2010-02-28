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
import org.sonatype.plexus.appevents.AbstractEvent;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * This {@link Event} is triggered when a Nexus plugin fails during activation.
 */
public final class PluginRejectedEvent
    extends AbstractEvent<NexusPluginManager>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final GAVCoordinate gav;

    private final Throwable reason;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PluginRejectedEvent( final NexusPluginManager component, final GAVCoordinate gav, final Throwable reason )
    {
        super( component );

        this.gav = gav;
        this.reason = reason;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public GAVCoordinate getPluginCoordinates()
    {
        return gav;
    }

    public Throwable getReason()
    {
        return reason;
    }

    public NexusPluginManager getNexusPluginManager()
    {
        return getEventSender();
    }
}