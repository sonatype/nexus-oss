package org.sonatype.nexus.proxy.maven.wl.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sonatype.nexus.proxy.maven.wl.EntrySource;

/**
 * Marshals entries into raw streams and other way around.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface EntrySourceMarshaller
{
    void write( EntrySource entrySource, OutputStream outputStream )
        throws IOException;

    EntrySource read( InputStream inputStream )
        throws IOException;
}
