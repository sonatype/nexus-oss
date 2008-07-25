package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class UserMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;
    
    private String baseNexusUrl;
    
    public UserMessageUtil( XStream xstream, MediaType mediaType, String baseNexusUrl )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
        this.baseNexusUrl = baseNexusUrl;
    }

    public Response sendMessage( Method method, UserResource resource )
    {
    
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );
    
        String userId = ( method == Method.POST ) ? "" : "/" + resource.getUserId();
    
        String serviceURI = this.baseNexusUrl + "service/local/users" + userId;
        System.out.println( "serviceURI: " + serviceURI );
    
        Request request = new Request();
    
        request.setResourceRef( serviceURI );
    
        request.setMethod( method );
    
        UserResourceRequest userRequest = new UserResourceRequest();
        userRequest.setData( resource );
    
        // now set the payload
        representation.setPayload( userRequest );
        request.setEntity( representation );
    
        Client client = new Client( Protocol.HTTP );
    
        return client.handle( request );
    }

    public UserResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );
    
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
    
        // this
        UserResourceRequest resourceResponse =
            (UserResourceRequest) representation.getPayload( new UserResourceRequest() );
    
        return resourceResponse.getData();
    }

}
