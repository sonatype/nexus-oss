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
package org.sonatype.security.ldap.realms;

import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapGroupDAO;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;


public class NestedUserActiveDirectoryLdapSchemaTest
extends PlexusTestCase
{

    private LdapConfiguration ldapConfiguration;

    private LdapGroupDAO ldapGroupManager;

    private LdapUserDAO ldapUserManager;

    private LdapContextFactory ldapContextFactory;

    private Realm realm;

    @Override
    protected void customizeContext( Context context )
    {
       super.customizeContext( context );

       String classname = this.getClass().getName();
       context.put( "test-path", getBasedir() +"/target/test-classes/"+ classname.replace( '.', '/' ) );
    }


    /*
     * (non-Javadoc)
     *
     * @see org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment#setUp()
     */
    @Override
    public void setUp()
        throws Exception
    {
     // configure the logging
//        SLF4JBridgeHandler.install();

        super.setUp();

        this.ldapGroupManager = this.lookup( LdapGroupDAO.class );
        this.ldapConfiguration = this.lookup( LdapConfiguration.class );
        this.ldapContextFactory = this.lookup(
            LdapContextFactory.class,
            "PlexusLdapContextFactory" );
        this.ldapUserManager = (LdapUserDAO) lookup( LdapUserDAO.class.getName() );
        this.realm = this.lookup( Realm.class, "LdapAuthenticatingRealm" );
    }

    public void testUserManager()
        throws Exception
    {
        LdapAuthConfiguration configuration = this.ldapConfiguration.getLdapAuthConfiguration();

        LdapUser user = this.ldapUserManager.getUser( "tstevens", this.ldapContextFactory.getSystemLdapContext(), configuration );
        assertEquals( "tstevens", user.getUsername() );
        assertEquals( "Toby Stevens", user.getRealName() );

        try
        {
            user = this.ldapUserManager.getUser(
                "intruder",
                this.ldapContextFactory.getSystemLdapContext(),
                configuration );
            fail( "Expected NoSuchUserException" );
        }
        catch ( NoSuchLdapUserException e )
        {
            // good
        }
    }

    public void testGroupManager()
        throws Exception
    {
        LdapAuthConfiguration configuration = ldapConfiguration.getLdapAuthConfiguration();

        Set<String> groups = this.ldapGroupManager.getGroupMembership( "tstevens", this.ldapContextFactory
            .getSystemLdapContext(), configuration );

        assertTrue("Groups: "+ groups, groups.contains( "Administrators" ) );
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {

        UsernamePasswordToken upToken = new UsernamePasswordToken( "tstevens", "Tpass123" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        assertNull( ai.getCredentials() );

//        String password = new String( (char[]) ai.getCredentials() );
//
//        // password is plain text
//        assertEquals( "brianf123", password );
    }

    public void testWrongPassword()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "tstevens", "JUNK" );
        try
        {
            realm.getAuthenticationInfo( upToken );
            Assert.fail( "Expected AuthenticationException exception." );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    public void testFailedAuthentication()
    {

        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        try
        {
            realm.getAuthenticationInfo( upToken );
            Assert.fail( "Expected AuthenticationException exception." );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    protected boolean isPasswordsEncrypted()
    {
        return false;
    }


}
