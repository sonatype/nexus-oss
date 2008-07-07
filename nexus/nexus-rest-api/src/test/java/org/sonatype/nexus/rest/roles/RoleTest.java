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
package org.sonatype.nexus.rest.roles;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RoleContainedPrivilegeResource;
import org.sonatype.nexus.rest.model.RoleContainedRoleResource;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class RoleTest
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
        
        RoleResourceResponse response = new RoleResourceResponse();
        
        RoleResource resource = new RoleResource();
        
        resource.setDescription( "This is a test role" );
        resource.setId( "somerole" );
        resource.setName( "Test Role" );
        resource.setSessionTimeout( 50 );

        RoleContainedRoleResource role = new RoleContainedRoleResource();
        role.setId( "roleid" );
        role.setName( "rolename" );
        
        resource.addRole( role );
        
        RoleContainedPrivilegeResource priv = new RoleContainedPrivilegeResource();
        priv.setId( "privid" );
        priv.setName( "privname" );
        
        resource.addPrivilege( priv );
        
        response.setData( resource );
        
        representation.setPayload( response );
        
        assertEquals( "{\"data\":{\"id\":\"somerole\",\"name\":\"Test Role\",\"description\":\"This is a test role\",\"sessionTimeout\":50," +
        		"\"roles\":[{\"id\":\"roleid\",\"name\":\"rolename\"}],\"privileges\":[{\"id\":\"privid\",\"name\":\"privname\"}]}}", 
        		representation.getText() );
    }

    public void testRequest()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"Test Role\",\"description\":\"This is a test role\",\"sessionTimeout\":50," +
            "\"roles\":[{\"id\":\"roleid\",\"name\":\"rolename\",\"@class\":\"org.sonatype.nexus.rest.model.RoleContainedRoleResource\"}]," +
            "\"privileges\":[{\"id\":\"privid\",\"name\":\"privname\",\"@class\":\"org.sonatype.nexus.rest.model.RoleContainedPrivilegeResource\"}]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );
        
        RoleResourceRequest request = ( RoleResourceRequest ) representation.getPayload( new RoleResourceRequest() );

        assert request.getData().getId() == null;
        assert request.getData().getName().equals( "Test Role" );
        assert request.getData().getDescription().equals( "This is a test role" );
        assert request.getData().getSessionTimeout() == 50;
        assert request.getData().getRoles().size() == 1;
        
        RoleContainedRoleResource role = ( RoleContainedRoleResource ) request.getData().getRoles().get( 0 );
        
        assert role != null;
        assert role.getId().equals( "roleid" );
        assert role.getName().equals( "rolename" );
        
        assert request.getData().getPrivileges().size() == 1;
        
        RoleContainedPrivilegeResource priv = ( RoleContainedPrivilegeResource ) request.getData().getPrivileges().get( 0 );
        
        assert priv != null;
        
        assert priv.getId().equals( "privid" );
        assert priv.getName().equals( "privname" );
    }
}
