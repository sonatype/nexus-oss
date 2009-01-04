package org.sonatype.nexus.proxy.target;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The default implementation of target registry.
 * 
 * @author cstamas
 */
@Component( role = TargetRegistry.class )
public class DefaultTargetRegistry
    extends AbstractLogEnabled
    implements TargetRegistry
{
    private HashMap<String, Target> targets = new HashMap<String, Target>();

    public void addRepositoryTarget( Target target )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Added target " + target.getId() );
        }

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
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Removed target " + id );
            }

            targets.remove( id );

            return true;
        }

        return false;
    }

    public Set<Target> getTargetsForContentClassPath( ContentClass contentClass, String path )
    {
        Set<Target> result = new HashSet<Target>();

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Resolving targets for contentClass='" + contentClass.getId() + "' for path='" + path + "'" );
        }

        for ( Target t : targets.values() )
        {
            if ( t.isPathContained( contentClass, path ) )
            {
                result.add( t );
            }
        }

        return result;
    }

    public TargetSet getTargetsForRepositoryPath( Repository repository, String path )
    {
        TargetSet result = new TargetSet();

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Resolving targets for repository='" + repository.getId() + "' for path='" + path + "'" );
        }

        for ( Target t : targets.values() )
        {
            if ( t.isPathContained( repository.getRepositoryContentClass(), path ) )
            {
                result.addTargetMatch( new TargetMatch( t, repository ) );
            }
        }

        return result;
    }

}
