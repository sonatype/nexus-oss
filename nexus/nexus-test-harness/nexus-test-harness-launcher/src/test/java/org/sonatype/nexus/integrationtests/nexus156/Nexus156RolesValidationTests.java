/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

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
 * Extra CRUD validation tests.
 */
public class Nexus156RolesValidationTests extends AbstractNexusIntegrationTest
{
    
    protected RoleMessageUtil messageUtil;

    public Nexus156RolesValidationTests()
    {
        this.messageUtil =
            new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void roleWithNoPrivsTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "roleWithNoPrivsTest" );
        resource.setName( "roleWithNoPrivsTest" );
        resource.setSessionTimeout( 30 );
//        resource.addPrivilege( "priv1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void roleWithNoName()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "roleWithNoName" );
//        resource.setName( "roleWithNoName" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void duplicateIdTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "duplicateIdTest" );
        resource.setName( "duplicateIdTest" );
        resource.setId( "duplicateIdTest" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        // create
        resource = this.messageUtil.createRole( resource );
        Assert.assertEquals( "duplicateIdTest", resource.getId() );

        // update
        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() +"New Id: "+ this.messageUtil.getResourceFromResponse( response ).getId() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void createWithNoTimeout()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "roleWithNoName" );
        resource.setName( "roleWithNoName" );
//        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void createRecursiveContainment()
        throws IOException
    {
        RoleResource resourceA = new RoleResource();
        resourceA.setName( "recursive1" );
        resourceA.setSessionTimeout( 60 );
        resourceA.addPrivilege( "1" );
        
        Response response = this.messageUtil.sendMessage( Method.POST, resourceA );
        
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should have been created: " + response.getStatus() );
        }
        
        // get the Resource object
        RoleResource responseResourceA = this.messageUtil.getResourceFromResponse( response );
        
        RoleResource resourceB = new RoleResource();
        resourceB = new RoleResource();
        resourceB.setName( "recursive2" );
        resourceB.setSessionTimeout( 60 );
        resourceB.addRole( responseResourceA.getId() );
        
        response = this.messageUtil.sendMessage( Method.POST, resourceB );
        
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should have been created: " + response.getStatus() );
        }
        
        // get the Resource object
        RoleResource responseResourceB = this.messageUtil.getResourceFromResponse( response );
        
        RoleResource resourceC = new RoleResource();
        resourceC = new RoleResource();
        resourceC.setName( "recursive3" );
        resourceC.setSessionTimeout( 60 );
        resourceC.addRole( responseResourceB.getId() );
        
        response = this.messageUtil.sendMessage( Method.POST, resourceC );
        
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should have been created: " + response.getStatus() );
        }
        
        // get the Resource object
        RoleResource responseResourceC = this.messageUtil.getResourceFromResponse( response );
        
        resourceA.setId( responseResourceA.getId() );
        resourceA.getRoles().clear();
        resourceA.addRole( responseResourceC.getId() );
        
        response = this.messageUtil.sendMessage( Method.PUT, resourceA );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void updateValidationTests() throws IOException
    {
        RoleResource resource = new RoleResource();

        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );

        resource.setId( responseResource.getId() );

        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( resource );

        
        /*
         * NO Name
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( null );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );


        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );        
        
        
        
        /*
         * NO Privs
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setSessionTimeout( 99999 );
        resource.getPrivileges().clear();


        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
        /*
         * INVALID Privs
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setSessionTimeout( 99999 );
        resource.getPrivileges().clear();
        resource.getPrivileges().add( "junk" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
     
        /*
         * Update Id
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setId( "NEW-ID-WILL-FAIL" );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        // expect a 404
        Assert.assertEquals( 404, response.getStatus().getCode() );
        
        
    }
    
    
}
