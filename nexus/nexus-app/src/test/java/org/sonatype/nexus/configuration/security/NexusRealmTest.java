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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.jsecurity.authc.AccountException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.DisabledAccountException;
import org.jsecurity.authc.ExpiredCredentialsException;
import org.jsecurity.authc.IncorrectCredentialsException;
import org.jsecurity.authc.LockedAccountException;
import org.jsecurity.authc.UnknownAccountException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.nexus.util.StringDigester;

public class NexusRealmTest
    extends AbstractRealmTest
{

    public void testAuthenticationInfo()
    {
        AuthenticationInfo authenticationInfo = realm
            .getAuthenticationInfo( new UsernamePasswordToken( "dain", "niad" ) );
        assertNotNull( "authenticationInfo is null", authenticationInfo );
        String username = (String) authenticationInfo.getPrincipals().fromRealm( realm.getName() ).iterator().next();
        assertEquals( "dain", username );
        String password = new String( (char[]) authenticationInfo.getCredentials() );
        assertEquals( StringDigester.getSha1Digest( "niad" ), password );
    }

    /*public void testLockedAuthenticationInfo()
    {
        try
        {
            AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo( new UsernamePasswordToken(
                "locked",
                "locked" ) );

            fail();
        }
        catch ( LockedAccountException e )
        {
            // cool
        }
    }

    public void testExpiredAuthenticationInfo()
    {
        try
        {
            AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo( new UsernamePasswordToken(
                "expired",
                "expired" ) );

            fail();
        }
        catch ( ExpiredCredentialsException e )
        {
            // cool
        }
    }*/

    public void testDisabledAuthenticationInfo()
    {
        try
        {
            AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo( new UsernamePasswordToken(
                "disabled",
                "disabled" ) );

            fail();
        }
        catch ( DisabledAccountException e )
        {
            // cool
        }
    }
    
    public void testIllegalStatusAuthenticationInfo()
    {
        try
        {
            AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo( new UsernamePasswordToken(
                "illegalStatus",
                "illegalStatus" ) );

            fail();
        }
        catch ( AccountException e )
        {
            // cool
        }
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
        AuthorizationInfo dain = realm.getAuthorizationInfo( new SimplePrincipalCollection( "dain", realm.getName() ) );
        assertNotNull( "authorizationInfo is null", dain );

        // dain is only a member of maven-user
        assertEquals( new LinkedHashSet<String>( Arrays.asList( "maven-user" ) ), dain.getRoles() );

        // verify dain has permission to read but not create artifacts
        Collection<Permission> dainPermissions = dain.getObjectPermissions();
        assertImplied( new WildcardPermission( "nexus:target:maven:central:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), dainPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:myRepository:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:myRepository:CREATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), dainPermissions );

        // verify dain has permission to read but not create repository
        assertImplied( new WildcardPermission( "nexus:repository:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:repository:CREATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:repository:UPDATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:repository:DELETE" ), dainPermissions );

        // verify dain does not have permissions to admin users
        assertNotImplied( new WildcardPermission( "nexus:user:READ" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:user:CREATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:user:UPDATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:user:DELETE" ), dainPermissions );

        // get authentication info
        AuthorizationInfo jason = realm
            .getAuthorizationInfo( new SimplePrincipalCollection( "jason", realm.getName() ) );
        assertNotNull( "authorizationInfo is null", jason );

        // jason is a member of maven-user and maven-committer
        assertEquals( new LinkedHashSet<String>( Arrays.asList( "maven-user", "maven-committer" ) ), jason.getRoles() );

        // verify jason has permission to read and create artifacts
        Collection<Permission> jasonPermissions = jason.getObjectPermissions();
        assertImplied( new WildcardPermission( "nexus:target:maven:central:READ" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:myRepository:READ" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:myRepository:CREATE" ), jasonPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:READ" ), jasonPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), jasonPermissions );

        // verify jason has permission to read and create repository, but not update or delete
        assertImplied( new WildcardPermission( "nexus:repository:READ" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:repository:CREATE" ), jasonPermissions );
        assertNotImplied( new WildcardPermission( "nexus:repository:UPDATE" ), dainPermissions );
        assertNotImplied( new WildcardPermission( "nexus:repository:DELETE" ), dainPermissions );

        // verify jason has permissions to admin users
        assertImplied( new WildcardPermission( "nexus:user:READ" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:user:CREATE" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:user:UPDATE" ), jasonPermissions );
        assertImplied( new WildcardPermission( "nexus:user:DELETE" ), jasonPermissions );

    }

    public void testAuthorizerInterface()
    {
        SimplePrincipalCollection dain = new SimplePrincipalCollection( "dain", realm.getName() );

        // dain is only a member of maven-user
        assertTrue( realm.hasRole( dain, "maven-user" ) );
        assertFalse( realm.hasRole( dain, "maven-committer" ) );
        assertFalse( realm.hasRole( dain, "unknown" ) );

        // verify dain has permission to read but not create artifacts
        assertPermitted( dain, "nexus:target:maven:central:READ" );
        assertNotPermitted( dain, "nexus:target:maven:central:CREATE" );
        assertPermitted( dain, "nexus:target:maven:myRepository:READ" );
        assertNotPermitted( dain, "nexus:target:maven:myRepository:CREATE" );
        assertNotPermitted( dain, "nexus:target:maven:codehaus:READ" );
        assertNotPermitted( dain, "nexus:target:maven:codehaus:CREATE" );

        // verify dain has permission to read but not create repository
        assertPermitted( dain, "nexus:repository:READ" );
        assertNotPermitted( dain, "nexus:repository:CREATE" );
        assertNotPermitted( dain, "nexus:repository:UPDATE" );
        assertNotPermitted( dain, "nexus:repository:DELETE" );

        // verify dain does not have permissions to admin users
        assertNotPermitted( dain, "nexus:user:READ" );
        assertNotPermitted( dain, "nexus:user:CREATE" );
        assertNotPermitted( dain, "nexus:user:UPDATE" );
        assertNotPermitted( dain, "nexus:user:DELETE" );

        SimplePrincipalCollection jason = new SimplePrincipalCollection( "jason", realm.getName() );

        // jason is a member of maven-user and maven-committer
        assertTrue( realm.hasRole( jason, "maven-user" ) );
        assertTrue( realm.hasRole( jason, "maven-committer" ) );
        assertFalse( realm.hasRole( jason, "unknown" ) );

        // verify jason has permission to read and create artifacts
        assertPermitted( jason, "nexus:target:maven:central:READ" );
        assertPermitted( jason, "nexus:target:maven:central:CREATE" );
        assertPermitted( jason, "nexus:target:maven:myRepository:READ" );
        assertPermitted( jason, "nexus:target:maven:myRepository:CREATE" );
        assertNotPermitted( jason, "nexus:target:maven:codehaus:READ" );
        assertNotPermitted( jason, "nexus:target:maven:codehaus:CREATE" );

        // verify jason has permission to read and create repository, but not update or delete
        assertPermitted( jason, "nexus:repository:READ" );
        assertPermitted( jason, "nexus:repository:CREATE" );
        assertNotPermitted( jason, "nexus:repository:UPDATE" );
        assertNotPermitted( jason, "nexus:repository:DELETE" );

        // verify jason does not have permissions to admin users
        assertPermitted( jason, "nexus:user:READ" );
        assertPermitted( jason, "nexus:user:CREATE" );
        assertPermitted( jason, "nexus:user:UPDATE" );
        assertPermitted( jason, "nexus:user:DELETE" );
    }

    public void assertPermitted( SimplePrincipalCollection principal, String permission )
    {
        assertTrue( "Principal " + permission + " should be permitted to " + permission, realm.isPermitted(
            principal,
            permission ) );
        assertTrue( "Principal " + permission + " should be permitted to " + permission, realm.isPermitted(
            principal,
            new WildcardPermission( permission ) ) );
    }

    public void assertNotPermitted( SimplePrincipalCollection principal, String permission )
    {
        assertFalse( "Principal " + permission + " should not be permitted to " + permission, realm.isPermitted(
            principal,
            permission ) );
        assertFalse( "Principal " + permission + " should not be permitted to " + permission, realm.isPermitted(
            principal,
            new WildcardPermission( permission ) ) );
    }

}
