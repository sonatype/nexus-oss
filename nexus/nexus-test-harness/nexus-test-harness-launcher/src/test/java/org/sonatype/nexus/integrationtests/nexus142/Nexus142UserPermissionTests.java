package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.UserResource;

public class Nexus142UserPermissionTests
    extends AbstractPrivilegeTest
{

    @Test
    public void testCreatePermission()
        throws IOException
    {
        // create a user with anon access

        UserResource user = new UserResource();
        user.setEmail( "tesCreatePermission@foo.org" );
        user.setName( "tesCreatePermission" );
        user.setUserId( "tesCreatePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "38" );

        // print out the users privs
        this.printUserPrivs( "test-user" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "tesUpdatePermission@foo.org" );
        user.setName( "tesUpdatePermission" );
        user.setUserId( "tesUpdatePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give update
        this.giveUserPrivilege( "test-user", "40" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }
    
    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "testReadPermission@foo.org" );
        user.setName( "testReadPermission" );
        user.setUserId( "testReadPermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give read
        this.giveUserPrivilege( "test-user", "39" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...

        // update user
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "testDeletePermission@foo.org" );
        user.setName( "testDeletePermission" );
        user.setUserId( "testDeletePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give delete
        this.giveUserPrivilege( "test-user", "41" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...

        // update user
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

    }

}
