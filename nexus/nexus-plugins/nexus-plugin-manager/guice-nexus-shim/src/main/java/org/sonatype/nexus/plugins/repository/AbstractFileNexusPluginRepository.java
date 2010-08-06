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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Abstract {@link NexusPluginRepository} backed by a file-system.
 */
public abstract class AbstractFileNexusPluginRepository
    extends AbstractNexusPluginRepository
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String PLUGIN_XML = "META-INF/nexus/plugin.xml";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Requirement
    private ArtifactPackagingMapper packagingMapper;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final Map<GAVCoordinate, PluginMetadata> findAvailablePlugins()
    {
        final File[] plugins = getPluginFolders();
        if ( null == plugins )
        {
            return Collections.emptyMap();
        }

        final Map<GAVCoordinate, PluginMetadata> installedPlugins =
            new HashMap<GAVCoordinate, PluginMetadata>( plugins.length );

        for ( final File f : plugins )
        {
            if ( !f.isDirectory() )
            {
                continue;
            }
            final File pluginJar = getPluginJar( f );
            if ( !pluginJar.isFile() )
            {
                continue;
            }
            final PluginMetadata md = getPluginMetadata( pluginJar );
            if ( null == md )
            {
                continue;
            }
            installedPlugins.put( new GAVCoordinate( md.getGroupId(), md.getArtifactId(), md.getVersion() ), md );
        }

        return installedPlugins;
    }

    public final PluginRepositoryArtifact resolveArtifact( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        return new PluginRepositoryArtifact( gav, resolvePluginJar( gav ), this );
    }

    public final PluginRepositoryArtifact resolveDependencyArtifact( final PluginRepositoryArtifact plugin,
                                                                     final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        final File dependenciesFolder = new File( getPluginFolder( plugin.getCoordinate() ), "dependencies" );
        final File artifact = new File( dependenciesFolder, gav.getFinalName( packagingMapper ) );
        if ( !artifact.isFile() )
        {
            throw new NoSuchPluginRepositoryArtifactException( this, gav );
        }
        return new PluginRepositoryArtifact( gav, artifact, this );
    }

    public final PluginMetadata getPluginMetadata( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        return getPluginMetadata( resolvePluginJar( gav ) );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract File getNexusPluginsDirectory();

    protected File[] getPluginFolders()
    {
        return getNexusPluginsDirectory().listFiles();
    }

    @SuppressWarnings( "unused" )
    protected File getPluginFolder( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        return new File( getNexusPluginsDirectory(), gav.getArtifactId() + '-' + gav.getVersion() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static final File getPluginJar( final File pluginFolder )
    {
        return new File( pluginFolder, pluginFolder.getName() + ".jar" );
    }

    private final File resolvePluginJar( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        final File pluginFolder = getPluginFolder( gav );
        final File pluginJar = getPluginJar( pluginFolder );
        if ( pluginJar.isFile() )
        {
            return pluginJar;
        }
        throw new NoSuchPluginRepositoryArtifactException( this, gav );
    }

    private final PluginMetadata getPluginMetadata( final File file )
    {
        try
        {
            return getPluginMetadata( new URL( "jar:" + file.toURI() + "!/" + PLUGIN_XML ) );
        }
        catch ( final IOException e )
        {
            return null;
        }
    }
}
