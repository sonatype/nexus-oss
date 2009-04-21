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
package org.sonatype.jsecurity.realms;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.DefaultConfigurationManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.StringDigester;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;

public class XmlAuthenticatingRealmTest
    extends PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    
    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml"; 
    
    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );
    
    private XmlAuthenticatingRealm realm;
    
    private DefaultConfigurationManager configurationManager;
        
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( XmlAuthenticatingRealm ) lookup( Realm.class, "XmlAuthenticatingRealm" );
        
        configurationManager = ( DefaultConfigurationManager ) lookup( ConfigurationManager.class, "default" );
        
        configurationManager.clearCache();
        
        configFile.delete();
    }
    
    public void testSuccessfulAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = new String( (char[] ) ai.getCredentials() );
        
        assertEquals( StringDigester.getSha1Digest( "password" ), password );        
    }
    
    
    public void testCreateWithPassowrd()
    throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );
        
        String clearPassword = "default-password";
        
        SecurityUser user = new SecurityUser();
        user.setEmail( "testCreateWithPassowrdEmail" );
        user.setName( "testCreateWithPassowrdEmail" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setId( "testCreateWithPassowrdEmailUserId" );
        user.addRole( "role" );
        configurationManager.createUser( user, clearPassword );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "testCreateWithPassowrdEmailUserId", clearPassword );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = new String( (char[] ) ai.getCredentials() );
        
        assertEquals( StringDigester.getSha1Digest( clearPassword ), password );        
    }
    
    public void testFailedAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "badpassword" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }   
    }
    
    public void testDisabledAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_DISABLED );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }
    }
    
    private void buildTestAuthenticationConfig( String status ) throws InvalidConfigurationException
    {
        SecurityPrivilege priv = new SecurityPrivilege();
        priv.setId( "priv" );
        priv.setName( "name" );
        priv.setDescription( "desc" );
        priv.setType( "method" );
        
        SecurityProperty prop = new SecurityProperty();
        prop.setKey( "method" );
        prop.setValue( "read" );
        priv.addProperty( prop );
        
        prop = new SecurityProperty();
        prop.setKey( "permission" );
        prop.setValue( "somevalue" );
        priv.addProperty( prop );
        
        configurationManager.createPrivilege( priv );
        
        SecurityRole role = new SecurityRole();
        role.setName( "name" );
        role.setId( "role" );
        role.setDescription( "desc" );
        role.setSessionTimeout( 50 );
        role.addPrivilege( "priv" );
        
        configurationManager.createRole( role );
        
        SecurityUser user = new SecurityUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( status );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
        user.addRole( "role" );
        
        configurationManager.createUser( user );
        
        configurationManager.save();
    }
}
