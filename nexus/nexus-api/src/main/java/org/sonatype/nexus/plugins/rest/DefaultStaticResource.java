package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class DefaultStaticResource
    implements StaticResource
{
    private final URL resourceURL;

    private volatile URLConnection urlConnection;

    public DefaultStaticResource( URL url )
    {
        this.resourceURL = url;
    }

    protected synchronized boolean checkConnection()
    {
        if ( urlConnection == null )
        {
            try
            {
                urlConnection = resourceURL.openConnection();
            }
            catch ( IOException e )
            {
                // ignore it?
                urlConnection = null;
            }
        }

        return urlConnection != null;
    }

    public String getPath()
    {
        return resourceURL.getPath();
    }

    public long getSize()
    {
        if ( checkConnection() )
        {
            return urlConnection.getContentLength();
        }
        else
        {
            return -1;
        }
    }

    public String getContentType()
    {
        if ( checkConnection() )
        {
            return urlConnection.getContentType();
        }
        else
        {
            return null;
        }
    }

    public InputStream getInputStream()
        throws IOException
    {
        if ( checkConnection() )
        {
            InputStream is = urlConnection.getInputStream();
            
            urlConnection = null;
            
            return is;
        }
        else
        {
            throw new IOException( "Invalid resource!" );
        }
    }

}
