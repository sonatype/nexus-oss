package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.mime.MimeUtil;

public abstract class AbstractDocumentationNexusResourceBundle
    extends AbstractLogEnabled
    implements NexusResourceBundle
{

    @Requirement
    private MimeUtil mimeUtil;

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

                URL url = new URL( "jar:file:" + zip.getName() + "!" + name );
                String path = "/" + getUrlSnippet() + name;
                resources.add( new DefaultStaticResource( url, path, mimeUtil.getMimeType( name ) ) );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Discovered documentation for: '" + getPluginId() + "': " + resources.toString() );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Error discovering plugin documentation " + getPluginId(), e );
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

    public abstract String getPluginId();

    public abstract String getUrlSnippet();

    protected ZipFile getZipFile()
        throws IOException
    {
        return getZipFile( getClass() );
    }

    protected ZipFile getZipFile( final Class<?> clazz )
        throws IOException, UnsupportedEncodingException
    {
        URL baseClass = clazz.getClassLoader().getResource( clazz.getName().replace( '.', '/' ) + ".class" );
        assert baseClass.getProtocol().equals( "jar" );

        String jarPath = baseClass.getPath().substring( 5, baseClass.getPath().indexOf( "!" ) );
        return new ZipFile( URLDecoder.decode( jarPath, "UTF-8" ) );
    }

}
