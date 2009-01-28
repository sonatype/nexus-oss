package org.sonatype.nexus.repository.metadata;

public class RawTransportRequest
{
    private String id;

    private String url;

    private String path;

    public RawTransportRequest()
    {
    }

    public RawTransportRequest( String id, String url, String path )
    {
        this();

        setId( id );

        setUrl( url );

        setPath( path );
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

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }
}
