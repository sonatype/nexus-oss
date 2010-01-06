/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.util.Set;
import java.util.SortedSet;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;
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
