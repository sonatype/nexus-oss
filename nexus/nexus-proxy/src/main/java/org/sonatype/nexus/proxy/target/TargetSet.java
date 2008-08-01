package org.sonatype.nexus.proxy.target;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A simple helper Set implementation.
 * 
 * @author cstamas
 */
public class TargetSet
{
    private final Set<TargetMatch> matches = new HashSet<TargetMatch>();

    private final Set<String> matchedRepositoryIds = new HashSet<String>();

    public Set<TargetMatch> getMatches()
    {
        return Collections.unmodifiableSet( matches );
    }

    public Set<String> getMatchedRepositoryIds()
    {
        return Collections.unmodifiableSet( matchedRepositoryIds );
    }

    public void addTargetMatch( TargetMatch tm )
    {
        matches.add( tm );

        matchedRepositoryIds.add( tm.getRepository().getId() );
    }

    public void addTargetSet( TargetSet ts )
    {
        matches.addAll( ts.getMatches() );

        matchedRepositoryIds.addAll( ts.getMatchedRepositoryIds() );
    }

    boolean isPathContained( Repository repository, String path )
    {
        for ( TargetMatch targetMatch : matches )
        {
            if ( targetMatch.getRepository().getId().equals( repository.getId() )
                && targetMatch.getTarget().isPathContained( repository, path ) )
            {
                return true;
            }
        }

        return false;
    }
}
