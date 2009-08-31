/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.maven.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.maven.tasks.descriptors.properties.MinimumSnapshotCountPropertyDescriptor;
import org.sonatype.nexus.maven.tasks.descriptors.properties.RemoveIfReleasedPropertyDescriptor;
import org.sonatype.nexus.maven.tasks.descriptors.properties.SnapshotRetentionDaysPropertyDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
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

    @Override
    public SnapshotRemovalResult doRun()
        throws Exception
    {

        if ( getMinSnapshotsToKeep() < -1 )
        {
            throw new IllegalArgumentException( "Invalid number of snapshots to be kept.  Must be positive, 0 or -1!" );
        }

        if ( getRemoveOlderThanDays() < -1 )
        {
            throw new IllegalArgumentException( "Invalid number of days to be kept.  Must be positive, 0 or -1!" );
        }

        SnapshotRemovalRequest req =
            new SnapshotRemovalRequest( getRepositoryId(), getRepositoryGroupId(), getMinSnapshotsToKeep(),
                                        getRemoveOlderThanDays(), isRemoveIfReleaseExists() );

        return getNexus().removeSnapshots( req );
    }

    @Override
    protected String getAction()
    {
        return SYSTEM_REMOVE_SNAPSHOTS_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Removing snapshots from repository group " + getRepositoryGroupName();
        }
        else if ( getRepositoryId() != null )
        {
            return "Removing snapshots from repository " + getRepositoryName();
        }
        else
        {
            return "Removing snapshots from all registered repositories";
        }
    }

}
