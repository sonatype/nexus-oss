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
package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.security.rest.model.UserResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus142UserPermissionIT
    extends AbstractPrivilegeTest
{

    @Test
    public void testCreatePermission()
        throws IOException
    {
        // create a user with anon access

        UserResource user = new UserResource();
        user.setEmail( "tesCreatePermission@foo.org" );
        user.setFirstName( "tesCreatePermission" );
        user.setUserId( "tesCreatePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "38" );

        // print out the users privs
        // this.printUserPrivs( "test-user" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }

    @Test( dependsOnMethods = "testCreatePermission" )
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "tesUpdatePermission@foo.org" );
        user.setFirstName( "tesUpdatePermission" );
        user.setUserId( "tesUpdatePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }

    @Test( dependsOnMethods = "testUpdatePermission" )
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "testReadPermission@foo.org" );
        user.setFirstName( "testReadPermission" );
        user.setUserId( "testReadPermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // read should fail
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }

    @Test( dependsOnMethods = { "testReadPermission" } )
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "testDeletePermission@foo.org" );
        user.setFirstName( "testDeletePermission" );
        user.setUserId( "testDeletePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( response.getStatus().getCode(), 204, "Response status: " );

    }

}
