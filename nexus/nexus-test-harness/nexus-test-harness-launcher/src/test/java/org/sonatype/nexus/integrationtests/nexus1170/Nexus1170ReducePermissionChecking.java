/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus1170;

import java.io.IOException;
import java.util.List;

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
import org.sonatype.nexus.rest.model.ClientPermission;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class Nexus1170ReducePermissionChecking
    extends AbstractNexusIntegrationTest
{

    public Nexus1170ReducePermissionChecking()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testAdminPrivileges()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        List<ClientPermission> permissions = this.getPermissions();
        
        Assert.assertEquals( 37, permissions.size() );

        for ( ClientPermission clientPermission : permissions )
        {
            Assert.assertEquals( 15, clientPermission.getValue() );
        }
    }

    @Test
    public void testDeploymentUserPrivileges()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        List<ClientPermission> permissions = this.getPermissions();

        Assert.assertEquals( 37, permissions.size() );
        this.checkPermission( permissions, "nexus:*", 0 );
        this.checkPermission( permissions, "nexus:status", 1 );
        this.checkPermission( permissions, "nexus:authentication", 1 );
        this.checkPermission( permissions, "nexus:settings", 0 );
        this.checkPermission( permissions, "nexus:repositories", 1 );
        this.checkPermission( permissions, "nexus:repotemplates", 0 );
        this.checkPermission( permissions, "nexus:repogroups", 1 );
        this.checkPermission( permissions, "nexus:index", 1 );
        this.checkPermission( permissions, "nexus:identify", 1 );
        this.checkPermission( permissions, "nexus:attributes", 0 );
        
        this.checkPermission( permissions, "nexus:cache", 0 );
        this.checkPermission( permissions, "nexus:routes", 0 );
        this.checkPermission( permissions, "nexus:tasks", 0 );
        this.checkPermission( permissions, "nexus:privileges", 0 );
        this.checkPermission( permissions, "nexus:roles", 0 );
        this.checkPermission( permissions, "nexus:users", 0 );
        this.checkPermission( permissions, "nexus:logs", 0 );
        this.checkPermission( permissions, "nexus:configuration", 0 );
        this.checkPermission( permissions, "nexus:feeds", 1 );
        this.checkPermission( permissions, "nexus:targets", 0 );
        
        this.checkPermission( permissions, "nexus:wastebasket", 0 );
        this.checkPermission( permissions, "nexus:artifact", 1 );
        this.checkPermission( permissions, "nexus:repostatus", 1 );
        this.checkPermission( permissions, "nexus:repocontentclasses", 1 );
        this.checkPermission( permissions, "nexus:usersforgotpw", 9 );
        this.checkPermission( permissions, "nexus:usersforgotid", 9 );
        this.checkPermission( permissions, "nexus:usersreset", 0 );
        this.checkPermission( permissions, "nexus:userschangepw", 9 );
        
        this.checkPermission( permissions, "nexus:command", 0 );
        this.checkPermission( permissions, "nexus:repometa", 0 );
        this.checkPermission( permissions, "nexus:tasksrun", 0 );
        this.checkPermission( permissions, "nexus:tasktypes", 0 );
        this.checkPermission( permissions, "nexus:componentscontentclasses", 1 );
        this.checkPermission( permissions, "nexus:componentscheduletypes", 0 );
        this.checkPermission( permissions, "nexus:userssetpw", 0 );
        this.checkPermission( permissions, "nexus:componentrealmtypes", 0 );
        this.checkPermission( permissions, "nexus:componentsrepotypes", 1 );
        
        for ( ClientPermission outPermission : permissions )
        {
            int count = 0;
            for ( ClientPermission inPermission : permissions )
            {
              if(outPermission.getId().equals( inPermission.getId() ))
              {
                  count++;
              }
              if(count > 1)
              {
                  Assert.fail( "Duplicate privilege: "+ outPermission.getId() +" found count: "+ count);
              }
            }
            
        }
       
        
    }

    private void checkPermission( List<ClientPermission> permissions, String permission, int expectedValue )
    {
        for ( ClientPermission clientPermission : permissions )
        {

            if ( clientPermission.getId().equals( permission ) )
            {
                Assert.assertEquals( expectedValue, clientPermission.getValue() );
                return;
            }

        }
        Assert.fail( "Did not find permission: " + permissions );
    }

    private List<ClientPermission> getPermissions()
        throws IOException
    {
        Response response = RequestFacade
            .sendMessage( RequestFacade.SERVICE_LOCAL + "authentication/login", Method.GET );

        String responseText = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        AuthenticationLoginResourceResponse resourceResponse = (AuthenticationLoginResourceResponse) representation
            .getPayload( new AuthenticationLoginResourceResponse() );

        AuthenticationLoginResource resource = resourceResponse.getData();

        return resource.getClientPermissions().getPermissions();
    }
}
