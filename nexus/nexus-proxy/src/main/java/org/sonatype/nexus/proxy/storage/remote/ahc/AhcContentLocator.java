package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.proxy.item.ContentLocator;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.Response.ResponseBuilder;

/**
 * ContentLocator backed by AsyncHttpClient offering multiple strategies: reusable and non-reusable. When reusable. it
 * actually performs HTTP GET on every getContent() invocation.
 * 
 * @author cstamas
 */
public class AhcContentLocator
    implements ContentLocator
{
    private final AsyncHttpClient client;

    private final String itemUrl;

    private final long length;

    private final long lastModified;

    private final String mimeType;

    private final InputStream inputStream;

    public AhcContentLocator( final AsyncHttpClient client, final String itemUrl, final long length,
                              final long lastModified, final String mimeType, final InputStream is )
    {
        this.client = client;
        this.itemUrl = itemUrl;
        this.length = length;
        this.lastModified = lastModified;
        this.mimeType = mimeType;
        this.inputStream = is;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        if ( inputStream != null )
        {
            return inputStream;
        }
        else
        {
            return fetchContent( client, itemUrl );
        }
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public boolean isReusable()
    {
        return inputStream == null;
    }

    public String getItemUrl()
    {
        return itemUrl;
    }

    public long getLength()
    {
        return length;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    // ==

    public static ResponseInputStream fetchContent( final AsyncHttpClient client, final String itemUrl )
        throws IOException
    {
        try
        {
            final PipedOutputStream po = new PipedOutputStream();

            final HeadResponseAsyncHandler hrah = new HeadResponseAsyncHandler( itemUrl, po );

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

    // ==

    public static class HeadResponseAsyncHandler
        implements AsyncHandler<Response>
    {
        private final ResponseBuilder responseBuilder = new ResponseBuilder();

        private final Lock lock = new ReentrantLock();

        private final String itemUrl;

        private final OutputStream output;

        private Throwable t;

        public HeadResponseAsyncHandler( final String itemUrl, final OutputStream os )
        {
            this.itemUrl = itemUrl;

            this.output = os;

            this.lock.lock();
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

            lock.unlock();

            return check();
        }

        @Override
        public STATE onBodyPartReceived( HttpResponseBodyPart bodyPart )
            throws Exception
        {
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
        {
            lock.lock();

            try
            {
                return responseBuilder.build();
            }
            finally
            {
                lock.unlock();
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
                return responseBuilder.build();
            }
        }
    }

    public static class ResponseInputStream
        extends FilterInputStream
    {
        private final HeadResponseAsyncHandler hrah;

        protected ResponseInputStream( final HeadResponseAsyncHandler hrah, final InputStream in )
        {
            super( in );

            this.hrah = hrah;
        }

        public Response getResponse()
            throws ExecutionException, InterruptedException
        {
            return hrah.getResponse();
        }
    }
}
