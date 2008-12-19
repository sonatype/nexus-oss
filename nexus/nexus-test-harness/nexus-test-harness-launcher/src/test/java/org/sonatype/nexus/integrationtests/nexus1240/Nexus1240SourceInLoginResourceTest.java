package org.sonatype.nexus.integrationtests.nexus1240;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.AuthenticationClientPermissions;
import org.sonatype.nexus.rest.model.AuthenticationLoginResource;
import org.sonatype.nexus.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class Nexus1240SourceInLoginResourceTest
    extends AbstractNexusIntegrationTest
{
    public Nexus1240SourceInLoginResourceTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void sourceInLoginResourceTest() throws IOException
    {     
        AuthenticationClientPermissions clientPermissions = this.getPermissions();
        
        Assert.assertEquals( "default", clientPermissions.getLoggedInUserSource() );
    }

    private AuthenticationClientPermissions getPermissions()
        throws IOException
    {
        Response response = RequestFacade
            .sendMessage( RequestFacade.SERVICE_LOCAL + "authentication/login", Method.GET );

        Assert.assertTrue( "Status: "+ response.getStatus(), response.getStatus().isSuccess()  );
        
        String responseText = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        AuthenticationLoginResourceResponse resourceResponse = (AuthenticationLoginResourceResponse) representation
            .getPayload( new AuthenticationLoginResourceResponse() );

        AuthenticationLoginResource resource = resourceResponse.getData();

        return resource.getClientPermissions();
    }

}
