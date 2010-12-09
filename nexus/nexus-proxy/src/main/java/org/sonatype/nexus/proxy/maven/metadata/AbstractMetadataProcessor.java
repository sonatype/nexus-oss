package org.sonatype.nexus.proxy.maven.metadata;

import java.io.InputStream;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;

/**
 * @author juven
 */
public abstract class AbstractMetadataProcessor
{
    protected static final String METADATA_SUFFIX = "/maven-metadata.xml";

    protected AbstractMetadataHelper metadataHelper;

    public AbstractMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        this.metadataHelper = metadataHelper;
    }

    /**
     * @param path
     * @return true if got processed, else false
     * @throws Exception
     */
    public boolean process( String path )
        throws Exception
    {
        if ( !shouldProcessMetadata( path ) )
        {
            return false;
        }

        if ( isMetadataExisted( path ) )
        {
            Metadata metadata = readMetadata( path );

            if ( metadata != null && isMetadataCorrect( metadata, path ) )
            {
                postProcessMetadata( path );

                return true;
            }
            else
            {
                removedMetadata( path );
            }
        }

        processMetadata( path );

        postProcessMetadata( path );

        buildMetadataChecksum( path );

        return true;

    }

    protected boolean isMetadataExisted( String path )
        throws Exception
    {
        return metadataHelper.exists( path + METADATA_SUFFIX );
    }

    protected Metadata readMetadata( String path )
        throws Exception
    {
        InputStream mdStream = null;

        try
        {
            mdStream = metadataHelper.retrieveContent( path + METADATA_SUFFIX );

            Metadata md = MetadataBuilder.read( mdStream );

            return md;
        }
        catch ( MetadataException e )
        {
            if ( metadataHelper.logger.isDebugEnabled() )
            {
                metadataHelper.logger.info( "Failed to parse metadata from '" + path + "'", e );
            }
            else
            {
                metadataHelper.logger.info( "Failed to parse metadata from '" + path + "'" );
            }

            return null;
        }
        finally
        {
            IOUtil.close( mdStream );
        }
    }

    protected void removedMetadata( String path )
        throws Exception
    {
        metadataHelper.remove( path + METADATA_SUFFIX );
    }

    protected void buildMetadataChecksum( String path )
        throws Exception
    {
        metadataHelper.rebuildChecksum( path + METADATA_SUFFIX );
    }

    protected abstract boolean isMetadataCorrect( Metadata metadata, String path )
        throws Exception;

    protected abstract boolean shouldProcessMetadata( String path );

    protected abstract void processMetadata( String path )
        throws Exception;

    protected abstract void postProcessMetadata( String path );
}
