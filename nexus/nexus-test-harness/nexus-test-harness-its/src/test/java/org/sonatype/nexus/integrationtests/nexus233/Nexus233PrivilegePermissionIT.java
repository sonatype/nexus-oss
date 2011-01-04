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
package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus233PrivilegePermissionIT
    extends AbstractPrivilegeTest
{

    @Test
    public void testCreatePermission()
        throws IOException
    {
        PrivilegeResource privilege = new PrivilegeResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( TargetPrivilegeDescriptor.TYPE );
        privilege.setRepositoryTargetId( "testTarget" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "30" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        PrivilegeStatusResource responsePrivilege = this.privUtil.getResourceListFromResponse( response ).get( 0 );

        // read should succeed (inherited by create)
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }

    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        PrivilegeResource privilege = new PrivilegeResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( TargetPrivilegeDescriptor.TYPE );
        privilege.setRepositoryTargetId( "testTarget" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        PrivilegeStatusResource responsePrivilege = this.privUtil.getResourceListFromResponse( response ).get( 0 );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "31" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should fail
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        PrivilegeResource privilege = new PrivilegeResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( TargetPrivilegeDescriptor.TYPE );
        privilege.setRepositoryTargetId( "testTarget" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        PrivilegeStatusResource responsePrivilege = this.privUtil.getResourceListFromResponse( response ).get( 0 );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give delete
        this.giveUserPrivilege( "test-user", "33" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should succeed (inherited by delete)
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // update should fail
        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( response.getStatus().getCode(), 204, "Response status: " );

    }
}
