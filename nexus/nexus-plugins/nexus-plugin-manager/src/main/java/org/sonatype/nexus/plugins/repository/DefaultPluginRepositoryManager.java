package org.sonatype.nexus.plugins.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        HashSet<PluginRepositoryArtifact> result = new HashSet<PluginRepositoryArtifact>();

        for ( NexusPluginRepository repository : getRepositories().values() )
        {
            result.addAll( repository.findAvailablePlugins() );
        }

        return result;
    }

    public PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates )
    {
        // iterate as long as you get something non-null
        PluginRepositoryArtifact result = null;

        for ( NexusPluginRepository repository : getRepositories().values() )
        {
            result = repository.resolveArtifact( coordinates );

            if ( result != null )
            {
                return result;
            }
        }

        // nobody has it
        return null;
    }
}
