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
package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "ClearCache", description="Clear Repository Caches" )
public class ClearCacheTaskDescriptor
    implements ScheduledTaskDescriptor, Initializable
{
    public static final String ID = "ClearCacheTask";
    
    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "RepositoryOrGroup" )
    private ScheduledTaskPropertyDescriptor repositoryOrGroupId;
    
    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "ResourceStorePath" )
    private ScheduledTaskPropertyDescriptor resourceStorePath;
    
    public void initialize()
        throws InitializationException
    {
        repositoryOrGroupId.setHelpText( "Type in the repository path from which to clear caches recursively (ie. \"/\" for root or \"/org/apache\")" );
    }
    
    public String getId()
    {
        return ID;
    }
    
    public String getName()
    {
        return "Clear Repository Caches";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();
        
        properties.add( repositoryOrGroupId );
        properties.add(  resourceStorePath );
        
        return properties;
    }
}
