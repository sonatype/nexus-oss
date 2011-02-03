package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.Response.ResponseBuilder;

/**
 * An AsyncHandler that introduces new call: getResponse() that will block as long as headers are received and will make
 * you Response available as soon as possible, but still pouring response body into supplied output stream. This handler
 * is meant for situations when the "usual" Future<Response>.get() would not work for you, since a potentially large
 * response body is about to be GETed, but you need headers first and lifting all into memory (huge body) would not
 * work.
 */
public class ResponseBlockingAsyncHandler
    implements AsyncHandler<Response>
{
    private final ResponseBuilder responseBuilder = new ResponseBuilder();

    private final CountDownLatch headersArrived = new CountDownLatch( 1 );

    private final OutputStream output;

    private volatile boolean responseSet;

    private volatile Response response;

    private volatile Throwable t;

    public ResponseBlockingAsyncHandler( final OutputStream os )
    {
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
        responseBuilder.reset();

        responseBuilder.accumulate( responseStatus );

        return STATE.CONTINUE;
    }

    @Override
    public STATE onHeadersReceived( HttpResponseHeaders headers )
        throws Exception
    {
        responseBuilder.accumulate( headers );

        return STATE.CONTINUE;
    }

    @Override
    public STATE onBodyPartReceived( HttpResponseBodyPart bodyPart )
        throws Exception
    {
        // body arrived, flush headers
        if ( !responseSet )
        {
            response = responseBuilder.build();

            responseSet = true;

            headersArrived.countDown();
        }

        bodyPart.writeTo( output );

        return STATE.CONTINUE;
    }

    @Override
    public Response onCompleted()
        throws Exception
    {
        // Counting down to handle error cases too.
        // In "normal" cases, latch is already at 0 here/
        // But in other cases, for example when because of some error onBodyPartReceived() is never called, the caller
        // of getResponse() would remain blocked infinitely.
        // By contract, onCompleted() is always invoked, even in case of errors
        headersArrived.countDown();

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

    /**
     * This method -- unlike Future<Reponse>.get() -- will block only as long, as headers arrive. This is useful for
     * large transfers, to examine headers ASAP, and defer body streaming to it's fine destination and prevent unneeded
     * bandwidth consumption. The response here will contain the very 1st response from server, so status code and
     * headers, but it might be incomplete in case of broken servers sending trailing headers. In that case, the "usual"
     * Future<Response>.get() method will return complete headers, but multiple invocations of getResponse() will always
     * return the 1st cached, probably incomplete one. Note: the response returned by this method will contain
     * everything <em>except</em> the response body itself, so invoking any method like Response.getResponseBodyXXX()
     * will result in error!
     * 
     * @return
     * @throws InterruptedException
     */
    public Response getResponse()
        throws InterruptedException
    {
        // block here as long as headers arrive
        headersArrived.await();

        return response;
    }
}