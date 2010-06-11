package org.sonatype.nexus.plugins.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

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

        ZipInputStream zip = null;
        try
        {
            zip = getJarInputStream();
            for ( ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry() )
            {
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

                DefaultStaticResource resource =
                    new DefaultStaticResource( getClass().getResource( name ), "/" + getPluginId() + name,
                                               getContentType( name ) );
                resources.add( resource );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Error discovering plugin documentation", e );
        }
        finally
        {
            IOUtil.close( zip );
        }

        return resources;
    }

    protected abstract String getPluginId();

    private String getContentType( String name )
    {
        return MEDIA_TYPES.get( FileUtils.getExtension( name ) );
    }

    protected ZipInputStream getJarInputStream()
        throws IOException
    {
        URL baseClass = getClass().getClassLoader().getResource( getClass().getName().replace( '.', '/' ) + ".class" );
        assert baseClass.getProtocol().equals( "jar" );

        String jarPath = baseClass.getPath().substring( 6, baseClass.getPath().indexOf( "!" ) );
        return new ZipInputStream( new FileInputStream( URLDecoder.decode( jarPath, "UTF-8" ) ) );
    }

}
