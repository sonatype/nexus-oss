/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.ldap.realms;

import java.util.Set;

import junit.framework.Assert;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.codehaus.plexus.context.Context;
import org.junit.Test;
import org.sonatype.nexus.test.PlexusTestCaseSupport;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapGroupDAO;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;

public class NestedUserActiveDirectoryLdapSchemaTest
    extends PlexusTestCaseSupport
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
        // configure the logging
        // SLF4JBridgeHandler.install();

        super.setUp();

        this.ldapGroupManager = this.lookup( LdapGroupDAO.class );
        this.ldapConfiguration = this.lookup( LdapConfiguration.class );
        this.ldapContextFactory = this.lookup( LdapContextFactory.class, "PlexusLdapContextFactory" );
        this.ldapUserManager = (LdapUserDAO) lookup( LdapUserDAO.class.getName() );
        this.realm = this.lookup( Realm.class, "LdapAuthenticatingRealm" );
    }

    @Test
    public void testUserManager()
        throws Exception
    {
        LdapAuthConfiguration configuration = this.ldapConfiguration.getLdapAuthConfiguration();

        LdapUser user =
            this.ldapUserManager.getUser( "tstevens", this.ldapContextFactory.getSystemLdapContext(), configuration );
        Assert.assertEquals( "tstevens", user.getUsername() );
        Assert.assertEquals( "Toby Stevens", user.getRealName() );

        try
        {
            user =
                this.ldapUserManager.getUser( "intruder", this.ldapContextFactory.getSystemLdapContext(), configuration );
            Assert.fail( "Expected NoSuchUserException" );
        }
        catch ( NoSuchLdapUserException e )
        {
            // good
        }
    }

    @Test
    public void testGroupManager()
        throws Exception
    {
        LdapAuthConfiguration configuration = ldapConfiguration.getLdapAuthConfiguration();

        Set<String> groups =
            this.ldapGroupManager.getGroupMembership( "tstevens", this.ldapContextFactory.getSystemLdapContext(),
                configuration );

        Assert.assertTrue( "Groups: " + groups, groups.contains( "Administrators" ) );
    }

    @Test
    public void testSuccessfulAuthentication()
        throws Exception
    {

        UsernamePasswordToken upToken = new UsernamePasswordToken( "tstevens", "Tpass123" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        Assert.assertNull( ai.getCredentials() );

        // String password = new String( (char[]) ai.getCredentials() );
        //
        // // password is plain text
        // Assert.assertEquals( "brianf123", password );
    }

    @Test
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

    @Test
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
