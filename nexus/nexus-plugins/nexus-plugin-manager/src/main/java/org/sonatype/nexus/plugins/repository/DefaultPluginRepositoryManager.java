package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.PluginCoordinates;

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

    public Collection<PluginCoordinates> findAvailablePlugins()
    {
        HashSet<PluginCoordinates> result = new HashSet<PluginCoordinates>();

        for ( NexusPluginRepository repository : getRepositories().values() )
        {
            result.addAll( repository.findAvailablePlugins() );
        }

        return result;
    }

    public File resolvePlugin( PluginCoordinates coordinates )
    {
        // iterate as long as you get something non-null
        for ( NexusPluginRepository repository : getRepositories().values() )
        {
            File result = repository.resolvePlugin( coordinates );

            if ( result != null )
            {
                return result;
            }
        }

        // nobody has it
        return null;
    }

    public Collection<File> resolvePluginDependencies( PluginCoordinates coordinates )
    {
        ArrayList<File> result = new ArrayList<File>();

        for ( NexusPluginRepository repository : getRepositories().values() )
        {
            result.addAll( repository.resolvePluginDependencies( coordinates ) );
        }

        return result;
    }
}
