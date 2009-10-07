package org.sonatype.nexus.feeds;

import java.util.List;

/**
 * Filter the Feeds results.  For example if the user didn't have access to an element in the feed it should be filtered so they do not see it.
 */
public interface FeedArtifactEventFilter
{
    /**
     * Filters the list <code>artifactEvents</code>.
     * @param artifactEvents the events to be filtered.
     * @return A subset of the original <code>artifactEvents</code> list.
     */
    List<NexusArtifactEvent> filterArtifactEventList( List<NexusArtifactEvent> artifactEvents );
}
