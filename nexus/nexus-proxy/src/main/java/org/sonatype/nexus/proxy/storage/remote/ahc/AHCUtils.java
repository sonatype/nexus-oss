package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.util.DateUtil;
import com.ning.http.util.DateUtil.DateParseException;

public class AHCUtils
{
    public static ResponseInputStream fetchContent( final AsyncHttpClient client, final String itemUrl )
        throws IOException
    {
        try
        {
            final PipedOutputStream po = new PipedOutputStream();

            final ResponseBlockingAsyncHandler hrah = new ResponseBlockingAsyncHandler( itemUrl, po );

            client.prepareGet( itemUrl ).execute( hrah );

            PipedInputStream pi = new PipedInputStream( po );

            return new ResponseInputStream( hrah, pi );
        }
        catch ( Exception e )
        {
            if ( e instanceof IOException )
            {
                throw (IOException) e;
            }
            else
            {
                throw new IOException( e );
            }
        }
    }

    public static long getLastModified( Response response )
    {
        try
        {
            return DateUtil.parseDate( response.getHeader( "last-modified" ) ).getTime();
        }
        catch ( DateParseException e )
        {
            // neglect
            return System.currentTimeMillis();
        }
    }

    public static boolean isAnyOfTheseStatusCodes( final Response response, int... codes )
    {
        for ( int code : codes )
        {
            if ( code == response.getStatusCode() )
            {
                return true;
            }
        }

        return false;
    }
}
