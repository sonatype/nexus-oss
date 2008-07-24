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
package org.sonatype.nexus.configuration.security;

import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.IncorrectCredentialsException;
import org.jsecurity.authc.UnknownAccountException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.SimplePrincipalCollection;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

public class NexusRealmTest extends AbstractRealmTest
{

    public void testAuthenticationInfo()
    {
        AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(
            new UsernamePasswordToken( "dain", "niad" ) );
        assertNotNull( "authenticationInfo is null", authenticationInfo );
        String username = (String) authenticationInfo.getPrincipals().fromRealm( realm.getName() ).iterator().next();
        assertEquals( "dain", username );
        String password = new String( (char[]) authenticationInfo.getCredentials() );
        assertEquals( "niad", password );
    }

    public void testBadPassword()
    {
        try
        {
            realm.getAuthenticationInfo( new UsernamePasswordToken( "dain", "bad" ) );
            fail( "Expected IncorrectCredentialsException" );
        }
        catch ( IncorrectCredentialsException expected )
        {
        }
    }

    public void testUnknownAccount()
    {
        try
        {
            realm.getAuthenticationInfo( new UsernamePasswordToken( "unknown", "unknown" ) );
            fail( "Expected UnknownAccountException" );
        }
        catch ( UnknownAccountException expected )
        {
        }
    }

    public void testAuthorizationInfo()
    {
        // get authentication info
        AuthorizationInfo dain = realm.getAuthorizationInfo(
            new SimplePrincipalCollection( "dain", realm.getName() ) );
        assertNotNull( "authorizationInfo is null", dain );

        // dain is only a member of maven-user
        assertEquals( new LinkedHashSet<String>( Arrays.asList( "maven-user" ) ), dain.getRoles() );

        // verify dain has permission to read but not create artifacts
        Collection<Permission> dainPermissions = dain.getObjectPermissions();
        assertImplied( new WildcardPermission( "maven:central:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "maven:central:CREATE" ), dainPermissions );
        assertImplied( new WildcardPermission( "maven:myRepository:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "maven:myRepository:CREATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "maven:codehaus:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "maven:codehaus:CREATE" ), dainPermissions );

        // get authentication info
        AuthorizationInfo jason = realm.getAuthorizationInfo(
            new SimplePrincipalCollection( "jason", realm.getName() ) );
        assertNotNull( "authorizationInfo is null", jason );

        // jason is a member of maven-user and maven-committer
        assertEquals( new LinkedHashSet<String>( Arrays.asList( "maven-user", "maven-committer" ) ), jason.getRoles() );

        // verify jason has permission to read and create artifacts
        Collection<Permission> jasonPermissions = jason.getObjectPermissions();
        assertImplied( new WildcardPermission( "maven:central:READ" ), jasonPermissions );
        assertImplied( new WildcardPermission( "maven:central:CREATE" ), jasonPermissions );
        assertImplied( new WildcardPermission( "maven:myRepository:READ" ), jasonPermissions );
        assertImplied( new WildcardPermission( "maven:myRepository:CREATE" ), jasonPermissions );
        assertNotImplied( new WildcardPermission( "maven:codehaus:READ" ), jasonPermissions );
        assertNotImplied( new WildcardPermission( "maven:codehaus:CREATE" ), jasonPermissions );
    }

    public void testAuthorizerInterface()
    {
        SimplePrincipalCollection dain = new SimplePrincipalCollection( "dain", realm.getName() );

        // dain is only a member of maven-user
        assertTrue( realm.hasRole( dain, "maven-user" ) );
        assertFalse( realm.hasRole( dain, "maven-committer" ) );
        assertFalse( realm.hasRole( dain, "unknown" ) );

        // verify dain has permission to read but not create artifacts
        assertPermitted( dain, "maven:central:READ" );
        assertNotPermitted( dain, "maven:central:CREATE" );
        assertPermitted( dain, "maven:myRepository:READ" );
        assertNotPermitted( dain, "maven:myRepository:CREATE" );
        assertNotPermitted( dain, "maven:codehaus:READ" );
        assertNotPermitted( dain, "maven:codehaus:CREATE" );


        SimplePrincipalCollection jason = new SimplePrincipalCollection( "jason", realm.getName() );

        // jason is a member of maven-user and maven-committer
        assertTrue( realm.hasRole( jason, "maven-user" ) );
        assertTrue( realm.hasRole( jason, "maven-committer" ) );
        assertFalse( realm.hasRole( jason, "unknown" ) );

        // verify jason has permission to read and create artifacts
        assertPermitted( jason, "maven:central:READ" );
        assertPermitted( jason, "maven:central:CREATE" );
        assertPermitted( jason, "maven:myRepository:READ" );
        assertPermitted( jason, "maven:myRepository:CREATE" );
        assertNotPermitted( jason, "maven:codehaus:READ" );
        assertNotPermitted( jason, "maven:codehaus:CREATE" );
    }

    public void assertPermitted( SimplePrincipalCollection principal, String permission )
    {
        assertTrue( "Principal " + permission + " should be permitted to " + permission,
            realm.isPermitted( principal, permission ) );
        assertTrue( "Principal " + permission + " should be permitted to " + permission,
            realm.isPermitted( principal, new WildcardPermission( permission ) ) );
    }

    public void assertNotPermitted( SimplePrincipalCollection principal, String permission )
    {
        assertFalse( "Principal " + permission + " should not be permitted to " + permission,
            realm.isPermitted( principal, permission ) );
        assertFalse( "Principal " + permission + " should not be permitted to " + permission,
            realm.isPermitted( principal, new WildcardPermission( permission ) ) );
    }

}
