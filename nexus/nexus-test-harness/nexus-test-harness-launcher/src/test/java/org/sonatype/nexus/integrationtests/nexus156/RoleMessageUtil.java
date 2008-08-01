package org.sonatype.nexus.integrationtests.nexus156;

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
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RoleListResourceResponse;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RoleMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;
    
    private String baseNexusUrl;
    
    public RoleMessageUtil( XStream xstream, MediaType mediaType, String baseNexusUrl )
    {
        this.xstream = xstream;
        this.mediaType = mediaType;
        this.baseNexusUrl = baseNexusUrl;
    }

    public Response sendMessage( Method method, RoleResource resource ) throws IOException
    {
    
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );
    
        String roleId = ( method == Method.POST ) ? "" : "/" + resource.getId();
    
        String serviceURI = "service/local/roles" + roleId;
    
        RoleResourceRequest userRequest = new RoleResourceRequest();
        userRequest.setData( resource );
    
        // now set the payload
        representation.setPayload( userRequest );
    
        return RequestFacade.sendMessage( serviceURI, method, representation );
    }
    
    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<RoleResource> getList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( "service/local/roles" ).getEntity().getText();

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        RoleListResourceResponse resourceResponse =
            (RoleListResourceResponse) representation.getPayload( new RoleListResourceResponse() );

        return resourceResponse.getData();

    }

    public RoleResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );
    
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
    
        // this
        RoleResourceRequest roleResourceRequest =
            (RoleResourceRequest) representation.getPayload( new RoleResourceRequest() );
    
        return roleResourceRequest.getData();
    }
    
    
    
    
}
