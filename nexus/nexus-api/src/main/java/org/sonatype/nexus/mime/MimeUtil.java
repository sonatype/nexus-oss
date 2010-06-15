package org.sonatype.nexus.mime;

import java.io.File;
import java.net.URL;

public interface MimeUtil
{
    String getMimeType( String fileName );
    
    String getMimeType( File file );

    String getMimeType( URL url );
}
