/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.users;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceStatusResponse;
import org.sonatype.nexus.rest.model.UserRoleResource;
import org.sonatype.nexus.rest.model.UserStatusResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class UserTest
    extends TestCase
{

    protected XStream xstream;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // create and configure XStream for JSON
        xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }
    
    public void testResponse()
    throws Exception
{
    XStreamRepresentation representation = new XStreamRepresentation( xstream , "", MediaType.APPLICATION_JSON );
    
    UserResourceStatusResponse response = new UserResourceStatusResponse();
    
    UserStatusResource resource = new UserStatusResource();
    
    resource.setUserId( "testuser" );
    resource.setName( "johnny test" );
    resource.setEmail( "test@email.com" );
    resource.setStatus( "active" );

    UserRoleResource role = new UserRoleResource();
    role.setRoleId( "roleid" );
    role.setRoleName( "rolename" );
    
    resource.addRole( role );
    
    response.setData( resource );
    
    representation.setPayload( response );
    
    assertEquals( "{\"data\":{\"userId\":\"testuser\",\"name\":\"johnny test\",\"status\":\"active\",\"email\":\"test@email.com\"," +
            "\"roles\":[{\"roleId\":\"roleid\",\"roleName\":\"rolename\"}]}}", 
            representation.getText() );
}

    public void testRequest()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"userId\":null,\"name\":\"johnny test\",\"email\":\"test@email.com\",\"status\":\"active\"," +
            "\"password\":\"mypassword\",\"roles\":[{\"roleId\":\"roleid\",\"roleName\":\"rolename\",\"@class\":\"org.sonatype.nexus.rest.model.UserRoleResource\"}]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );
        
        UserResourceRequest request = ( UserResourceRequest ) representation.getPayload( new UserResourceRequest() );

        assert request.getData().getUserId() == null;
        assert request.getData().getName().equals( "johnny test" );
        assert request.getData().getEmail().equals( "test@email.com" );
        assert request.getData().getPassword().equals( "mypassword" );
        assert request.getData().getStatus().equals( "active" );
        assert request.getData().getRoles().size() == 1;
        
        UserRoleResource role = ( UserRoleResource ) request.getData().getRoles().get( 0 );
        
        assert role != null;
        
        assert role.getRoleId().equals( "roleid" );
        assert role.getRoleName().equals( "rolename" );
    }
}
