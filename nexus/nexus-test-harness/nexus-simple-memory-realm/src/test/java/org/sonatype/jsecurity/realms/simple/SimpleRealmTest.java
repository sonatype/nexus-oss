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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.codehaus.plexus.util.IOUtil;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jsecurity.realms.PlexusSecurity;
import org.sonatype.nexus.AbstractNexusTestCase;

public class SimpleRealmTest
    extends AbstractNexusTestCase
{

    // Realm Tests
    /**
     * Test authentication with a valid user and password.
     * 
     * @throws Exception
     */
    public void testValidAuthentication()
        throws Exception
    {
        PlexusSecurity plexusSecurity = this.lookup( PlexusSecurity.class, "web" );
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
        PlexusSecurity plexusSecurity = this.lookup( PlexusSecurity.class, "web" );
        AuthenticationToken token = new UsernamePasswordToken( "admin-simple", "INVALID" );

        try
        {
            AuthenticationInfo authInfo = plexusSecurity.authenticate( token );
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
        PlexusSecurity plexusSecurity = this.lookup( PlexusSecurity.class, "web" );
        AuthenticationToken token = new UsernamePasswordToken( "INVALID", "INVALID" );

        try
        {
            AuthenticationInfo authInfo = plexusSecurity.authenticate( token );
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
        PlexusSecurity plexusSecurity = this.lookup( PlexusSecurity.class, "web" );

        PrincipalCollection principal = new SimplePrincipalCollection( "admin-simple", PlexusSecurity.class
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
        PlexusSecurity plexusSecurity = this.lookup( PlexusSecurity.class, "web" );

        PrincipalCollection principal = new SimplePrincipalCollection( "INVALID", PlexusSecurity.class
            .getSimpleName() );

        // test one of the privleges
        Assert.assertFalse( plexusSecurity.isPermitted( principal, "nexus:repositories:create" ) );// Repositories -
        // (create,read)

    }

    @Override
    protected void setUp()
        throws Exception
    {
        // call super
        super.setUp();
        // copy the tests nexus.xml and security.xml to the correct location
        this.copyTestConfigToPlace();
    }

    private void copyTestConfigToPlace()
        throws FileNotFoundException,
            IOException
    {   
        InputStream nexusConf = null;
        InputStream securityConf = null;
        
        try
        {
            nexusConf = Thread.currentThread().getContextClassLoader().getResourceAsStream( "nexus.xml" );
            IOUtil.copy(
                nexusConf,
                new FileOutputStream( getNexusConfiguration() ) );
    
            securityConf = Thread.currentThread().getContextClassLoader().getResourceAsStream( "security.xml" );
            IOUtil.copy(
                securityConf,
                new FileOutputStream( getNexusSecurityConfiguration() ) );
        }
        finally
        {   
            IOUtil.close( nexusConf );
            IOUtil.close( securityConf );
            
        }
    }
}
