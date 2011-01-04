/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus2490;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigDTO;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigRequest;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2490LvoConfigCheckIT
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
