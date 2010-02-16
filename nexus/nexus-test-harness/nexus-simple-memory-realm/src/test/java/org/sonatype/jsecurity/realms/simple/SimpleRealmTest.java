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
package org.sonatype.jsecurity.realms.simple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import org.sonatype.security.realms.tools.ConfigurationManager;

public class SimpleRealmTest
    extends PlexusTestCase
{
    private static java.io.File confdir = new File( "target/app-conf" );
    // Realm Tests
    /**
     * Test authentication with a valid user and password.
     *
     * @throws Exception
     */
    public void testValidAuthentication()
        throws Exception
    {
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );
        AuthenticationToken token = new UsernamePasswordToken( "admin-simple", "admin123" );
        AuthenticationInfo authInfo = plexusSecurity.authenticate( token );

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
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );
        AuthenticationToken token = new UsernamePasswordToken( "admin-simple", "INVALID" );

        try
        {
            plexusSecurity.authenticate( token );
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
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );
        AuthenticationToken token = new UsernamePasswordToken( "INVALID", "INVALID" );

        try
        {
            plexusSecurity.authenticate( token );
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
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );

        PrincipalCollection principal = new SimplePrincipalCollection( "admin-simple", SecuritySystem.class
            .getSimpleName() );

        // test one of the privleges that the admin user has
        Assert.assertTrue( plexusSecurity.isPermitted( principal, "nexus:repositories:create" ) );// Repositories -
        // (create,read)

    }

    /**
     * Tests a valid privilege for an invalid user
     * @throws Exception
     */
    public void testPrivilegesInvalidUser()
        throws Exception
    {
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );

        PrincipalCollection principal = new SimplePrincipalCollection( "INVALID", SecuritySystem.class
            .getSimpleName() );

        // test one of the privleges
        Assert.assertFalse( plexusSecurity.isPermitted( principal, "nexus:repositories:create" ) );// Repositories -
        // (create,read)

    }

    @Override
    protected void setUp()
        throws Exception
    {        
        confdir.mkdirs();
        // copy the tests nexus.xml and security.xml to the correct location
        this.copyTestConfigToPlace();
        
        // restart security
        this.lookup( ConfigurationManager.class ).clearCache();
        this.lookup( SecuritySystem.class ).start();
    }
    

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        
        ctx.put( "application-conf", confdir.getAbsolutePath() );
        ctx.put( "security-xml-file", confdir.getAbsolutePath() + "/security.xml" );
    }

    private void copyTestConfigToPlace()
        throws FileNotFoundException,
            IOException
    {
        InputStream nexusConf = null;
        InputStream security = null;
        InputStream securityConf = null;

        OutputStream nexusOut = null;
        OutputStream securityOut = null;
        OutputStream securityConfOut = null;

        try
        {
            nexusConf = Thread.currentThread().getContextClassLoader().getResourceAsStream( "nexus.xml" );
            nexusOut = new FileOutputStream( new File( confdir, "nexus.xml" ) );
            IOUtil.copy( nexusConf, nexusOut );

            security = Thread.currentThread().getContextClassLoader().getResourceAsStream( "security.xml" );
            securityOut = new FileOutputStream( new File( confdir, "security.xml" ) );
            IOUtil.copy( security, securityOut);
            
            securityConf = Thread.currentThread().getContextClassLoader().getResourceAsStream( "security-configuration.xml" );
            securityConfOut = new FileOutputStream( new File( confdir, "security-configuration.xml" ) );
            IOUtil.copy( securityConf, securityConfOut);
        }
        finally
        {
            IOUtil.close( nexusConf );
            IOUtil.close( securityConf );
            IOUtil.close( nexusOut );
            IOUtil.close( securityOut );
            IOUtil.close( security );
            IOUtil.close( securityConfOut );

        }
    }
}
