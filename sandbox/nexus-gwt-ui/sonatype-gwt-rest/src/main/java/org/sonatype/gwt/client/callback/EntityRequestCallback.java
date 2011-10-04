/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
