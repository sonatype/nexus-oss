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
package org.sonatype.nexus.maven.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.nexus.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.MinimumSnapshotCountPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.RemoveIfReleasedPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.SnapshotRetentionDaysPropertyDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * SnapshotRemoverTask
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = SnapshotRemovalTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class SnapshotRemoverTask
    extends AbstractNexusRepositoriesTask<SnapshotRemovalResult>
{
    public static final String SYSTEM_REMOVE_SNAPSHOTS_ACTION = "REMOVESNAPSHOTS";
    
    public static final int DEFAULT_MIN_SNAPSHOTS_TO_KEEP = 0;
    
    public static final int DEFAULT_OLDER_THAN_DAYS = -1;

    public int getMinSnapshotsToKeep()
    {        
        String param = getParameters().get( MinimumSnapshotCountPropertyDescriptor.ID );
        
        if ( StringUtils.isEmpty( param ) )
        {
            return DEFAULT_MIN_SNAPSHOTS_TO_KEEP;
        }
        
        return Integer.parseInt( param );
    }

    public void setMinSnapshotsToKeep( int minSnapshotsToKeep )
    {
        getParameters().put( MinimumSnapshotCountPropertyDescriptor.ID, Integer.toString( minSnapshotsToKeep ) );
    }

    public int getRemoveOlderThanDays()
    {
        String param = getParameters().get( SnapshotRetentionDaysPropertyDescriptor.ID );
        
        if ( StringUtils.isEmpty( param ) )
        {
            return DEFAULT_OLDER_THAN_DAYS;
        }
        
        return Integer.parseInt( param );
    }

    public void setRemoveOlderThanDays( int removeOlderThanDays )
    {
        getParameters().put( SnapshotRetentionDaysPropertyDescriptor.ID, Integer.toString( removeOlderThanDays ) );
    }

    public boolean isRemoveIfReleaseExists()
    {
        return Boolean.parseBoolean( getParameters().get( RemoveIfReleasedPropertyDescriptor.ID ) );
    }

    public void setRemoveIfReleaseExists( boolean removeIfReleaseExists )
    {
        getParameters().put( RemoveIfReleasedPropertyDescriptor.ID, Boolean.toString( removeIfReleaseExists ) );
    }

    public SnapshotRemovalResult doRun()
        throws Exception
    {
        SnapshotRemovalRequest req = new SnapshotRemovalRequest(
            getRepositoryId(),
            getRepositoryGroupId(),
            getMinSnapshotsToKeep(),
            getRemoveOlderThanDays(),
            isRemoveIfReleaseExists() );

        return getNexus().removeSnapshots( req );
    }

    protected String getAction()
    {
        return SYSTEM_REMOVE_SNAPSHOTS_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Removing snapshots from repository group with ID=" + getRepositoryGroupId();
        }
        else if ( getRepositoryId() != null )
        {
            return "Removing snapshots from repository with ID=" + getRepositoryId();
        }
        else
        {
            return "Removing snapshots from all registered repositories";
        }
    }

}
