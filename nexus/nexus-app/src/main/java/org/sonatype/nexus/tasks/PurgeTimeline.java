package org.sonatype.nexus.tasks;

import java.util.Arrays;
import java.util.Date;
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
 * @plexus.component role="org.sonatype.nexus.tasks.PurgeTimeline"
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

    public Date getPurgeOlderThan()
    {
        long ts = Long.parseLong( getParameters().get( PURGE_OLDER_THAN_KEY ) );

        return new Date( ts );
    }

    public void setPurgeOlderThan( Date purgeOlderThan )
    {
        getParameters().put( PURGE_OLDER_THAN_KEY, Long.toString( purgeOlderThan.getTime() ) );
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
            timeline.purgeOlderThan( getPurgeOlderThan().getTime() );
        }
        else if ( getSubTypes().size() == 0 )
        {
            timeline.purgeOlderThan( getPurgeOlderThan().getTime(), getTypes() );
        }
        else
        {
            timeline.purgeOlderThan( getPurgeOlderThan().getTime(), getTypes(), getSubTypes() );
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
