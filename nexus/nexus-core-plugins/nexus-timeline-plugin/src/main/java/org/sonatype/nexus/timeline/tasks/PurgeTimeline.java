/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.timeline.tasks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.timeline.NexusTimeline;
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
    /**
     * System event action: timeline purge
     */
    public static final String ACTION = "TL_PURGE";

    public static final String TYPES_KEY = "types";

    public static final String SUBTYPES_KEY = "subTypes";

    @Requirement
    private NexusTimeline timeline;
    
    public int getPurgeOlderThan()
    {
        return Integer.parseInt( getParameters().get( PurgeTimelineTaskDescriptor.OLDER_THAN_FIELD_ID ) );
    }

    public void setPurgeOlderThan( int purgeOlderThan )
    {
        getParameters().put( PurgeTimelineTaskDescriptor.OLDER_THAN_FIELD_ID, Integer.toString( purgeOlderThan ) );
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
            timeline.purgeOlderThan( System.currentTimeMillis() - ( getPurgeOlderThan() * A_DAY ), null, null, null );
        }
        else if ( getSubTypes().size() == 0 )
        {
            timeline.purgeOlderThan( System.currentTimeMillis() - ( getPurgeOlderThan() * A_DAY ), getTypes(), null,
                null );
        }
        else
        {
            timeline.purgeOlderThan( System.currentTimeMillis() - ( getPurgeOlderThan() * A_DAY ), getTypes(),
                getSubTypes(), null );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Purging Timeline records.";
    }

}
