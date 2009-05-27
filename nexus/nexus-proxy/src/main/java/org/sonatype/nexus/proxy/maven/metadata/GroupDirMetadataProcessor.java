package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.AddPluginOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.mercury.repository.metadata.PluginOperand;

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
        throws Exception
    {
        Metadata md = createMetadata( path );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        metadataHelper.store( mdString, path + AbstractMetadataHelper.METADATA_SUFFIX );

    }

    private Metadata createMetadata( String path )
        throws MetadataException
    {
        Metadata md = new Metadata();

        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( Plugin plugin : metadataHelper.gData.get( path ) )
        {
            ops.add( new AddPluginOperation( new PluginOperand( plugin ) ) );
        }

        MetadataBuilder.changeMetadata( md, ops );

        return md;
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

    @SuppressWarnings( "unchecked" )
    @Override
    protected boolean isMetadataCorrect( String path )
        throws Exception
    {
        Metadata oldMd = readMetadata( path );

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

    private boolean isPluginEquals( Plugin p1, Plugin p2 )
    {
        if ( p1.getName() == null )
        {
            p1.setName( "" );
        }

        if ( p2.getName() == null )
        {
            p2.setName( "" );
        }

        if ( p1.getArtifactId().equals( p2.getArtifactId() ) && p1.getPrefix().equals( p2.getPrefix() )
            && p1.getName().equals( p2.getName() ) )
        {
            return true;
        }

        return false;
    }
}
