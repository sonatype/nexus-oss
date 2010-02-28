/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Default {@link PluginRepositoryManager} implementation.
 */
@Component( role = PluginRepositoryManager.class )
final class DefaultPluginRepositoryManager
    implements PluginRepositoryManager
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Comparator<NexusPluginRepository> REPOSITORY_COMPARATOR =
        new NexusPluginRepositoryComparator();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Requirement( role = NexusPluginRepository.class )
    private Map<String, NexusPluginRepository> repositoryMap;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getId()
    {
        return null;
    }

    public int getPriority()
    {
        return -1;
    }

    public NexusPluginRepository getNexusPluginRepository( final String id )
    {
        return repositoryMap.get( id );
    }

    public Map<GAVCoordinate, PluginMetadata> findAvailablePlugins()
    {
        final Map<GAVCoordinate, PluginMetadata> installedPlugins = new HashMap<GAVCoordinate, PluginMetadata>();
        for ( final NexusPluginRepository r : getRepositories( true ) )
        {
            installedPlugins.putAll( r.findAvailablePlugins() );
        }
        return installedPlugins;
    }

    public PluginRepositoryArtifact resolveArtifact( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        for ( final NexusPluginRepository r : getRepositories( false ) )
        {
            try
            {
                return r.resolveArtifact( gav );
            }
            catch ( final NoSuchPluginRepositoryArtifactException e ) // NOPMD
            {
                // continue
            }
        }
        throw new NoSuchPluginRepositoryArtifactException( null, gav );
    }

    public PluginRepositoryArtifact resolveDependencyArtifact( final PluginRepositoryArtifact plugin,
                                                               final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        try
        {
            return plugin.getNexusPluginRepository().resolveDependencyArtifact( plugin, gav );
        }
        catch ( final NoSuchPluginRepositoryArtifactException e ) // NOPMD
        {
            // continue
        }
        for ( final NexusPluginRepository r : getRepositories( false ) )
        {
            try
            {
                if ( r != plugin.getNexusPluginRepository() )
                {
                    return r.resolveDependencyArtifact( plugin, gav );
                }
            }
            catch ( final NoSuchPluginRepositoryArtifactException e ) // NOPMD
            {
                // continue
            }
        }
        throw new NoSuchPluginRepositoryArtifactException( null, gav );
    }

    public PluginMetadata getPluginMetadata( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        for ( final NexusPluginRepository r : getRepositories( false ) )
        {
            try
            {
                return r.getPluginMetadata( gav );
            }
            catch ( final NoSuchPluginRepositoryArtifactException e ) // NOPMD
            {
                // continue
            }
        }
        throw new NoSuchPluginRepositoryArtifactException( null, gav );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private NexusPluginRepository[] getRepositories( final boolean reverse )
    {
        final Collection<NexusPluginRepository> values = repositoryMap.values();
        final NexusPluginRepository[] array = values.toArray( new NexusPluginRepository[values.size()] );
        if ( reverse )
        {
            Arrays.sort( array, Collections.reverseOrder( REPOSITORY_COMPARATOR ) );
        }
        else
        {
            Arrays.sort( array, REPOSITORY_COMPARATOR );
        }
        return array;
    }
}
