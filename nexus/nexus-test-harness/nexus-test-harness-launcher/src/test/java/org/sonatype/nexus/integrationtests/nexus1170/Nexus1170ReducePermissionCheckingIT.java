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
package org.sonatype.nexus.integrationtests.nexus1170;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.source.FileConfigurationSource;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.rest.model.AuthenticationLoginResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.security.rest.model.ClientPermission;

public class Nexus1170ReducePermissionCheckingIT
    extends AbstractNexusIntegrationTest
{

    private int getExpectedPrivilegeCount() throws Exception
    {
        NexusConfiguration configuration = container.lookup( NexusConfiguration.class );
        ( ( FileConfigurationSource )configuration.getConfigurationSource()).setConfigurationFile( new File( getBasedir(), "target/plexus-home/nexus-work/conf/nexus.xml" ) );
        configuration.loadConfiguration();
        ConfigurationManager configManager = container.lookup( ConfigurationManager.class, "resourceMerging");

        Set<String> privIds = new HashSet<String>();
        for ( SecurityPrivilege priv : configManager.listPrivileges() )
        {
            for ( CProperty prop : priv.getProperties() )
            {
                if ( prop.getKey().equals( "permission" ) )
                {
                    privIds.add( prop.getValue() );
                }
            }
        }
        return privIds.size();
    }

    public Nexus1170ReducePermissionCheckingIT()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testAdminPrivileges()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        List<ClientPermission> permissions = this.getPermissions();

        Assert.assertEquals( this.getExpectedPrivilegeCount(), permissions.size() );

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

        Assert.assertEquals( this.getExpectedPrivilegeCount(), permissions.size() );
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
        this.checkPermission( permissions, "security:privileges", 0 );
        this.checkPermission( permissions, "security:roles", 0 );
        this.checkPermission( permissions, "security:users", 0 );
        this.checkPermission( permissions, "nexus:logs", 0 );
        this.checkPermission( permissions, "nexus:configuration", 0 );
        this.checkPermission( permissions, "nexus:feeds", 1 );
        this.checkPermission( permissions, "nexus:targets", 0 );

        this.checkPermission( permissions, "nexus:wastebasket", 0 );
        this.checkPermission( permissions, "nexus:artifact", 1 );
        this.checkPermission( permissions, "nexus:repostatus", 1 );
        this.checkPermission( permissions, "security:usersforgotpw", 9 );
        this.checkPermission( permissions, "security:usersforgotid", 9 );
        this.checkPermission( permissions, "security:usersreset", 0 );
        this.checkPermission( permissions, "security:userschangepw", 9 );

        this.checkPermission( permissions, "nexus:command", 0 );
        this.checkPermission( permissions, "nexus:repometa", 0 );
        this.checkPermission( permissions, "nexus:tasksrun", 0 );
        this.checkPermission( permissions, "nexus:tasktypes", 0 );
        this.checkPermission( permissions, "nexus:componentscontentclasses", 1 );
        this.checkPermission( permissions, "nexus:componentscheduletypes", 0 );
        this.checkPermission( permissions, "security:userssetpw", 0 );
        this.checkPermission( permissions, "nexus:componentrealmtypes", 0 );
        this.checkPermission( permissions, "nexus:componentsrepotypes", 1 );
        this.checkPermission( permissions, "security:componentsuserlocatortypes", 0 );

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
