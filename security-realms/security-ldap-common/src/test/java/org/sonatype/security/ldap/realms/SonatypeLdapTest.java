/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.util.Arrays;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.SimplePrincipalCollection;
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
