package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.Response.ResponseBuilder;

/**
 * An AsyncHandler that introduces new call: getResponse() that will block as long as headers are received and will
 * make you Response available as soon as possible, but still pouring response body into supplied output stream.
 * This handler is meant for situations when the "usual" Future<Response>.get() would not work for you, since a
 * potentially large response body is about to be GETed, but you need headers first and lifting all into memory
 * (huge body) would not work.
 */
public class ResponseBlockingAsyncHandler
    implements AsyncHandler<Response>
{
    private final ResponseBuilder responseBuilder = new ResponseBuilder();

    private final BlockingQueue<Response> exchanger = new ArrayBlockingQueue<Response>( 1 );

    private final String itemUrl;

    private final OutputStream output;

    private volatile boolean responseSet;

    private Response response;

    private Throwable t;

    public ResponseBlockingAsyncHandler( final String itemUrl, final OutputStream os )
    {
        this.itemUrl = itemUrl;

        this.output = os;

        this.responseSet = false;
    }

    @Override
    public void onThrowable( Throwable t )
    {
        this.t = t;
    }

    @Override
    public STATE onStatusReceived( HttpResponseStatus responseStatus )
        throws Exception
    {
        responseBuilder.accumulate( responseStatus );

        if ( responseStatus.getStatusCode() == 200 )
        {
            return STATE.CONTINUE;
        }
        else
        {
            t =
                new IOException( String.format( "Coult not perform GET against Url %s, unexpected response is %s",
                    itemUrl, responseStatus.getStatusText() ) );

            return STATE.ABORT;
        }
    }

    @Override
    public STATE onHeadersReceived( HttpResponseHeaders headers )
        throws Exception
    {
        responseBuilder.accumulate( headers );

        return check();
    }

    @Override
    public STATE onBodyPartReceived( HttpResponseBodyPart bodyPart )
        throws Exception
    {
        // body arrived, flush headers
        if ( !responseSet )
        {
            // sending out very 1st response
            exchanger.put( responseBuilder.build() );

            responseSet = true;
        }

        bodyPart.writeTo( output );

        return check();
    }

    protected STATE check()
        throws IOException
    {
        if ( t == null )
        {
            return STATE.CONTINUE;
        }
        else
        {
            return STATE.ABORT;
        }
    }

    public Response getResponse()
        throws InterruptedException
    {
        if ( response != null )
        {
            return response;
        }
        else
        {
            response = exchanger.take();

            return response;
        }
    }

    @Override
    public Response onCompleted()
        throws Exception
    {
        output.close();

        if ( t != null )
        {
            throw new IOException( t );
        }
        else
        {
            // sending out current response
            return responseBuilder.build();
        }
    }
}