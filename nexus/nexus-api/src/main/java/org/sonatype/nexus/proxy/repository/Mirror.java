package org.sonatype.nexus.proxy.repository;

public class Mirror
{
    private String id;

    private String url;

    public Mirror( String id, String url )
    {
        setId( id );
        setUrl( url );
    }

    public String getId()
    {
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }
}
