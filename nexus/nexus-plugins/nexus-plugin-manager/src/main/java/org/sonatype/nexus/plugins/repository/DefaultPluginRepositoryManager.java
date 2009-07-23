package org.sonatype.nexus.plugins.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plugin.metadata.GAVCoordinate;

@Component( role = PluginRepositoryManager.class )
public class DefaultPluginRepositoryManager
    implements PluginRepositoryManager
{
    @Requirement( role = NexusPluginRepository.class )
    private Map<String, NexusPluginRepository> repositories;

    private Map<String, NexusPluginRepository> customRepositories;

    /**
     * Returns ALL repositories, merged. Custom repository may "override" a discovered repository.
     * 
     * @return
     */
    protected Map<String, NexusPluginRepository> getRepositories()
    {
        HashMap<String, NexusPluginRepository> result =
            new HashMap<String, NexusPluginRepository>( repositories.size() + getCustomRepositories().size() );

        result.putAll( repositories );

        result.putAll( getCustomRepositories() );

        return result;
    }

    /**
     * Gets custom repositories.
     * 
     * @return
     */
    protected Map<String, NexusPluginRepository> getCustomRepositories()
    {
        if ( customRepositories == null )
        {
            customRepositories = new HashMap<String, NexusPluginRepository>();
        }

        return customRepositories;
    }

    public void addCustomNexusPluginRepository( NexusPluginRepository repository )
    {
        getCustomRepositories().put( repository.getId(), repository );
    }

    public void removeCustomNexusPluginRepository( String id )
    {
        getCustomRepositories().remove( id );
    }

    public Collection<PluginRepositoryArtifact> findAvailablePlugins()
    {
        // we collect and order repositories into _descending_ order
        // to make system plugins stomp over the user added ones if
        // collision occurs
        TreeSet<NexusPluginRepository> repositories =
            new TreeSet<NexusPluginRepository>( new NexusPluginRepositoryComparator( true ) );

        repositories.addAll( getRepositories().values() );

        HashMap<GAVCoordinate, PluginRepositoryArtifact> result =
            new HashMap<GAVCoordinate, PluginRepositoryArtifact>();

        for ( NexusPluginRepository repository : repositories )
        {
            Collection<PluginRepositoryArtifact> artifacts = repository.findAvailablePlugins();

            for ( PluginRepositoryArtifact artifact : artifacts )
            {
                result.put( artifact.getCoordinate(), artifact );
            }
        }

        return result.values();
    }

    public PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException
    {
        // we collect and order repositories into _asceding_ order
        // to make dependencies be found in system repo before user repo if
        // collision occurs
        TreeSet<NexusPluginRepository> repositories =
            new TreeSet<NexusPluginRepository>( new NexusPluginRepositoryComparator( false ) );

        repositories.addAll( getRepositories().values() );

        for ( NexusPluginRepository repository : repositories )
        {
            try
            {
                return repository.resolveArtifact( coordinates );
            }
            catch ( NoSuchPluginRepositoryArtifactException e )
            {
                // nothing
            }
        }

        // nobody has it
        throw new NoSuchPluginRepositoryArtifactException( null, coordinates );
    }

    public PluginRepositoryArtifact resolveDependencyArtifact( PluginRepositoryArtifact dependant,
                                                               GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException
    {
        // 1st try the originating repository
        try
        {
            return dependant.getNexusPluginRepository().resolveDependencyArtifact( dependant, coordinates );
        }
        catch ( NoSuchPluginRepositoryArtifactException e )
        {
            // next, iterate over reposes and search it
            // we collect and order repositories into _asceding_ order
            // to make dependencies be found in system repo before user repo if
            // collision occurs
            TreeSet<NexusPluginRepository> repositories =
                new TreeSet<NexusPluginRepository>( new NexusPluginRepositoryComparator( false ) );

            repositories.addAll( getRepositories().values() );

            // iterate as long as you get something non-null

            for ( NexusPluginRepository repository : repositories )
            {
                try
                {
                    return repository.resolveDependencyArtifact( dependant, coordinates );
                }
                catch ( NoSuchPluginRepositoryArtifactException e1 )
                {
                    // nothing
                }
            }

            // nobody has it
            throw new NoSuchPluginRepositoryArtifactException( null, coordinates );
        }

    }
}
