package org.sonatype.nexus.proxy.target;

import java.util.HashMap;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The default implementation of target registry.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultTargetRegistry
    implements TargetRegistry
{
    private HashMap<String, Target> targets = new HashMap<String, Target>();

    public void addRepositoryTarget( Target target )
    {
        targets.put( target.getId(), target );
    }

    public Target getRepositoryTarget( String id )
    {
        return targets.get( id );
    }

    public boolean removeRepositoryTarget( String id )
    {
        if ( targets.containsKey( id ) )
        {
            targets.remove( id );

            return true;
        }

        return false;
    }

    public TargetSet getTargetsForRepositoryPath( Repository repository, String path )
    {
        TargetSet result = new TargetSet();

        for ( Target t : targets.values() )
        {
            if ( t.isPathContained( repository, path ) )
            {
                result.add( t );
            }
        }

        return result;
    }

}
