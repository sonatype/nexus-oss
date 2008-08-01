package org.sonatype.nexus.proxy.target;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A simple helper Set implementation.
 * 
 * @author cstamas
 */
public class TargetSet
    extends HashSet<TargetMatch>
    implements Set<TargetMatch>
{
    private int involvedRepositories;

    public int getInvolvedRepositories()
    {
        return involvedRepositories;
    }

    public void setInvolvedRepositories( int involvedRepositories )
    {
        this.involvedRepositories = involvedRepositories;
    }

    boolean isPathContained( Repository repository, String path )
    {
        for ( TargetMatch targetMatch : this )
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
