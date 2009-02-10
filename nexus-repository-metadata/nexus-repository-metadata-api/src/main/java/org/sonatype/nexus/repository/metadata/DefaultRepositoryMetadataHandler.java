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
import org.sonatype.nexus.repository.metadata.validation.DefaultRepositoryMetadataValidator;
import org.sonatype.nexus.repository.metadata.validation.RepositoryMetadataValidator;

@Component( role = RepositoryMetadataHandler.class )
public class DefaultRepositoryMetadataHandler
    implements RepositoryMetadataHandler
{
    private static final String REPOSITORY_METADATA_PATH = "/.meta/nexus-repository-metadata.xml";

    protected RepositoryMetadataXpp3Reader repositoryMetadataXpp3Reader = new RepositoryMetadataXpp3Reader();

    protected RepositoryMetadataXpp3Writer repositoryMetadataXpp3Writer = new RepositoryMetadataXpp3Writer();

    protected OrderedRepositoryMirrorsMetadataXpp3Reader orderedRepositoryMirrorsMetadataXpp3Reader = new OrderedRepositoryMirrorsMetadataXpp3Reader();

    public RepositoryMetadata readRepositoryMetadata( RawTransport transport )
        throws MetadataHandlerException,
            IOException
    {
        return readRepositoryMetadata( transport, new DefaultRepositoryMetadataValidator() );
    }

    public RepositoryMetadata readRepositoryMetadata( RawTransport transport, RepositoryMetadataValidator validator )
        throws MetadataHandlerException,
            IOException
    {
        try
        {
            byte[] data = transport.readRawData( REPOSITORY_METADATA_PATH );

            // TODO: add means for transparent on-the-fly metadata upgrade

            if ( data != null )
            {
                try
                {
                    ByteArrayInputStream bis = new ByteArrayInputStream( data );

                    InputStreamReader isr = new InputStreamReader( bis, "UTF-8" );

                    RepositoryMetadata md = repositoryMetadataXpp3Reader.read( isr );

                    if ( validator != null )
                    {
                        validator.validate( md );
                    }

                    return md;
                }
                catch ( XmlPullParserException e )
                {
                    throw new MetadataHandlerException( "Metadata is corrupt!", e );
                }
            }
            else
            {
                return null;
            }
        }
        catch ( Exception e )
        {
            throw new MetadataHandlerException( e );
        }
    }

    public void writeRepositoryMetadata( RepositoryMetadata metadata, RawTransport transport )
        throws MetadataHandlerException,
            IOException
    {
        writeRepositoryMetadata( metadata, transport, new DefaultRepositoryMetadataValidator() );
    }

    public void writeRepositoryMetadata( RepositoryMetadata metadata, RawTransport transport,
        RepositoryMetadataValidator validator )
        throws MetadataHandlerException,
            IOException
    {
        try
        {
            if ( validator != null )
            {
                validator.validate( metadata );
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            OutputStreamWriter writer = new OutputStreamWriter( bos, "UTF-8" );

            repositoryMetadataXpp3Writer.write( writer, metadata );

            writer.flush();

            transport.writeRawData( REPOSITORY_METADATA_PATH, bos.toByteArray() );
        }
        catch ( Exception e )
        {
            throw new MetadataHandlerException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public OrderedRepositoryMirrorsMetadata fetchOrderedMirrorMetadata( RepositoryMetadata metadata,
        RawTransport transport )
        throws MetadataHandlerException,
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
