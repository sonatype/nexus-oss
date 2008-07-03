package org.sonatype.gwt.client.callback;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Representation;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class EntityRequestCallback
    extends AbstractRequestCallback
{

    public EntityRequestCallback( EntityResponseHandler handler )
    {
        super( SUCCESS_OK, handler );
    }

    public EntityRequestCallback( int expectedStatusCode, EntityResponseHandler handler )
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
                ( (EntityResponseHandler) getResponseHandler() ).onSuccess( new Representation( response ) );
            }
        }
    }

}
