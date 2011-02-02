/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.maven.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
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

    @Override
    protected String getRepositoryFieldId()
    {
        return SnapshotRemovalTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    public int getMinSnapshotsToKeep()
    {
        String param = getParameters().get( SnapshotRemovalTaskDescriptor.MIN_TO_KEEP_FIELD_ID );

        if ( StringUtils.isEmpty( param ) )
        {
            return DEFAULT_MIN_SNAPSHOTS_TO_KEEP;
        }

        return Integer.parseInt( param );
    }

    public void setMinSnapshotsToKeep( int minSnapshotsToKeep )
    {
        getParameters().put( SnapshotRemovalTaskDescriptor.MIN_TO_KEEP_FIELD_ID, Integer.toString( minSnapshotsToKeep ) );
    }

    public int getRemoveOlderThanDays()
    {
        String param = getParameters().get( SnapshotRemovalTaskDescriptor.KEEP_DAYS_FIELD_ID );

        if ( StringUtils.isEmpty( param ) )
        {
            return DEFAULT_OLDER_THAN_DAYS;
        }

        return Integer.parseInt( param );
    }

    public void setRemoveOlderThanDays( int removeOlderThanDays )
    {
        getParameters().put( SnapshotRemovalTaskDescriptor.KEEP_DAYS_FIELD_ID, Integer.toString( removeOlderThanDays ) );
    }

    public boolean isRemoveIfReleaseExists()
    {
        return Boolean.parseBoolean( getParameters().get( SnapshotRemovalTaskDescriptor.REMOVE_WHEN_RELEASED_FIELD_ID ) );
    }

    public void setRemoveIfReleaseExists( boolean removeIfReleaseExists )
    {
        getParameters().put( SnapshotRemovalTaskDescriptor.REMOVE_WHEN_RELEASED_FIELD_ID,
                             Boolean.toString( removeIfReleaseExists ) );
    }

    @Override
    public SnapshotRemovalResult doRun()
        throws Exception
    {
        SnapshotRemovalRequest req =
            new SnapshotRemovalRequest( getRepositoryId(), getMinSnapshotsToKeep(), getRemoveOlderThanDays(),
                isRemoveIfReleaseExists() );

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
        if ( getRepositoryId() != null )
        {
            return "Removing snapshots from repository " + getRepositoryName();
        }
        else
        {
            return "Removing snapshots from all registered repositories";
        }
    }

}
