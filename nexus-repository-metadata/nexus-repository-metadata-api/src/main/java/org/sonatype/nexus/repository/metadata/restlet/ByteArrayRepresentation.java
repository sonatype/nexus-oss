package org.sonatype.nexus.repository.metadata.restlet;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class ByteArrayRepresentation
    extends OutputRepresentation
{
    protected byte[] data;

    public ByteArrayRepresentation( MediaType mediaType, byte[] data )
    {
        super( mediaType, data.length );

        this.data = data;
    }

    @Override
    public void write( OutputStream outputStream )
        throws IOException
    {
        IOUtil.copy( data, outputStream );
    }
}
