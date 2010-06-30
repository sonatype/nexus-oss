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
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.mime.MimeUtil;

public abstract class AbstractDocumentationNexusResourceBundle
    implements NexusDocumentationBundle
{
    @Requirement
    private Logger logger;

    @Requirement
    private MimeUtil mimeUtil;

    protected Logger getLogger()
    {
        return logger;
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

                URL url = new URL( "jar:file:" + zip.getName() + "!" + name );

                // to lessen clash possibilities, this way only within single plugin may be clashes, but one
                // plugin is usually developed by one team or one user so this is okay
                // system-wide clashes are much harder to resolve
                String path = "/" + getPluginId() + "/" + getUrlSnippet() + name;

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

    public String getPathPrefix()
    {
        return getUrlSnippet();
    }

    public abstract String getDescription();

    public abstract String getPluginId();

    /**
     * Deprecated, but left in place because old plugins still rely on this.
     * 
     * @return
     * @deprecated use getPathPrefix() method.
     */
    @Deprecated
    protected String getUrlSnippet()
    {
        return "default";
    }

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
