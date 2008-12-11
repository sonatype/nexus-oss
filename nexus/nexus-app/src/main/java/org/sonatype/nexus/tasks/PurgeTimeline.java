/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.tasks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.tasks.descriptors.PurgeTimelineTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.PurgeOlderThanDaysPropertyDescriptor;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Purge timeline.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = PurgeTimelineTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class PurgeTimeline
    extends AbstractNexusTask<Object>
{
    public static final String TYPES_KEY = "types";

    public static final String SUBTYPES_KEY = "subTypes";

    @Requirement( role = Timeline.class )
    private Timeline timeline;

    public int getPurgeOlderThan()
    {
        return Integer.parseInt( getParameters().get( PurgeOlderThanDaysPropertyDescriptor.ID ) );
    }

    public void setPurgeOlderThan( int purgeOlderThan )
    {
        getParameters().put( PurgeOlderThanDaysPropertyDescriptor.ID, Integer.toString( purgeOlderThan ) );
    }

    public Set<String> getTypes()
    {
        String[] elems = null;

        if ( getParameters().containsKey( TYPES_KEY ) )
        {
            elems = getParameters().get( TYPES_KEY ).split( "," );
        }
        else
        {
            elems = new String[0];
        }

        return new HashSet<String>( Arrays.asList( elems ) );
    }

    public void setTypes( Set<String> types )
    {
        getParameters().put( TYPES_KEY, StringUtils.join( types.toArray(), "," ) );
    }

    public Set<String> getSubTypes()
    {
        String[] elems = null;

        if ( getParameters().containsKey( SUBTYPES_KEY ) )
        {
            elems = getParameters().get( SUBTYPES_KEY ).split( "," );
        }
        else
        {
            elems = new String[0];
        }

        return new HashSet<String>( Arrays.asList( elems ) );
    }

    public void setSubTypes( Set<String> subTypes )
    {
        getParameters().put( SUBTYPES_KEY, StringUtils.join( subTypes.toArray(), "," ) );
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( getTypes().size() == 0 )
        {
            timeline.purgeOlderThan( System.currentTimeMillis() - ( (long) getPurgeOlderThan() * A_DAY ) );
        }
        else if ( getSubTypes().size() == 0 )
        {
            timeline.purgeOlderThan( System.currentTimeMillis() - ( (long) getPurgeOlderThan() * A_DAY ), getTypes() );
        }
        else
        {
            timeline.purgeOlderThan(
                System.currentTimeMillis() - ( (long) getPurgeOlderThan() * A_DAY ),
                getTypes(),
                getSubTypes(),
                null );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_TL_PURGE_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Purging Timeline records.";
    }

}
