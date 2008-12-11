/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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
    extends AbstractScheduledTaskDescriptor
    implements Initializable
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
        return "Expire Repository Caches";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();
        
        properties.add( repositoryOrGroupId );
        properties.add(  resourceStorePath );
        
        return properties;
    }
}
