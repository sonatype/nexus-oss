package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.StringUtils;

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

    // ==

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null || ( o.getClass() != this.getClass() ) )
        {
            return false;
        }

        Mirror other = (Mirror) o;

        return StringUtils.equals( getId(), other.getId() ) && StringUtils.equals( getUrl(), other.getUrl() );
    }

    public int hashCode()
    {
        int result = 7;

        result = 31 * result + ( id == null ? 0 : id.hashCode() );

        result = 31 * result + ( url == null ? 0 : url.hashCode() );

        return result;
    }
}
