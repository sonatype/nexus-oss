package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractDocumentationNexusResourceBundle
    extends AbstractLogEnabled
    implements NexusResourceBundle
{

    public static final Map<String, String> MEDIA_TYPES;
    static
    {
        final LinkedHashMap<String, String> mediaTypes = new LinkedHashMap<String, String>();
        mediaTypes.put( "html ", "text/html" );
        mediaTypes.put( "html", "text/html" );
        mediaTypes.put( "bmp", "image/bmp" );
        mediaTypes.put( "gif", "image/gif" );
        mediaTypes.put( "jpg", "image/jpeg" );
        mediaTypes.put( "jpeg", "image/jpeg" );
        mediaTypes.put( "png", "image/png" );
        mediaTypes.put( "tif", "image/tiff" );
        mediaTypes.put( "tiff", "image/tiff" );
        mediaTypes.put( "wbmp", "image/wbmp" );
        mediaTypes.put( "jar", "application/zip" );
        mediaTypes.put( "zip", "application/zip" );
        mediaTypes.put( "txt", "text/plain" );
        MEDIA_TYPES = Collections.unmodifiableMap( mediaTypes );
    }

    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> resources = new LinkedList<StaticResource>();

        ZipFile zip = null;
        try
        {
            zip = getZipFile();
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while ( entries.hasMoreElements() )
            {
                ZipEntry entry = entries.nextElement();

                if ( entry.isDirectory() )
                {
                    continue;
                }
                String name = entry.getName();
                if ( !name.startsWith( "docs" ) )
                {
                    continue;
                }

                name = "/" + name;

                URL url = new URL( "jar:file:/" + zip.getName() + "!" + name );
                String path = "/" + getPluginId() + name;
                resources.add( new DefaultStaticResource( url, path, getContentType( name ) ) );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Error discovering plugin documentation", e );
        }
        finally
        {
            if ( zip != null )
            {
                try
                {
                    zip.close();
                }
                catch ( IOException e )
                {
                    getLogger().debug( e.getMessage(), e );
                }
            }
        }

        return resources;
    }

    protected abstract String getPluginId();

    private String getContentType( String name )
    {
        return MEDIA_TYPES.get( FileUtils.getExtension( name ) );
    }

    protected ZipFile getZipFile()
        throws IOException
    {
        URL baseClass = getClass().getClassLoader().getResource( getClass().getName().replace( '.', '/' ) + ".class" );
        assert baseClass.getProtocol().equals( "jar" );

        String jarPath = baseClass.getPath().substring( 6, baseClass.getPath().indexOf( "!" ) );
        return new ZipFile( URLDecoder.decode( jarPath, "UTF-8" ) );
    }

}
