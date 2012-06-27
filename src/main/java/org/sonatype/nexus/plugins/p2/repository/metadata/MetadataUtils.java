package org.sonatype.nexus.plugins.p2.repository.metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Utilities to create DOM out of metadata items.
 * 
 * @author cstamas
 */
public class MetadataUtils
{
    private static final String XPP3DOM_KEY = "p2.metadata.dom";

    private MetadataUtils()
    {
        // static utils here
    }

    /**
     * Method that will create the DOM for given P2 metadata file. It handles files like "artifacts.xml" and
     * "content.xml", but also thier JAR counterparts, like "artifacts.jar" and "content.jar" by cranking them up,
     * getting the entry with same name but with modified extension to ".xml".
     * 
     * @param item
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static Xpp3Dom getMetadataXpp3Dom( final StorageFileItem item )
        throws IOException, XmlPullParserException
    {
        if ( item.getItemContext().containsKey( XPP3DOM_KEY ) )
        {
            return (Xpp3Dom) item.getItemContext().get( XPP3DOM_KEY );
        }
        else
        {
            final Xpp3Dom dom;
            if ( item.getName().endsWith( ".jar" ) )
            {
                dom = parseJarItem( item, item.getName().replace( ".jar", ".xml" ) );
            }
            else if ( item.getName().endsWith( ".xml" ) )
            {
                dom = parseXmlItem( item );
            }
            else
            {
                throw new IOException( "Cannot parse the DOM for metadata in item " + item.getRepositoryItemUid() );
            }
            // TODO: we do not put it, to get it GCed after consumed
            // item.getItemContext().put( XPP3DOM_KEY, dom );
            return dom;
        }
    }

    // ==

    private static Xpp3Dom parseXmlItem( final StorageFileItem item )
        throws IOException, XmlPullParserException
    {
        final InputStream is = item.getInputStream();
        try
        {
            return Xpp3DomBuilder.build( new XmlStreamReader( is ) );
        }
        finally
        {
            is.close();
        }
    }

    private static Xpp3Dom parseJarItem( final StorageFileItem item, final String jarPath )
        throws IOException, XmlPullParserException
    {
        final File file = File.createTempFile( "p2file", "zip" );
        try
        {
            final InputStream is = item.getInputStream();
            try
            {
                FileUtils.copyStreamToFile( new RawInputStreamFacade( is ), file );
                final ZipFile z = new ZipFile( file );
                try
                {
                    final ZipEntry ze = z.getEntry( jarPath );
                    if ( ze == null )
                    {
                        throw new LocalStorageException( "Corrupted P2 metadata jar " + jarPath );
                    }
                    final InputStream zis = z.getInputStream( ze );
                    return Xpp3DomBuilder.build( new XmlStreamReader( zis ) );
                }
                finally
                {
                    z.close();
                }
            }
            finally
            {
                is.close();
            }
        }
        finally
        {
            file.delete();
        }
    }
}
