package org.sonatype.nexus.repository.metadata;

import org.restlet.data.Protocol;
import org.sonatype.nexus.repository.metadata.restlet.RestletRawTransport;
import org.sonatype.nexus.repository.metadata.validation.DefaultRepositoryMetadataValidator;
import org.sonatype.nexus.repository.metadata.validation.RepositoryMetadataValidator;

public class MetadataRequest
{
    private String id;

    private String url;

    private RawTransport transport;

    private RepositoryMetadataValidator validator;

    public MetadataRequest()
    {
    }

    public MetadataRequest( String id, String url )
    {
        this();

        setId( id );

        setUrl( url );

        if ( url != null )
        {
            if ( url.startsWith( "http:" ) )
            {
                setTransport( new RestletRawTransport() );
            }
            else
            {
                String protocol = url.substring( 0, url.indexOf( "://" ) );

                setTransport( new RestletRawTransport( Protocol.valueOf( protocol.toUpperCase() ) ) );
            }
        }

        setValidator( new DefaultRepositoryMetadataValidator() );
    }

    public MetadataRequest( String id, String url, RawTransport transport )
    {
        this();

        setId( id );

        setUrl( url );

        setTransport( transport );

        setValidator( new DefaultRepositoryMetadataValidator() );
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public RawTransport getTransport()
    {
        return transport;
    }

    public void setTransport( RawTransport transport )
    {
        this.transport = transport;
    }

    public RepositoryMetadataValidator getValidator()
    {
        return validator;
    }

    public void setValidator( RepositoryMetadataValidator validator )
    {
        this.validator = validator;
    }
}
