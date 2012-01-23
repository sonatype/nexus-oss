/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
