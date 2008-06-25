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
package org.sonatype.nexus.tasks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.timeline.Timeline;

/**
 * Purge timeline.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.scheduling.SchedulerTask" role-hint="org.sonatype.nexus.tasks.PurgeTimeline"
 *                   instantiation-strategy="per-lookup"
 */
public class PurgeTimeline
    extends AbstractNexusTask<Object>
{
    public static final String PURGE_OLDER_THAN_KEY = "purgeOlderThan";

    public static final String TYPES_KEY = "types";

    public static final String SUBTYPES_KEY = "subTypes";

    /**
     * @plexus.requirement
     */
    private Timeline timeline;

    public int getPurgeOlderThan()
    {
        return Integer.parseInt( getParameters().get( PURGE_OLDER_THAN_KEY ) );
    }

    public void setPurgeOlderThan( int purgeOlderThan )
    {
        getParameters().put( PURGE_OLDER_THAN_KEY, Integer.toString( purgeOlderThan ) );
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
            timeline.purgeOlderThan( System.currentTimeMillis() - ( getPurgeOlderThan() * 24 * 60 * 60 * 1000 ) );
        }
        else if ( getSubTypes().size() == 0 )
        {
            timeline.purgeOlderThan(
                System.currentTimeMillis() - ( getPurgeOlderThan() * 24 * 60 * 60 * 1000 ),
                getTypes() );
        }
        else
        {
            timeline.purgeOlderThan(
                System.currentTimeMillis() - ( getPurgeOlderThan() * 24 * 60 * 60 * 1000 ),
                getTypes(),
                getSubTypes() );
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
