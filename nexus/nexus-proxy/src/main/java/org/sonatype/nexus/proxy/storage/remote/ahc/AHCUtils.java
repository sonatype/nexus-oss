package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.BodyDeferringAsyncHandler;
import com.ning.http.client.BodyDeferringAsyncHandler.BodyDeferringInputStream;
import com.ning.http.client.Response;
import com.ning.http.util.DateUtil;
import com.ning.http.util.DateUtil.DateParseException;

public class AHCUtils
{
    public static BodyDeferringInputStream fetchContent( final AsyncHttpClient client, final String itemUrl )
        throws IOException
    {
        try
        {
            final PipedOutputStream po = new PipedOutputStream();

            final PipedInputStream pi = new PipedInputStream( po );

            final BodyDeferringAsyncHandler hrah = new BodyDeferringAsyncHandler( po );

            Future<Response> f = client.prepareGet( itemUrl ).execute( hrah );

            return new BodyDeferringInputStream( f, hrah, pi );
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

    public static long getContentLength( final Response response, final long defaultValue )
    {
        final String contentLengthStr = response.getHeader( "content-length" );

        if ( null != contentLengthStr )
        {
            try
            {
                return Long.parseLong( contentLengthStr );
            }
            catch ( NumberFormatException e )
            {
                // neglect
                return defaultValue;
            }
        }
        else
        {
            return defaultValue;
        }

    }

    public static long getLastModified( final Response response, final long defaultValue )
    {
        final String lastModifiedStr = response.getHeader( "last-modified" );

        if ( null != lastModifiedStr )
        {
            try
            {
                return DateUtil.parseDate( lastModifiedStr ).getTime();
            }
            catch ( DateParseException e )
            {
                // neglect
                return defaultValue;
            }
        }
        else
        {
            return defaultValue;
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
