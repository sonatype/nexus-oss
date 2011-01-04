/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.codehaus.plexus.component.annotations.Requirement;
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
