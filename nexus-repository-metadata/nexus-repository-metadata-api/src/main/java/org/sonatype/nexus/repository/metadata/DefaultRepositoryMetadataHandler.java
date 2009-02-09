package org.sonatype.nexus.repository.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.repository.metadata.model.OrderedMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.OrderedRepositoryMirrorsMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.OrderedRepositoryMirrorsMetadataXpp3Reader;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Reader;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Writer;
import org.sonatype.nexus.repository.metadata.validation.ValidationException;

@Component( role = RepositoryMetadataHandler.class )
public class DefaultRepositoryMetadataHandler
    implements RepositoryMetadataHandler
{
    private String repositoryMetadataPath = "/.meta/nexus-repository-metadata.xml";

    protected RepositoryMetadataXpp3Reader repositoryMetadataXpp3Reader = new RepositoryMetadataXpp3Reader();

    protected RepositoryMetadataXpp3Writer repositoryMetadataXpp3Writer = new RepositoryMetadataXpp3Writer();

    protected OrderedRepositoryMirrorsMetadataXpp3Reader orderedRepositoryMirrorsMetadataXpp3Reader = new OrderedRepositoryMirrorsMetadataXpp3Reader();

    public RepositoryMetadata createMetadata( String url, String recommendedId, String recommendedName, String layout,
        String policy )
    {
        RepositoryMetadata result = new RepositoryMetadata();

        result.setVersion( RepositoryMetadata.MODEL_VERSION );

        result.setUrl( url );

        result.setId( recommendedId );

        result.setName( recommendedName );

        result.setLayout( layout );

        result.setPolicy( policy );

        return result;
    }

    public RepositoryMetadata readRepositoryMetadata( MetadataRequest req )
        throws MetadadaHandlerException,
            IOException
    {
        RawTransportRequest request = new RawTransportRequest( req.getId(), req.getUrl(), repositoryMetadataPath );

        try
        {
            byte[] data = req.getTransport().readRawData( request );

            // TODO: add means for transparent on-the-fly metadata upgrade

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
        catch ( Exception e )
        {
            throw new MetadadaHandlerException( e );
        }
    }

    public void writeRepositoryMetadata( MetadataRequest req, RepositoryMetadata metadata )
        throws MetadadaHandlerException,
            IOException
    {
        try
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
        catch ( Exception e )
        {
            throw new MetadadaHandlerException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public OrderedRepositoryMirrorsMetadata fetchOrderedMirrorMetadata( RepositoryMetadata metadata,
        RawTransport transport )
        throws MetadadaHandlerException,
            IOException
    {
        OrderedRepositoryMirrorsMetadata result = new OrderedRepositoryMirrorsMetadata();

        result.setVersion( OrderedRepositoryMirrorsMetadata.MODEL_VERSION );

        result.setStrategy( OrderedRepositoryMirrorsMetadata.STRATEGY_CLIENT_MANUAL );

        result.setRequestIp( null );

        result.setRequestTimestamp( System.currentTimeMillis() );

        for ( RepositoryMirrorMetadata mmd : (List<RepositoryMirrorMetadata>) metadata.getMirrors() )
        {
            OrderedMirrorMetadata omd = new OrderedMirrorMetadata();

            omd.setId( mmd.getId() );
            omd.setUrl( mmd.getUrl() );

            result.addMirror( omd );
        }

        return result;
    }
}
