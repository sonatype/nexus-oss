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
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.DefaultConfigurationManager;
import org.sonatype.security.usermanagement.StringDigester;

public class XmlAuthenticatingRealmTest
    extends InjectedTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";

    private final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml";

    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );

    private XmlAuthenticatingRealm realm;

    private DefaultConfigurationManager configurationManager;

    @Override
    public void configure( Properties properties )
    {
        properties.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
        super.configure( properties );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        realm = (XmlAuthenticatingRealm) lookup( Realm.class, "XmlAuthenticatingRealm" );

        configurationManager = lookup( DefaultConfigurationManager.class);

        configurationManager.clearCache();

        configFile.delete();
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );

        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        String password = new String( (char[]) ai.getCredentials() );

        assertEquals( StringDigester.getSha1Digest( "password" ), password );
    }

    public void testCreateWithPassowrd()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );

        String clearPassword = "default-password";

        CUser user = new CUser();
        user.setEmail( "testCreateWithPassowrdEmail@somewhere" );
        user.setFirstName( "testCreateWithPassowrdEmail" );
        user.setLastName( "testCreateWithPassowrdEmail" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setId( "testCreateWithPassowrdEmailUserId" );

        Set<String> roles = new HashSet<String>();
        roles.add( "role" );

        configurationManager.createUser( user, clearPassword, roles );

        UsernamePasswordToken upToken = new UsernamePasswordToken( "testCreateWithPassowrdEmailUserId", clearPassword );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        String password = new String( (char[]) ai.getCredentials() );

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

    private void buildTestAuthenticationConfig( String status )
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

        CUser user = new CUser();
        user.setEmail( "dummyemail@somewhere" );
        user.setFirstName( "dummyFirstName" );
        user.setLastName( "dummyLastName" );
        user.setStatus( status );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );

        Set<String> roles = new HashSet<String>();
        roles.add( "role" );

        configurationManager.createUser( user, roles );
        configurationManager.save();
    }
}
