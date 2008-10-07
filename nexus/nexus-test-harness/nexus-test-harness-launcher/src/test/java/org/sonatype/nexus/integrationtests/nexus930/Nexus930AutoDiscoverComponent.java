package org.sonatype.nexus.integrationtests.nexus930;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

/**
 * Test the AutoDiscoverComponent a
 */
public class Nexus930AutoDiscoverComponent
    extends AbstractNexusIntegrationTest
{

    @Test
    public void testInvalidRole()
        throws Exception
    {
        Response response1 = sendMessage( "JUNK-foo-Bar-JUNK", this.getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( response1.getStatus().isClientError() );
        Assert.assertEquals( 404, response1.getStatus().getCode() );

        // restlet client doesn't set accept on gets
//        Response response2 = sendMessage( "JUNK-foo-Bar-JUNK", this.getJsonXStream(), MediaType.APPLICATION_JSON );
//        Assert.assertTrue( response2.getStatus().isClientError() );
//        Assert.assertEquals( 404, response2.getStatus().getCode() );
    }

    @Test
    public void testPlexusResourceRole()
        throws Exception
    {
        List<PlexusComponentListResource> result1 = this.getResult( PlexusResource.class.getName(), this
            .getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( "Expected list larger then 1.", result1.size() > 1 );

     // restlet client doesn't set accept on gets
//        List<PlexusComponentListResource>  result2 = this.getResult( PlexusResource.class.getName(), this.getJsonXStream(), MediaType.APPLICATION_JSON );
//        Assert.assertTrue( "Expected list larger then 1.", result2.size() > 1 );
    }

    private List<PlexusComponentListResource> getResult( String role, XStream xstream, MediaType mediaType )
        throws IOException
    {
        String responseString = this.sendMessage( role, xstream, mediaType ).getEntity().getText();

        System.out.println( "responseString - "+ mediaType +" : "+ responseString );
        
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PlexusComponentListResourceResponse resourceResponse = (PlexusComponentListResourceResponse) representation
            .getPayload( new PlexusComponentListResourceResponse() );

        return (List<PlexusComponentListResource>) resourceResponse.getData();
    }

    private Response sendMessage( String role, XStream xstream, MediaType mediaType )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = "service/local/components/" + role;

        return RequestFacade.sendMessage( serviceURI, Method.GET, representation );
    }

}
