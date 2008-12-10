/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus233PrivilegePermissionTests
    extends AbstractPrivilegeTest
{

    @Test
    public void testCreatePermission()
        throws IOException
    {
        PrivilegeTargetResource privilege = new PrivilegeTargetResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( "repositoryTarget" );
        privilege.setRepositoryTargetId( "testTarget" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "30" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        PrivilegeBaseStatusResource responsePrivilege = this.privUtil.getResourceFromResponse( response );

        // read should succeed (inherited by create)
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }

    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        PrivilegeTargetResource privilege = new PrivilegeTargetResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( "repositoryTarget" );
        privilege.setRepositoryTargetId( "testTarget" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        PrivilegeBaseStatusResource responsePrivilege = this.privUtil.getResourceFromResponse( response );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "31" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // update should fail
        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        PrivilegeTargetResource privilege = new PrivilegeTargetResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( "repositoryTarget" );
        privilege.setRepositoryTargetId( "testTarget" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        PrivilegeBaseStatusResource responsePrivilege = this.privUtil.getResourceFromResponse( response );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give delete
        this.giveUserPrivilege( "test-user", "33" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // read should succeed (inherited by delete)
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

    }
}
