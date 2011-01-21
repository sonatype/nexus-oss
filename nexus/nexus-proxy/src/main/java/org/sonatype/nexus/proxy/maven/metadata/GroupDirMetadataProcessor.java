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
package org.sonatype.nexus.proxy.maven.metadata;

import static org.sonatype.nexus.proxy.maven.metadata.operations.MetadataUtil.isPluginEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.sonatype.nexus.proxy.maven.metadata.operations.AddPluginOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility;
import org.sonatype.nexus.proxy.maven.metadata.operations.PluginOperand;

/**
 * Process maven metadata in plugin group directory
 * 
 * @author juven
 */
public class GroupDirMetadataProcessor
    extends AbstractMetadataProcessor
{
    public GroupDirMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        super( metadataHelper );
    }

    @Override
    public void processMetadata( String path )
        throws IOException
    {
        Metadata md = createMetadata( path );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        metadataHelper.store( mdString, path + AbstractMetadataHelper.METADATA_SUFFIX );

    }

    private Metadata createMetadata( String path )
        throws IOException
    {
        try
        {
            Metadata md = new Metadata();

            List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

            for ( Plugin plugin : metadataHelper.gData.get( path ) )
            {
                ops.add( new AddPluginOperation( new PluginOperand( ModelVersionUtility.LATEST_MODEL_VERSION, plugin ) ) );
            }

            MetadataBuilder.changeMetadata( md, ops );

            ModelVersionUtility.setModelVersion( md, ModelVersionUtility.LATEST_MODEL_VERSION );

            return md;
        }
        catch ( MetadataException e )
        {
            throw new IOException( e );
        }
    }

    @Override
    public boolean shouldProcessMetadata( String path )
    {
        Collection<Plugin> plugins = metadataHelper.gData.get( path );

        if ( plugins != null && !plugins.isEmpty() )
        {
            return true;
        }

        return false;
    }

    @Override
    public void postProcessMetadata( String path )
    {
        metadataHelper.gData.remove( path );
    }

    @Override
    protected boolean isMetadataCorrect( Metadata oldMd, String path )
        throws IOException
    {
        Metadata md = createMetadata( path );

        List<Plugin> oldPlugins = oldMd.getPlugins();

        if ( oldPlugins == null )
        {
            return false;
        }

        List<Plugin> plugins = md.getPlugins();

        if ( oldPlugins.size() != plugins.size() )
        {
            return false;
        }

        for ( int i = 0; i < oldPlugins.size(); i++ )
        {
            Plugin oldPlugin = oldPlugins.get( i );

            if ( !containPlugin( plugins, oldPlugin ) )
            {
                return false;
            }
        }

        return true;

    }

    private boolean containPlugin( List<Plugin> plugins, Plugin expect )
    {
        for ( Plugin plugin : plugins )
        {
            if ( isPluginEquals( plugin, expect ) )
            {
                return true;
            }
        }

        return false;
    }

}
