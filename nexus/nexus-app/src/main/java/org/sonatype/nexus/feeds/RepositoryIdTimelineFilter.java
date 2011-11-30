package org.sonatype.nexus.feeds;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import org.sonatype.nexus.timeline.Entry;
import com.google.common.base.Predicate;

/**
 * TODO
 *
 * @author: cstamas
 */
public class RepositoryIdTimelineFilter
    implements Predicate<Entry>
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

    public boolean apply( final Entry hit )
    {
        return ( hit.getData().containsKey( DefaultFeedRecorder.REPOSITORY ) && repositoryIds.contains( hit.getData().get(
            DefaultFeedRecorder.REPOSITORY ) ) );
    }
}
