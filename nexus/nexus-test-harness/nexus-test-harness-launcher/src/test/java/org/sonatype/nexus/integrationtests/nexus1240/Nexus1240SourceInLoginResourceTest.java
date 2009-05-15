/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
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
import org.sonatype.nexus.rest.model.AuthenticationLoginResource;
import org.sonatype.nexus.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;

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
