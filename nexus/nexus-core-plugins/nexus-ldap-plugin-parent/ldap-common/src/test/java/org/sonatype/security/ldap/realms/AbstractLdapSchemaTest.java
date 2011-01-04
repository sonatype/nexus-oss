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
import java.util.SortedSet;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;

public abstract class AbstractLdapSchemaTest
    extends AbstractLdapTestEnvironment
{

    private LdapManager ldapManager;

    private Realm realm;

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        String classname = this.getClass().getName();
        context.put( "test-path", getBasedir() + "/target/test-classes/" + classname.replace( '.', '/' ) );
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment#setUp()
     */
    @Override
    public void setUp()
        throws Exception
    {
        assertNotNull( lookup( LdapConfiguration.class ) );

        super.setUp();

        this.realm = this.lookup( Realm.class, "LdapAuthenticatingRealm" );
        this.ldapManager = this.lookup( LdapManager.class );
    }

    public void testUserManager()
        throws Exception
    {
        LdapUser user = this.ldapManager.getUser( "cstamas" );
        assertEquals( "cstamas", user.getUsername() );
        // assertEquals( "Tamas Cservenak", user.getRealName() );

        assertTrue( this.isPasswordsEncrypted() || ( "cstamas123".equals( user.getPassword() ) ) );

        user = this.ldapManager.getUser( "brianf" );
        assertEquals( "brianf", user.getUsername() );
        // assertEquals( "Brian Fox", user.getRealName() );
        assertTrue( this.isPasswordsEncrypted() || ( "brianf123".equals( user.getPassword() ) ) );

        user = this.ldapManager.getUser( "jvanzyl" );
        assertEquals( "jvanzyl", user.getUsername() );
        // assertEquals( "Jason Van Zyl", user.getRealName() );
        assertTrue( this.isPasswordsEncrypted() || ( "jvanzyl123".equals( user.getPassword() ) ) );

        try
        {
            user = this.ldapManager.getUser( "intruder" );
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
        Set<String> groups = this.ldapManager.getUserRoles( "cstamas" );
        assertEquals( 2, groups.size() );
        assertTrue( groups.contains( "public" ) );
        assertTrue( groups.contains( "snapshots" ) );

        groups = this.ldapManager.getUserRoles( "brianf" );
        assertEquals( 2, groups.size() );
        assertTrue( groups.contains( "public" ) );
        assertTrue( groups.contains( "releases" ) );

        groups = this.ldapManager.getUserRoles( "jvanzyl" );
        assertEquals( 3, groups.size() );
        assertTrue( groups.contains( "public" ) );
        assertTrue( groups.contains( "releases" ) );
        assertTrue( groups.contains( "snapshots" ) );
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {

        UsernamePasswordToken upToken = new UsernamePasswordToken( "brianf", "brianf123" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        assertNull( ai.getCredentials() );

        // String password = new String( (char[]) ai.getCredentials() );
        //
        // // password is plain text
        // assertEquals( "brianf123", password );
    }

    public void testWrongPassword()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "brianf", "JUNK" );
        try
        {
            Assert.assertNull( realm.getAuthenticationInfo( upToken ) );
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

    public void testSearch()
        throws LdapDAOException
    {
        Set<LdapUser> users = this.ldapManager.searchUsers( "cstamas" );
        assertEquals( 1, users.size() );
        LdapUser user = users.iterator().next();
        assertEquals( "cstamas", user.getUsername() );
        assertTrue( this.isPasswordsEncrypted() || ( "cstamas123".equals( user.getPassword() ) ) );

        users = this.ldapManager.searchUsers( "br" );
        assertEquals( 1, users.size() );
        user = users.iterator().next();
        assertEquals( "brianf", user.getUsername() );
        // assertEquals( "Brian Fox", user.getRealName() );
        assertTrue( this.isPasswordsEncrypted() || ( "brianf123".equals( user.getPassword() ) ) );

        users = this.ldapManager.searchUsers( "j" );
        assertEquals( 1, users.size() );
        user = users.iterator().next();
        assertEquals( "jvanzyl", user.getUsername() );
        // assertEquals( "Jason Van Zyl", user.getRealName() );
        assertTrue( this.isPasswordsEncrypted() || ( "jvanzyl123".equals( user.getPassword() ) ) );

        users = this.ldapManager.searchUsers( "INVALID" );
        assertEquals( 0, users.size() );
    }

    public void testGetAllGroups()
        throws LdapDAOException
    {
        SortedSet<String> groupIds = this.ldapManager.getAllGroups();

        assertTrue( "GroupIds: " + groupIds, groupIds.contains( "public" ) );
        assertTrue( "GroupIds: " + groupIds, groupIds.contains( "releases" ) );
        assertTrue( "GroupIds: " + groupIds, groupIds.contains( "snapshots" ) );
        assertEquals( "GroupIds: " + groupIds, 3, groupIds.size() );

    }

    public void testGetGroupName()
        throws LdapDAOException,
            NoSuchLdapGroupException
    {
        assertEquals( "public", this.ldapManager.getGroupName( "public" ) );
        try
        {
            this.ldapManager.getGroupName( "p" );
            fail( "Expected NoSuchLdapGroupException" );
        }
        catch ( NoSuchLdapGroupException e )
        {
            // expected
        }
    }

}
