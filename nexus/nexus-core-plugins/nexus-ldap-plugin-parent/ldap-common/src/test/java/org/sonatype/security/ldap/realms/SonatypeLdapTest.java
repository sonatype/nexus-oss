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

import java.util.Arrays;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;


public class SonatypeLdapTest
    extends PlexusTestCase
{

    private Realm realm;
    
    private PasswordEncoderManager passwordManager;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        realm = this.lookup( Realm.class, "LdapAuthenticatingRealm" );        
        passwordManager = (PasswordEncoderManager) this.lookup( PasswordEncoderManager.class );
        passwordManager.setPreferredEncoding( "crypt" );
    }
    
    
    @Override
    protected void customizeContext( Context context )
    {
       context.put( "application-conf", getBasedir() +"/target/test-classes/sonatype-conf/conf/" );
    }



    public void testSuccessfulAuthentication()
        throws Exception
    {

        // buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );
        UsernamePasswordToken upToken = new UsernamePasswordToken( "tstevens", "password" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

         Assert.assertNotNull( ai );
        
     }

    public void testWrongPassword()
    throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "tstevens", "JUNK" );
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
    
    public void BrokentestHasAllRoles()
    {

        Assert.assertFalse( this.doesUserHaveAllRoles( "brianf", "public", "releases" ) );
        Assert.assertTrue( this.doesUserHaveAllRoles( "jvanzyl", "wheel", "sonatype", "labs", "svn", "svn-labs", "sales", "sonatype-conf", "sonatype-jira", "book" ) );
        
        Assert.assertTrue( this.doesUserHaveAllRoles( "tstevens", "svn" ) );
        
        // expect failure
        Assert.assertFalse( this.doesUserHaveAllRoles( "cstamas", "public", "releases", "snapshots" ) );
        
    }

    private boolean doesUserHaveAllRoles(String username, String ... roles)
    {
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add( username, this.realm.getName() );
        
        return this.realm.hasAllRoles( principals, Arrays.asList( roles ) );
    }
    

}
