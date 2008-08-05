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
        target = this.targetUtil.getResourceFromResponse( response );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "45" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

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

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "testUpdatePermission" );
        target.addPattern( ".*testUpdatePermission.*" );

        Response response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        target = this.targetUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        target.setName( "tesUpdatePermission2" );
        response = this.targetUtil.sendMessage( Method.PUT, target );
        System.out.println( "PROBLEM: " + this.userUtil.getUser( "test-user" ) );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "47" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.targetUtil.sendMessage( Method.PUT, target );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.targetUtil.sendMessage( Method.GET, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // update should fail
        response = this.targetUtil.sendMessage( Method.POST, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.targetUtil.sendMessage( Method.DELETE, target );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }

}
