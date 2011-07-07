package org.sonatype.nexus.artifactorybridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class URLInputStreamRepresentation
    extends OutputRepresentation
{

    private InputStream input;

    private HttpURLConnection urlConn;

    public URLInputStreamRepresentation( String type, InputStream input, HttpURLConnection urlConn )
    {
        super( MediaType.valueOf( type ) );
        if ( input == null )
        {
            throw new NullPointerException( "input" );
        }
        if ( urlConn == null )
        {
            throw new NullPointerException( "urlConn" );
        }
        this.input = input;
        this.urlConn = urlConn;
    }

    @Override
    public void write( OutputStream out )
        throws IOException
    {
        IOUtil.copy( input, out );
        out.flush();
    }

    @Override
    public void release()
    {
        IOUtil.close( input );
        urlConn.disconnect();

        input = null;
        urlConn = null;

        super.release();
    }
}
