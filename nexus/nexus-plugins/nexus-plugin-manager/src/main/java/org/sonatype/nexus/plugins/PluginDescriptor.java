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
package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Describes a Nexus plugin: its metadata, exports/imports, and what resources it contains.
 */
public final class PluginDescriptor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final GAVCoordinate gav;

    private PluginMetadata metadata;

    private List<String> exportedClassnames = Collections.emptyList();

    private List<GAVCoordinate> importedPlugins = Collections.emptyList();

    private List<RepositoryTypeDescriptor> repositoryTypes = Collections.emptyList();

    private List<StaticResource> staticResources = Collections.emptyList();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PluginDescriptor( final GAVCoordinate gav )
    {
        this.gav = gav;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public GAVCoordinate getPluginCoordinates()
    {
        return gav;
    }

    public PluginMetadata getPluginMetadata()
    {
        return metadata;
    }

    public List<String> getExportedClassnames()
    {
        return exportedClassnames;
    }

    public List<GAVCoordinate> getImportedPlugins()
    {
        return importedPlugins;
    }

    public List<RepositoryTypeDescriptor> getRepositoryTypes()
    {
        return repositoryTypes;
    }

    public List<StaticResource> getStaticResources()
    {
        return staticResources;
    }

    public String formatAsString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append( "       Detailed report about plugin \"" ).append( gav ).append( "\":" ).append( LS );

        if ( metadata != null )
        {
            buf.append( LS );
            buf.append( "         Source: \"" ).append( metadata.sourceUrl ).append( "\"" ).append( LS );
        }

        // TODO: list components? list exports/imports?

        if ( repositoryTypes != null )
        {
            buf.append( LS );
            buf.append( "         Custom repository types:" ).append( LS );

            for ( final RepositoryTypeDescriptor type : repositoryTypes )
            {
                buf.append( "         * Repository type \"" ).append( type.getRole() );
                buf.append( "\", to be published at path \"" ).append( type.getPrefix() ).append( "\"" ).append( LS );
            }
        }

        if ( staticResources != null )
        {
            buf.append( LS );
            buf.append( "         Static resources:" ).append( LS );

            for ( final StaticResource resource : staticResources )
            {
                buf.append( "         * Content type \"" ).append( resource.getContentType() );
                buf.append( "\", to be published at path \"" ).append( resource.getPath() ).append( "\"" ).append( LS );
            }
        }

        return buf.toString();
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    void setPluginMetadata( final PluginMetadata metadata )
    {
        this.metadata = metadata;
    }

    void setExportedClassnames( final List<String> classNames )
    {
        exportedClassnames = Collections.unmodifiableList( new ArrayList<String>( classNames ) );
    }

    void setImportedPlugins( final List<GAVCoordinate> plugins )
    {
        importedPlugins = Collections.unmodifiableList( new ArrayList<GAVCoordinate>( plugins ) );
    }

    void setRepositoryTypes( final List<RepositoryTypeDescriptor> types )
    {
        repositoryTypes = Collections.unmodifiableList( new ArrayList<RepositoryTypeDescriptor>( types ) );
    }

    void setStaticResources( final List<StaticResource> resources )
    {
        staticResources = Collections.unmodifiableList( new ArrayList<StaticResource>( resources ) );
    }
}
