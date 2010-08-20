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
package org.sonatype.nexus.integrationtests.nexus133;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus133TargetPermissionIT
    extends AbstractPrivilegeTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testCreatePermission()
        throws IOException
    {
        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "testCreatePermission" );
        target.addPattern( ".*testCreatePermission.*" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "45" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        target = this.targetUtil.getResourceFromResponse( response );

        // read should succeed (inherited)
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().useAdminForRequests();

        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "testUpdatePermission" );
        target.addPattern( ".*testUpdatePermission.*" );

        Response response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "47" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // read should succeed (inherited)
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }
    
    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().useAdminForRequests();

        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "testReadPermission" );
        target.addPattern( ".*testReadPermission.*" );

        Response response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "46" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // read should fail
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
     // should work now...
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }
    
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().useAdminForRequests();

        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "testDeletePermission" );
        target.addPattern( ".*testDeletePermission.*" );

        Response response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "48" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // read should succeed (inherited)
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
     // should work now...
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( response.getStatus().getCode(), 204, "Response status: " );

    }

}
