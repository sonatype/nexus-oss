/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.legacyadapter.test;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.IOUtil;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.legacyadapter.LegacySecurityAdapter;
import org.sonatype.security.usermanagement.UserManager;

public class SimpleRealmTest
    extends PlexusTestCase
{


    public static final String SECURITY_CONFIG_KEY = "security-xml-file";

    public static final String APP_CONFIGURATION_KEY = "application-conf";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File CONF_HOME = new File( PLEXUS_HOME, "conf" );

    @Override
    protected void customizeContext( Context ctx )
    {
        ctx.put( SECURITY_CONFIG_KEY, new File( CONF_HOME, "security.xml" ).getAbsolutePath() );
        ctx.put( APP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() );
    }

    @Override
    public void setUp()
        throws Exception
    {
        CONF_HOME.mkdirs();

        IOUtil.copy( ClassLoader.getSystemResourceAsStream( "conf/security.xml" ), new FileOutputStream(
            new File( CONF_HOME, "security.xml" ) ) );

        IOUtil.copy(
            ClassLoader.getSystemResourceAsStream( "conf/security-configuration.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security-configuration.xml" ) ) );

        super.setUp();
        
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.start();
        
    }
    
    
    // Realm Tests
    /**
     * Test authentication with a valid user and password.
     * 
     * @throws Exception
     */
    public void testValidAuthentication()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        AuthenticationToken token = new UsernamePasswordToken( "admin-simple", "admin123" );
        AuthenticationInfo authInfo = securitySystem.authenticate( token );

        // check
        Assert.assertNotNull( authInfo );
    }

    /**
     * Test authentication with a valid user and invalid password.
     * 
     * @throws Exception
     */
    public void testInvalidPasswordAuthentication()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        AuthenticationToken token = new UsernamePasswordToken( "admin-simple", "INVALID" );

        try
        {
            securitySystem.authenticate( token );
            Assert.fail( "AuthenticationException expected" );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    /**
     * Test authentication with a invalid user and password.
     * 
     * @throws Exception
     */
    public void testInvalidUserAuthentication()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        AuthenticationToken token = new UsernamePasswordToken( "INVALID", "INVALID" );

        try
        {
            securitySystem.authenticate( token );
            Assert.fail( "AuthenticationException expected" );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    // 
    /**
     * Test authorization using the NexusMethodAuthorizingRealm. <BR/> Take a look a the security.xml in
     * src/test/resources this maps the users in the UserStore to nexus roles/privileges
     * 
     * @throws Exception
     */
    public void testPrivileges()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.start();
        this.lookup( AuthorizationManager.class, "legacy" );

        PrincipalCollection principal = new SimplePrincipalCollection( "admin-simple", SecuritySystem.class
            .getSimpleName() );

        // test one of the privleges that the admin user has
        Assert.assertTrue( securitySystem.isPermitted( principal, "nexus:repositories:create" ) );// Repositories -
        // (create,read)  
    }

    /**
     * Tests a valid privilege for an invalid user
     * @throws Exception
     */
    public void testPrivilegesInvalidUser()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        PrincipalCollection principal = new SimplePrincipalCollection( "INVALID", SecuritySystem.class
            .getSimpleName() );

        // test one of the privleges
        Assert.assertFalse( securitySystem.isPermitted( principal, "nexus:repositories:create" ) );// Repositories -
        // (create,read)

    }
}
