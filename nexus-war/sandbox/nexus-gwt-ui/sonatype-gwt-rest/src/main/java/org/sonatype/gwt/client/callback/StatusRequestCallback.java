package org.sonatype.gwt.client.callback;

import org.sonatype.gwt.client.handler.StatusResponseHandler;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class StatusRequestCallback
    extends AbstractRequestCallback
{

    public StatusRequestCallback( StatusResponseHandler handler )
    {
        super( SUCCESS_NO_CONTENT, handler );
    }

    public StatusRequestCallback( int expectedStatusCode, StatusResponseHandler handler )
    {
        super( expectedStatusCode, handler );
    }

    public void onResponseReceived( Request request, Response response )
    {
        if ( getResponseHandler() != null )
        {
            if ( response.getStatusCode() != getExpectedResponseCode() )
            {
                getResponseHandler().onError(
                    request,
                    new RequestException( "Unexpected HTTP response code: " + response.getStatusCode() ) );
            }
            else
            {
                ( (StatusResponseHandler) getResponseHandler() ).onSuccess();
            }
        }
    }

}
