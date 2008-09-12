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
