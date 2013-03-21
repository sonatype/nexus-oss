/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.security.configuration.DefaultSecurityConfigurationManager;
import org.sonatype.security.configuration.SecurityConfigurationManager;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.DefaultConfigurationManager;
import org.sonatype.security.usermanagement.PasswordGenerator;

public class XmlAuthenticatingRealmTest
    extends InjectedTestCase
{
    private final String SECURITY_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml";
    
    private final String SECURITY_CONFIGURATION_FILE_PATH = getBasedir() + "/target/jsecurity/security-configuration.xml";

    private File configFile = new File( SECURITY_FILE_PATH );

    private XmlAuthenticatingRealm realm;

    private DefaultConfigurationManager configurationManager;
    
    private DefaultSecurityConfigurationManager securityConfigurationManager;
    
    private PasswordGenerator passwordGenerator;
    
    private CUser testUser;

    @Override
    public void configure( Properties properties )
    {
        properties.put( "security-xml-file", SECURITY_FILE_PATH );
        properties.put( "application-conf" , SECURITY_CONFIGURATION_FILE_PATH );
        super.configure( properties );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        realm = (XmlAuthenticatingRealm) lookup( Realm.class, "XmlAuthenticatingRealm" );

        configurationManager = (DefaultConfigurationManager) lookup( ConfigurationManager.class, "default" );

        configurationManager.clearCache();
        
        securityConfigurationManager = (DefaultSecurityConfigurationManager) lookup( SecurityConfigurationManager.class, "default" );
        
        passwordGenerator = lookup( PasswordGenerator.class, "default" );

        configFile.delete();
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );

        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        String password = new String( (char[]) ai.getCredentials() );

        assertEquals( this.hashPassword("password", testUser.getSalt()), password );
    }

    public void testCreateWithPassowrd()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );

        String clearPassword = "default-password";
        String username = "testCreateWithPassowrdEmailUserId";

        CUser user = new CUser();
        user.setEmail( "testCreateWithPassowrdEmail@somewhere" );
        user.setFirstName( "testCreateWithPassowrdEmail" );
        user.setLastName( "testCreateWithPassowrdEmail" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setId( username );

        Set<String> roles = new HashSet<String>();
        roles.add( "role" );

        configurationManager.createUser( user, clearPassword, roles );

        UsernamePasswordToken upToken = new UsernamePasswordToken( "testCreateWithPassowrdEmailUserId", clearPassword );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        String password = new String( (char[]) ai.getCredentials() );
        
        CUser updatedUser = this.configurationManager.readUser(username);

        assertEquals( this.hashPassword(clearPassword, updatedUser.getSalt()) , password );
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
        catch ( AuthenticationException e )
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
        catch ( AuthenticationException e )
        {
            // good
        }
    }
    
    public void testDetectLegacyUser()
        throws Exception
    {
        String password = "password";
        String username = "username";
        buildLegacyTestAuthenticationConfig(password);
        
        UsernamePasswordToken upToken = new UsernamePasswordToken(username, password);
        AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);
        CUser updatedUser = this.configurationManager.readUser(username);
        String hash = new String( (char[]) ai.getCredentials() );
        
        assertThat(hash, is(this.hashPassword(password, updatedUser.getSalt())));
        assertThat(updatedUser.getPassword(), is(this.hashPassword(password, updatedUser.getSalt())));
        
    }

    private void buildTestAuthenticationConfig( String status )
        throws InvalidConfigurationException
    {
        String salt = this.passwordGenerator.generateSalt();
        buildTestAuthenticationConfig(status, this.hashPassword("password", salt), salt);
    }
    
    private void buildTestAuthenticationConfig(String status, String hash, String salt)
    	throws InvalidConfigurationException
    {
        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "name" );
        priv.setDescription( "desc" );
        priv.setType( "method" );

        CProperty prop = new CProperty();
        prop.setKey( "method" );
        prop.setValue( "read" );
        priv.addProperty( prop );

        prop = new CProperty();
        prop.setKey( "permission" );
        prop.setValue( "somevalue" );
        priv.addProperty( prop );

        configurationManager.createPrivilege( priv );

        CRole role = new CRole();
        role.setName( "name" );
        role.setId( "role" );
        role.setDescription( "desc" );
        role.setSessionTimeout( 50 );
        role.addPrivilege( "priv" );

        configurationManager.createRole( role );

        testUser = new CUser();
        testUser.setEmail( "dummyemail@somewhere" );
        testUser.setFirstName( "dummyFirstName" );
        testUser.setLastName( "dummyLastName" );
        testUser.setStatus( status );
        testUser.setId( "username" );
        testUser.setSalt(salt);
        testUser.setPassword(hash);

        Set<String> roles = new HashSet<String>();
        roles.add( "role" );

        configurationManager.createUser( testUser, roles );
        configurationManager.save();
    }
    
    private void buildLegacyTestAuthenticationConfig(String password)
    	throws InvalidConfigurationException
    {
        buildTestAuthenticationConfig(CUser.STATUS_ACTIVE, this.legacyHashPassword(password), "");
    }
    
    private String hashPassword(String password, String salt)
    {
        return this.passwordGenerator.hashPassword(password, salt, this.securityConfigurationManager.getHashIterations());
    }
    
    private String legacyHashPassword(String password)
    {
        return this.passwordGenerator.hashPassword(password);
    }
}
