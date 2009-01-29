package org.sonatype.nexus.repository.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.repository.metadata.model.OrderedMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.OrderedRepositoryMirrorsMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.OrderedRepositoryMirrorsMetadataXpp3Reader;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Reader;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Writer;
import org.sonatype.nexus.repository.metadata.validation.ValidationException;

public class DefaultRepositoryMetadataHandler
    implements RepositoryMetadataHandler
{
    private String repositoryMetadataPath = "/.meta/nexus-repository-metadata.xml";

    protected RepositoryMetadataXpp3Reader repositoryMetadataXpp3Reader = new RepositoryMetadataXpp3Reader();

    protected RepositoryMetadataXpp3Writer repositoryMetadataXpp3Writer = new RepositoryMetadataXpp3Writer();

    protected OrderedRepositoryMirrorsMetadataXpp3Reader orderedRepositoryMirrorsMetadataXpp3Reader = new OrderedRepositoryMirrorsMetadataXpp3Reader();

    public RepositoryMetadata readRepositoryMetadata( MetadataRequest req )
        throws IOException
    {
        RawTransportRequest request = new RawTransportRequest( req.getId(), req.getUrl(), repositoryMetadataPath );

        byte[] data = req.getTransport().readRawData( request );

        if ( data != null )
        {
            try
            {
                ByteArrayInputStream bis = new ByteArrayInputStream( data );

                InputStreamReader isr = new InputStreamReader( bis, "UTF-8" );

                RepositoryMetadata md = repositoryMetadataXpp3Reader.read( isr );

                if ( req.getValidator() != null )
                {
                    try
                    {
                        req.getValidator().validate( md );
                    }
                    catch ( ValidationException e )
                    {
                        IOException ex = new IOException( "Remote metadata is invalid!" );

                        ex.initCause( e );

                        throw ex;
                    }
                }

                return md;
            }
            catch ( XmlPullParserException e )
            {
                IOException ex = new IOException( "Metadata unparsable!" );

                ex.initCause( e );

                throw ex;
            }
        }
        else
        {
            return null;
        }
    }

    public void writeRepositoryMetadata( MetadataRequest req, RepositoryMetadata metadata )
        throws IOException
    {
        if ( req.getValidator() != null )
        {
            try
            {
                req.getValidator().validate( metadata );
            }
            catch ( ValidationException e )
            {
                IOException ex = new IOException( "Metadata to store is invalid!" );

                ex.initCause( e );

                throw ex;
            }
        }

        RawTransportRequest request = new RawTransportRequest( req.getId(), req.getUrl(), repositoryMetadataPath );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        OutputStreamWriter osw = new OutputStreamWriter( bos, "UTF-8" );

        repositoryMetadataXpp3Writer.write( osw, metadata );

        req.getTransport().writeRawData( request, bos.toByteArray() );
    }

    public OrderedRepositoryMirrorsMetadata fetchOrderedMirrorMetadata( RepositoryMetadata metadata,
        RawTransport transport )
        throws IOException
    {
        OrderedRepositoryMirrorsMetadata result = new OrderedRepositoryMirrorsMetadata();

        result.setVersion( OrderedRepositoryMirrorsMetadata.MODEL_VERSION );

        result.setStrategy( OrderedRepositoryMirrorsMetadata.STRATEGY_CLIENT_MANUAL );

        result.setRequestIp( null );

        result.setRequestTimestamp( System.currentTimeMillis() );

        for ( RepositoryMirrorMetadata mmd : metadata.getMirrors() )
        {
            OrderedMirrorMetadata omd = new OrderedMirrorMetadata();

            omd.setId( mmd.getId() );
            omd.setUrl( mmd.getUrl() );

            result.addMirror( omd );
        }

        return result;
    }
}
