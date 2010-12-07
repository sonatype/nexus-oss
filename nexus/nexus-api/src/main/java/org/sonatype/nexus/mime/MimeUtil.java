package org.sonatype.nexus.mime;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

public interface MimeUtil
{
    String getMimeType( String fileName );

    String getMimeType( File file );

    String getMimeType( URL url );

    Set<String> getMimeTypes( String fileName );

    Set<String> getMimeTypes( File file );

    Set<String> getMimeTypes( URL url );

    Set<String> getMimeTypes( InputStream is );
}
