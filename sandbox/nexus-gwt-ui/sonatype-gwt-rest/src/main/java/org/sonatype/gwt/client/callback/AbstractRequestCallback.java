package org.sonatype.gwt.client.callback;

import org.sonatype.gwt.client.handler.ResponseHandler;

import com.google.gwt.http.client.Request;

public abstract class AbstractRequestCallback
    implements RestRequestCallback
{
    private int expectedResponseCode;

    private ResponseHandler handler;

    public AbstractRequestCallback( int expectedResponseCode, ResponseHandler handler )
    {
        super();

        this.expectedResponseCode = expectedResponseCode;

        this.handler = handler;
    }

    public int getExpectedResponseCode()
    {
        return expectedResponseCode;
    }

    public void onError( Request request, Throwable error )
    {
        if ( getResponseHandler() != null )
        {
            getResponseHandler().onError( request, error );
        }
    }

    public ResponseHandler getResponseHandler()
    {
        return handler;
    }

}
