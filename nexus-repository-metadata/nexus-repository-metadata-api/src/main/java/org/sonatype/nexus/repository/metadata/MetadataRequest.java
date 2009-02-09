package org.sonatype.nexus.repository.metadata;

import org.sonatype.nexus.repository.metadata.validation.DefaultRepositoryMetadataValidator;
import org.sonatype.nexus.repository.metadata.validation.RepositoryMetadataValidator;

public class MetadataRequest
{
    private String id;

    private String url;

    private RepositoryMetadataValidator validator;

    public MetadataRequest()
    {
    }

    public MetadataRequest( String id, String url )
    {
        this();

        setId( id );

        setUrl( url );

        setValidator( new DefaultRepositoryMetadataValidator() );
    }

    public MetadataRequest( String id, String url, RawTransport transport )
    {
        this();

        setId( id );

        setUrl( url );

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

    public RepositoryMetadataValidator getValidator()
    {
        return validator;
    }

    public void setValidator( RepositoryMetadataValidator validator )
    {
        this.validator = validator;
    }
}
