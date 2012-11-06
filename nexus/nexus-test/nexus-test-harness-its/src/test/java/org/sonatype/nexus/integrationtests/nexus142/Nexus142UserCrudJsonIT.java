/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.security.model.CUser;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.usermanagement.PasswordGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CRUD tests for JSON request/response.
 */
public class Nexus142UserCrudJsonIT
    extends AbstractNexusIntegrationTest
{
    protected UserMessageUtil messageUtil;

    public Nexus142UserCrudJsonIT()
    {
        this.messageUtil = new UserMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void createUserTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setFirstName( "Create User" );
        resource.setUserId( "createUser" );
        resource.setStatus( "active" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        // this also validates
        this.messageUtil.createUser( resource );
    }

    @Test
    public void createTestWithPassword()
        throws IOException, ComponentLookupException
    {

        UserResource resource = new UserResource();
        String password = "defaultPassword";
        resource.setFirstName( "Create User" );
        resource.setUserId( "createTestWithPassword" );
        resource.setStatus( "active" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );
        resource.setPassword( password );

        // this also validates
        this.messageUtil.createUser( resource );

        // validate password is correct
        PasswordGenerator pwGenerator = lookup( PasswordGenerator.class );
        String hashedPassword = pwGenerator.hashPassword( password );
        CUser cUser = getSecurityConfigUtil().getCUser( "createTestWithPassword" );
        Assert.assertEquals( cUser.getPassword(), hashedPassword, "Expected hashed passwords to be the same." );

    }

    @Test
    public void listTest()
        throws IOException
    {
        UserResource resource = new UserResource();

        resource.setFirstName( "list Test" );
        resource.setUserId( "listTest" );
        resource.setStatus( "active" );
        resource.setEmail( "listTest@user.com" );
        resource.addRole( "role1" );

        // this also validates
        this.messageUtil.createUser( resource );

        // now that we have at least one element stored (more from other tests, most likely)

        // NEED to work around a GET problem with the REST client
        List<UserResource> users = this.messageUtil.getList();
        getSecurityConfigUtil().verifyUsers( users );

    }

    public void readTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setFirstName( "Read User" );
        resource.setUserId( "readUser" );
        resource.setStatus( "active" );
        resource.setEmail( "read@user.com" );
        resource.addRole( "role1" );

        // this also validates
        this.messageUtil.createUser( resource );

        Response response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.messageUtil.getResourceFromResponse( response );

        Assert.assertEquals( resource.getFirstName(), responseResource.getFirstName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( "active", responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );
    }

    @Test
    public void updateTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setFirstName( "Update User" );
        resource.setUserId( "updateUser" );
        resource.setStatus( "active" );
        resource.setEmail( "updateUser@user.com" );
        resource.addRole( "role1" );

        this.messageUtil.createUser( resource );

        // update the user
        // TODO: add tests that changes the userId
        resource.setFirstName( "Update UserAgain" );
        resource.setUserId( "updateUser" );
        resource.setStatus( "active" );
        resource.setEmail( "updateUser@user2.com" );
        resource.getRoles().clear();
        resource.addRole( "role2" );

        // this validates
        this.messageUtil.updateUser( resource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setFirstName( "Delete User" );
        resource.setUserId( "deleteUser" );
        resource.setStatus( "active" );
        resource.setEmail( "deleteUser@user.com" );
        resource.addRole( "role2" );

        this.messageUtil.createUser( resource );

        // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete User: " + response.getStatus() );
        }

        getSecurityConfigUtil().verifyUsers( new ArrayList<UserResource>() );
    }

}
