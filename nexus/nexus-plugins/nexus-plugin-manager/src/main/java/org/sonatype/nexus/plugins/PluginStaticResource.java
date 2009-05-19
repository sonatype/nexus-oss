package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.plugins.rest.StaticResource;

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

    /**
     * The model for plugin static resources.
     * 
     * @author cstamas
     */
    public static class PluginStaticResourceModel
    {
        private final String resourcePath;

        private final String publishedPath;

        private final String contentType;

        public PluginStaticResourceModel( String resourcePath, String publishedPath, String contentType )
        {
            this.resourcePath = resourcePath;

            this.publishedPath = publishedPath;

            this.contentType = contentType;
        }

        public String getResourcePath()
        {
            return resourcePath;
        }

        public String getPublishedPath()
        {
            return publishedPath;
        }

        public String getContentType()
        {
            return contentType;
        }
    }
}
