package org.sonatype.gwt.client.handler;

import com.google.gwt.http.client.Request;

public interface ResponseHandler
{

    void onError( Request request, Throwable error );

}
