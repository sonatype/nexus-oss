/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.jsecurity.realms.tools;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;

public class ObjectClonerTest
    extends PlexusTestCase
{
    public void testUserClone()
        throws Exception
    {
        CUser user = new CUser();
        user.setEmail( "email" );
        user.setId( "id" );
        user.setName( "name" );
        user.setPassword( "password" );
        user.setStatus( "status" );
        
        List<String> roles = new ArrayList<String>();
        
        roles.add( "role1" );
        roles.add( "role2" );
        
//        user.setRoles( roles );
        
        SecurityUser cloned = new SecurityUser( user );
        
        assertTrue( cloned != null );
        assertTrue( cloned != user );
        assertTrue( cloned.getEmail().equals( user.getEmail() ) );
        assertTrue( cloned.getId().equals( user.getId() ) );
        assertTrue( cloned.getName().equals( user.getName() ) );
        assertTrue( cloned.getPassword().equals( user.getPassword() ) );
        assertTrue( cloned.getStatus().equals( user.getStatus() ) );
        
//        assertTrue( cloned.getRoles() != null );
//        assertTrue( cloned.getRoles() != user.getRoles() );
//        assertTrue( cloned.getRoles().size() == 2 );
//        assertTrue( cloned.getRoles().get( 0 ).equals( "role1" ) );
//        assertTrue( cloned.getRoles().get( 1 ).equals( "role2" ) );
    }
    
    public void testRoleClone()
        throws Exception
    {
        CRole role = new CRole();
        role.setDescription( "description" );
        role.setId( "id" );
        role.setName( "name" );
        role.setSessionTimeout( 60 );
        
        List<String> roles = new ArrayList<String>();
        
        roles.add( "role1" );
        roles.add( "role2" );
        
        role.setRoles( roles );
        
        List<String> privs = new ArrayList<String>();
        
        privs.add( "priv1" );
        privs.add( "priv2" );
        
        role.setPrivileges( privs );
        
        SecurityRole cloned = new SecurityRole( role );
        
        assertTrue( cloned != null );
        assertTrue( cloned != role );
        assertTrue( cloned.getDescription().equals( role.getDescription() ) );
        assertTrue( cloned.getId().equals( role.getId() ) );
        assertTrue( cloned.getName().equals( role.getName() ) );
        assertTrue( cloned.getSessionTimeout() == role.getSessionTimeout() );
        
        assertTrue( cloned.getRoles() != null );
        assertTrue( cloned.getRoles() != role.getRoles() );
        assertTrue( cloned.getRoles().size() == 2 );
        assertTrue( cloned.getRoles().get( 0 ).equals( "role1" ) );
        assertTrue( cloned.getRoles().get( 1 ).equals( "role2" ) );
        
        assertTrue( cloned.getPrivileges() != null );
        assertTrue( cloned.getPrivileges() != role.getPrivileges() );
        assertTrue( cloned.getPrivileges().size() == 2 );
        assertTrue( cloned.getPrivileges().get( 0 ).equals( "priv1" ) );
        assertTrue( cloned.getPrivileges().get( 1 ).equals( "priv2" ) );
    }
    
    public void testPrivilegeClone()
        throws Exception
    {
        CPrivilege priv = new CPrivilege();
        priv.setDescription( "description" );
        priv.setId( "id" );
        priv.setName( "name" );
        priv.setType( "type" );
        
        List<CProperty> props = new ArrayList<CProperty>();
        
        CProperty prop1 = new CProperty();
        prop1.setKey( "key1" );
        prop1.setValue( "value1" );
        
        props.add( prop1 );
        
        CProperty prop2 = new CProperty();
        prop2.setKey( "key2" );
        prop2.setValue( "value2" );
        
        props.add( prop2 );
        
        priv.setProperties( props );
        
        SecurityPrivilege cloned = new SecurityPrivilege( priv );
        
        assertTrue( cloned != null );
        assertTrue( cloned != priv );
        assertTrue( cloned.getDescription().equals( priv.getDescription() ) );
        assertTrue( cloned.getId().equals( priv.getId() ) );
        assertTrue( cloned.getName().equals( priv.getName() ) );
        assertTrue( cloned.getType().equals( priv.getType() ) );
        
        assertTrue( cloned.getProperties() != null );
        assertTrue( cloned.getProperties() != priv.getProperties() );
        assertTrue( cloned.getProperties().size() == 2 );
        assertTrue( cloned.getProperties().get( 0 ) != prop1 );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 0 ) ).getKey().equals( "key1" ) );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 0 ) ).getValue().equals( "value1" ) );
        assertTrue( cloned.getProperties().get( 1 ) != prop2 );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 1 ) ).getKey().equals( "key2" ) );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 1 ) ).getValue().equals( "value2" ) );
    }
}
