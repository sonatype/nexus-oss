package org.sonatype.nexus.tasks;

import java.util.Date;
import java.util.Set;

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
    /**
     * @plexus.requirement
     */
    private Timeline timeline;

    private Date purgeOlderThan;

    private Set<String> types;

    private Set<String> subTypes;

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( types == null || types.size() == 0 )
        {
            timeline.purgeOlderThan( purgeOlderThan.getTime() );
        }
        else if ( subTypes == null || subTypes.size() == 0 )
        {
            timeline.purgeOlderThan( purgeOlderThan.getTime(), types );
        }
        else
        {
            timeline.purgeOlderThan( purgeOlderThan.getTime(), types, subTypes );
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
