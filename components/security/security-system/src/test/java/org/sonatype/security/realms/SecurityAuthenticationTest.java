/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.realms;

import java.util.Collection;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.sonatype.security.AbstractSecurityTest;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

public class SecurityAuthenticationTest
    extends AbstractSecurityTest
{
    private SecuritySystem security;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        security = (SecuritySystem) lookup( SecuritySystem.class ); // started in parent class
    }

    public void testAuthcAndAuthzAfterRestart()
        throws Exception
    {
        testSuccessfulAuthentication();
        testAuthorization();
        security.stop();
        security.start();
        testSuccessfulAuthentication();
        testAuthorization();
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );

        // this.setupLoginContext( "test" );

        Subject ai = security.login( upToken );

        assertEquals( "username", ai.getPrincipal().toString() );
    }

    public void testFailedAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "badpassword" );

        try
        {
            security.login( upToken );

            fail( "Authentication should have failed" );
        }
        catch ( AuthenticationException e )
        {
            // good
        }
    }

    public void testAuthorization()
        throws Exception
    {
        assertTrue( security.isPermitted( new SimplePrincipalCollection( "username", FakeRealm1.class.getName() ),
                                          "test:perm" ) );

        assertTrue( security.isPermitted( new SimplePrincipalCollection( "username", FakeRealm1.class.getName() ),
                                          "other:perm" ) );

        assertTrue( security.isPermitted( new SimplePrincipalCollection( "username", FakeRealm2.class.getName() ),
                                          "other:perm" ) );

        assertTrue( security.isPermitted( new SimplePrincipalCollection( "username", FakeRealm2.class.getName() ),
                                          "test:perm" ) );
    }

    public static void assertImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                return;
            }
        }
        fail( "Expected " + testPermission + " to be implied by " + assignedPermissions );
    }

    public static void assertNotImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                fail( "Expected " + testPermission + " not to be implied by " + assignedPermission );
            }
        }
    }
}
