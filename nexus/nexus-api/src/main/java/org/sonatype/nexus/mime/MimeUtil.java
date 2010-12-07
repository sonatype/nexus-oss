package org.sonatype.nexus.mime;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

/**
 * A simple component that hides MIME detection code. Singular methods returns the "best" applicable MIME type, while
 * plural methods returns all detected MIME types in ascending order.
 * 
 * @author cstamas
 */
public interface MimeUtil
{
    String getMimeType( String fileName );

    String getMimeType( File file );

    String getMimeType( URL url );

    String getMimeType( InputStream is );

    Set<String> getMimeTypes( String fileName );

    Set<String> getMimeTypes( File file );

    Set<String> getMimeTypes( URL url );

    Set<String> getMimeTypes( InputStream is );
}
