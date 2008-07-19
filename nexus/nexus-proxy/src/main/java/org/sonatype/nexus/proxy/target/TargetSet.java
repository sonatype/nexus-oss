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
    extends HashSet<Target>
    implements Set<Target>
{
    boolean isPathContained( Repository repository, String path )
    {
        for ( Target target : this )
        {
            if ( target.isPathContained( repository, path ) )
            {
                return true;
            }
        }

        return false;
    }
}
