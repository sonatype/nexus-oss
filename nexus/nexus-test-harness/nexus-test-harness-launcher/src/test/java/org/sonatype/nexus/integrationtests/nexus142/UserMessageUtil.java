package org.sonatype.nexus.integrationtests.nexus142;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
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
    
    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<UserResource> getList()
        throws IOException
    {
        String serviceURI = this.baseNexusUrl + "service/local/users";
        System.out.println( "serviceURI: " + serviceURI );

        URL serviceURL = new URL( serviceURI );

        InputStream is = serviceURL.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int readChar = -1;
        while ( ( readChar = is.read() ) != -1 )
        {
            out.write( readChar );
        }

        String responseText = out.toString();
        System.out.println( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        UserListResourceResponse resourceResponse =
            (UserListResourceResponse) representation.getPayload( new UserListResourceResponse() );

        return resourceResponse.getData();

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
