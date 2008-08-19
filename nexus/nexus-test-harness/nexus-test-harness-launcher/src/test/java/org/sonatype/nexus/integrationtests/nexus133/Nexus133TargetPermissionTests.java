package org.sonatype.nexus.integrationtests.nexus133;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;

public class Nexus133TargetPermissionTests
    extends AbstractPrivilegeTest
{

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
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "45" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        target = this.targetUtil.getResourceFromResponse( response );

        // read should succeed (inherited)
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

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
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "47" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

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
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "46" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // read should fail
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
     // should work now...
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

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
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( "test-user", "48" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // read should succeed (inherited)
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
     // should work now...
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( "Response status: ", 204, response.getStatus().getCode() );

    }

}
