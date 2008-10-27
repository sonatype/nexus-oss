package org.sonatype.nexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;
import org.sonatype.nexus.plugins.rest.StaticResource;

public class StaticResourceRepresentation
    extends OutputRepresentation
{
    private final StaticResource resource;

    public StaticResourceRepresentation( StaticResource resource )
    {
        super( MediaType.valueOf( resource.getContentType() ) );

        setSize( resource.getSize() );

        setAvailable( true );

        this.resource = resource;
    }

    @Override
    public void write( OutputStream outputStream )
        throws IOException
    {
        InputStream is = null;

        try
        {
            is = resource.getInputStream();

            IOUtil.copy( is, outputStream );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

}
