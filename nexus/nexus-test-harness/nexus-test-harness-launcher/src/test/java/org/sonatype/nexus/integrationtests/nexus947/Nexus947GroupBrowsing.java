package org.sonatype.nexus.integrationtests.nexus947;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class Nexus947GroupBrowsing
    extends AbstractNexusIntegrationTest
{

    @Test
    public void doTest() throws IOException
    {
        List<ContentListResource> items = this.getContentListResource( "public", "/" );
        
        // make sure we have a few items
        Assert.assertTrue( "Expected more then 1 item. ", items.size() > 1 );
        
        // now for a bit more control
        items = this.getContentListResource( "public", "/nexus947/nexus947/3.2.1/");
        
        // exactly 2 items
        Assert.assertEquals( 2, items.size() );
        
        // they are sorted in alpha order, so expect the jar, then the pom
        Assert.assertEquals( "nexus947-3.2.1.jar", items.get( 0 ).getText() );
        Assert.assertEquals( "nexus947-3.2.1.pom", items.get( 1 ).getText() );
    }
    
    @Test
    public void redirectTest() throws IOException
    {
        String uriPart = RequestFacade.SERVICE_LOCAL + "repo_groups/" + "public" + "/content";
        Response response = RequestFacade.sendMessage( uriPart, Method.GET );
        Assert.assertEquals( 301, response.getStatus().getCode() );
        
        Assert.assertTrue(response.getLocationRef().toString().endsWith( uriPart + "/" ));
        
    }

    protected Response getResponse( String groupId, String path )
        throws IOException
    {
        String uriPart = RequestFacade.SERVICE_LOCAL + "repo_groups/" + groupId + "/content" + path;

        return RequestFacade.sendMessage( uriPart, Method.GET );
    }

    protected List<ContentListResource> getContentListResource( String groupId,String path )
        throws IOException
    {
        Response response = this.getResponse( groupId, path );

        String responeText = response.getEntity().getText();
        Assert.assertTrue(
            "Expected sucess: Status was: " + response.getStatus() + "\nResponse:\n" + responeText,
            response.getStatus().isSuccess() );

        XStreamRepresentation representation = new XStreamRepresentation(this.getXMLXStream(), responeText, MediaType.APPLICATION_XML);
        ContentListResourceResponse listRepsonse = (ContentListResourceResponse) representation.getPayload( new ContentListResourceResponse() );
        
        return listRepsonse.getData();
        
    }

}
