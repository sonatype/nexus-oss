/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.rest;

import java.io.StringWriter;
import java.io.Writer;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class TestClient
{
    private Response response;

    public String request( String uri )
        throws Exception
    {
        Request request = new Request();

        request.setResourceRef( uri );

        request.setMethod( Method.GET );

        Client client = new Client( Protocol.HTTP );

        response = client.handle( request );

        Writer writer = new StringWriter();

        if ( response.getStatus().isSuccess() )
        {
            response.getEntity().write( writer );

            return writer.toString();
        }
        else
        {
            return null;
        }
    }

    public Response getLastResponse()
    {
        return response;
    }
}
