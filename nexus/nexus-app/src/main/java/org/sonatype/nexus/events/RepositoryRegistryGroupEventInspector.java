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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventRemove;

/**
 * 
 * @author Juven Xu
 *
 */
@Component( role = EventInspector.class, hint = "RepositoryRegistryGroupEvent" )
public class RepositoryRegistryGroupEventInspector
    extends AbstractEventInspector
{

    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryRegistryGroupEvent)
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        try
        {
            RepositoryRegistryGroupEvent gevt = (RepositoryRegistryGroupEvent) evt;

            // we are handling repo events, like addition and removal
            if ( RepositoryRegistryGroupEventAdd.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().addRepositoryGroupIndexContext( gevt.getGroupId() );
            }
            else if ( RepositoryRegistryGroupEventRemove.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().removeRepositoryGroupIndexContext( gevt.getGroupId(), false );
            }
        }
        catch ( Exception e )
        {
            getLogger().error( "Could not maintain group (merged) indexing contexts!", e );
        }

    }

}
