/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.events;

import java.util.Date;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "ConfigurationChangeEvent" )
public class ConfigurationChangeEventInspector
    extends AbstractEventInspector
{
    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof ConfigurationChangeEvent )
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        inspectForNexus( evt );

        inspectForIndexerManager( evt );
    }

    private void inspectForNexus( AbstractEvent evt )
    {
        getNexus().getSystemStatus().setLastConfigChange( new Date() );

        getNexus().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, "Nexus configuration changed/updated." );

    }

    private void inspectForIndexerManager( AbstractEvent evt )
    {
        getIndexerManager().resetConfiguration();
    }

}
