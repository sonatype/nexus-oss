package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.NexusError;
import org.sonatype.nexus.rest.model.NexusErrorResponse;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class PrivilegesMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    private String baseNexusUrl;

    public PrivilegesMessageUtil( XStream xstream, MediaType mediaType, String baseNexusUrl )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
        this.baseNexusUrl = baseNexusUrl;
    }

    public Response sendMessage( Method method, PrivilegeBaseResource resource ) throws IOException
    {
        return this.sendMessage( method, resource, "" );
    }

    public Response sendMessage( Method method, PrivilegeBaseResource resource, String id ) throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String privId = ( method == Method.POST ) ? "" : "/" + id;
        String serviceURI = "service/local/privileges" + privId;

        if ( method == Method.POST )
        {
            PrivilegeResourceRequest requestResponse = new PrivilegeResourceRequest();
            requestResponse.setData( resource );

            // now set the payload
            representation.setPayload( requestResponse );
            System.out.println( method.getName() + ": " + representation.getText() );
        }

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    public PrivilegeBaseStatusResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PrivilegeStatusResourceResponse resourceResponse =
            (PrivilegeStatusResourceResponse) representation.getPayload( new PrivilegeStatusResourceResponse() );

        return (PrivilegeBaseStatusResource) resourceResponse.getData();

    }

    public List<PrivilegeBaseStatusResource> getResourceListFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PrivilegeListResourceResponse resourceResponse =
            (PrivilegeListResourceResponse) representation.getPayload( new PrivilegeListResourceResponse() );

        return resourceResponse.getData();
    }

    public void validateResponseErrorXml( String xml )
    {
        // to trick xstream
        // REMEMBER! You cannot use the XStreamInitializer 1:1 from Server!
        // It does n->1 mapping (maps different types to field data), while the client
        // has to do 1->n mapping (it knows what _will_ 'data' field contain)
        xstream.alias( "data", org.sonatype.nexus.rest.model.PrivilegeListResourceResponse.class );

        NexusErrorResponse errorResponse = (NexusErrorResponse) xstream.fromXML( xml, new NexusErrorResponse() );

        Assert.assertTrue( "Error response is empty.", errorResponse.getErrors().size() > 0 );

        for ( Iterator<NexusError> iter = errorResponse.getErrors().iterator(); iter.hasNext(); )
        {
            NexusError error = (NexusError) iter.next();
            Assert.assertFalse( "Response Error message is empty.", StringUtils.isEmpty( error.getMsg() ) );

        }

    }

}
