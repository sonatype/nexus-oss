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
package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.tasks.descriptors.EmptyTrashTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.EmptyOlderThanDaysPropertyDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Empty trash.
 */
@Component( role = SchedulerTask.class, hint = EmptyTrashTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class EmptyTrashTask
    extends AbstractNexusTask<Object>
{

    public static final int DEFAULT_OLDER_THAN_DAYS = -1;

    /**
     * The Wastebasket component.
     */
    @Requirement( role = Wastebasket.class )
    private Wastebasket wastebasket;

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( getEmptyOlderCacheItemsThan() == DEFAULT_OLDER_THAN_DAYS )
        {
            wastebasket.purge();
        }
        else
        {
            wastebasket.purge( getEmptyOlderCacheItemsThan() * A_DAY );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_EMPTY_TRASH_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Emptying Trash.";
    }

    public int getEmptyOlderCacheItemsThan()
    {
        String days = getParameters().get( EmptyOlderThanDaysPropertyDescriptor.ID );

        if ( StringUtils.isEmpty( days ) )
        {
            return DEFAULT_OLDER_THAN_DAYS;
        }

        return Integer.parseInt( days );
    }

    public void setEmptyOlderCacheItemsThan( int emptyOlderCacheItemsThan )
    {
        getParameters().put( EmptyOlderThanDaysPropertyDescriptor.ID, Integer.toString( emptyOlderCacheItemsThan ) );
    }
}
