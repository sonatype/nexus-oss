package org.sonatype.nexus.integrationtests.nexus2490;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigDTO;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigRequest;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class Nexus2490LvoConfigCheck
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testConfiguration()
        throws Exception
    {
        updateConfig( true );
        Assert.assertTrue( isEnabled() );
        updateConfig( false );
        Assert.assertFalse( isEnabled() );
        updateConfig( true );
        Assert.assertTrue( isEnabled() );
        updateConfig( false );
        Assert.assertFalse( isEnabled() );
    }

    private void updateConfig( boolean enabled )
        throws Exception
    {
        XStreamRepresentation representation = new XStreamRepresentation(
            getJsonXStream(),
            "",
            MediaType.APPLICATION_JSON );

        LvoConfigRequest request = new LvoConfigRequest();

        LvoConfigDTO dto = new LvoConfigDTO();
        dto.setEnabled( enabled );
        request.setData( dto );

        representation.setPayload( request );

        Assert.assertTrue( RequestFacade
            .sendMessage( "service/local/lvo_config", Method.PUT, representation ).getStatus().isSuccess() );
    }

    private boolean isEnabled()
        throws Exception
    {
        Response response = RequestFacade.doGetRequest( "service/local/lvo_config" );

        if ( response.getStatus().isSuccess() )
        {
            XStreamRepresentation representation = new XStreamRepresentation( getXMLXStream(), response
                .getEntity().getText(), MediaType.APPLICATION_XML );

            LvoConfigResponse resp = (LvoConfigResponse) representation.getPayload( new LvoConfigResponse() );
            
            return resp.getData().isEnabled();
        }
        
        Assert.fail( "Message not handles properly" );
        return false;
    }
}
