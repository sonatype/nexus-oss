package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import com.ning.http.client.Response;

public class ResponseInputStream
    extends FilterInputStream
{
    private final ResponseBlockingAsyncHandler hrah;

    protected ResponseInputStream( final ResponseBlockingAsyncHandler hrah, final InputStream in )
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