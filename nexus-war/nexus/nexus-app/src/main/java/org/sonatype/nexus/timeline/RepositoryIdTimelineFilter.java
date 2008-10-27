package org.sonatype.nexus.timeline;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.feeds.DefaultFeedRecorder;

public class RepositoryIdTimelineFilter
    implements TimelineFilter
{
    private final Set<String> repositoryIds;

    public RepositoryIdTimelineFilter( String repositoryId )
    {
        this.repositoryIds = new HashSet<String>();

        this.repositoryIds.add( repositoryId );
    }

    public RepositoryIdTimelineFilter( Set<String> repositoryIds )
    {
        this.repositoryIds = repositoryIds;
    }

    public boolean accept( Map<String, String> hit )
    {
        return ( hit.containsKey( DefaultFeedRecorder.REPOSITORY ) && repositoryIds.contains( hit
            .get( DefaultFeedRecorder.REPOSITORY ) ) );
    }
}
