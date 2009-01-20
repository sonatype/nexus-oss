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
package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;

/**
 * CRUD tests for JSON request/response.
 */
public class Nexus156RolesCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    protected RoleMessageUtil messageUtil;

    public Nexus156RolesCrudJsonTests()
    {
        this.messageUtil =
            new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void createRoleTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Create Test Role" );
        resource.setName( "CreateRole" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );
        resource.addPrivilege( "2" );

        this.messageUtil.createRole( resource );
    }
    
    @Test
    public void createRoleWithIdTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Create Test Role With ID" );
        resource.setName( "CreateRoleWithID" );
        resource.setId( "CreateRoleWithID" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );
        resource.addPrivilege( "2" );

        this.messageUtil.createRole( resource );
    }

    @Test
    public void listTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Create Test Role" );
        resource.setName( "ListTestRole" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        // create a role
        this.messageUtil.createRole( resource );

        // now that we have at least one element stored (more from other tests, most likely)

        // NEED to work around a GET problem with the REST client
        List<RoleResource> roles = this.messageUtil.getList();
        SecurityConfigUtil.verifyRoles( roles );

    }

    public void readTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Read Test Role" );
        resource.setName( "ReadRole" );
        resource.setSessionTimeout( 31 );
        resource.addPrivilege( "3" );
        resource.addPrivilege( "4" );
        resource = this.messageUtil.createRole( resource );

        // get the Resource object
        RoleResource responseResource = this.messageUtil.getRole( resource.getId() );

        Assert.assertEquals( resource.getId(), responseResource.getId() );
        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );
    }

    @Test
    public void updateTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Update Test Role" );
        resource.setName( "UpdateRole" );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );

        RoleResource responseResource = this.messageUtil.createRole( resource );

        // update the Role
        // TODO: add tests that changes the Id
        resource.setId( responseResource.getId() );
        resource.setName( "UpdateRole Again" );
        resource.setDescription( "Update Test Role Again" );
        resource.getPrivileges().clear(); // clear the privs
        resource.addPrivilege( "6" );
        resource.setSessionTimeout( 10 );

        Response response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not update Role: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

        Assert.assertEquals( resource.getId(), responseResource.getId() );
        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( resource );
    }

    @Test
    public void deleteTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Delete Test Role" );
        resource.setName( "deleteRole" );
        resource.setSessionTimeout( 1 );
        resource.addPrivilege( "7" );
        resource.addPrivilege( "8" );

        RoleResource responseResource = this.messageUtil.createRole( resource );

        // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Role: " + response.getStatus() );
        }

        // TODO: check if deleted
        Assert.assertNull( SecurityConfigUtil.getCRole( responseResource.getId() ) );
    }

}
