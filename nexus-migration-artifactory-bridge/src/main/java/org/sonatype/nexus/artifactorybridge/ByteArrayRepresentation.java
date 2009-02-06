package org.sonatype.nexus.artifactorybridge;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class ByteArrayRepresentation
    extends OutputRepresentation
{

    private byte[] bytes;

    public ByteArrayRepresentation( String type, byte[] bytes )
    {
        super( MediaType.valueOf( type ) );
        if ( bytes == null )
        {
            throw new NullPointerException( "bytes" );
        }
        this.bytes = bytes;
    }

    @Override
    public void write( OutputStream out )
        throws IOException
    {
        try
        {
            IOUtil.copy( bytes, out );
        }
        finally
        {
            bytes = null;
        }
    }

}
