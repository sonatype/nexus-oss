package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.util.jar.JarEntry;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.plugins.rest.StaticResource;

/**
 * A plugin manager specific StaticResource implementation. It uses plugin's Class Realm to load up static resources.
 * 
 * @author cstamas
 */
public class PluginStaticResource
    implements StaticResource
{
    private final ClassRealm classLoader;

    private final String resourcePath;

    private final String publishedPath;

    private final String contentType;

    public PluginStaticResource( ClassRealm classLoader, PluginStaticResourceModel model )
    {
        this( classLoader, model.getResourcePath(), model.getPublishedPath(), model.getContentType() );
    }

    public PluginStaticResource( ClassRealm classLoader, String resourcePath, String publishedPath, String contentType )
    {
        if ( classLoader == null || StringUtils.isBlank( resourcePath ) )
        {
            throw new IllegalArgumentException( "The passed class loader or resource path may not be null!" );
        }

        this.classLoader = classLoader;

        this.resourcePath = resourcePath;

        if ( StringUtils.isEmpty( publishedPath ) )
        {
            this.publishedPath = resourcePath;
        }
        else
        {
            this.publishedPath = publishedPath;
        }

        this.contentType = contentType;
    }

    public String getPath()
    {
        return publishedPath;
    }

    public long getSize()
    {
        URLConnection urlConnection = null;

        try
        {
            urlConnection = classLoader.getRealmResource( resourcePath ).openConnection();

            if ( urlConnection != null )
            {
                urlConnection.connect();

                return urlConnection.getContentLength();
            }
            else
            {
                return -1;
            }
        }
        catch ( IOException e )
        {
            // ignore it?
            return -1;
        }
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return classLoader.getResourceAsStream( resourcePath );
    }

    public Long getLastModified()
    {
        try
        {
            URLConnection urlConn = classLoader.getRealmResource( resourcePath ).openConnection();
            if ( !( urlConn instanceof JarURLConnection ) )
            {
                return urlConn.getLastModified();
            }

            JarURLConnection jarUrlConn = (JarURLConnection) urlConn;
            JarEntry jarEntry = jarUrlConn.getJarEntry();
            if ( jarEntry == null )
            {
                // This is a jar, not an entry in a jar
                return urlConn.getLastModified();
            }

            return jarEntry.getTime();
        }
        catch ( final Throwable e ) // NOPMD
        {
            // default to unknown last modified time
            return null;
        }
    }
}
