/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.simple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.realms.AbstractRealmWithSecuritySystemTest;
import org.sonatype.security.realms.tools.ConfigurationManager;

public class SimpleRealmTest
    extends AbstractRealmWithSecuritySystemTest
{
    private final File confdir;

    public SimpleRealmTest()
    {
        this.confdir = new File( "target/app-conf" );
    }

    @Override
    protected File getConfDir()
    {
        return confdir;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        getConfDir().mkdirs();
        // copy the tests nexus.xml and security.xml to the correct location
        copyTestConfigToPlace();
        // restart security
        lookup( ConfigurationManager.class, "legacydefault" ).clearCache();
        lookup( SecuritySystem.class ).start();
    }

    private void copyTestConfigToPlace()
        throws FileNotFoundException, IOException
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
            IOUtil.copy( security, securityOut );

            securityConf =
                Thread.currentThread().getContextClassLoader().getResourceAsStream( "security-configuration.xml" );
            securityConfOut = new FileOutputStream( new File( confdir, "security-configuration.xml" ) );
            IOUtil.copy( securityConf, securityConfOut );
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

    // Realm Tests
    /**
     * Test authentication with a valid user and password.
     * 
     * @throws Exception
     */
    @Test
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
    @Test
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
    @Test
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
     * Test authorization using the NexusMethodAuthorizingRealm. <BR/>
     * Take a look a the security.xml in src/test/resources this maps the users in the UserStore to nexus
     * roles/privileges
     * 
     * @throws Exception
     */
    @Test
    public void testPrivileges()
        throws Exception
    {
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );

        PrincipalCollection principal = new SimplePrincipalCollection( "admin-simple", new SimpleRealm().getName() );

        // test one of the privleges that the admin user has Repositories - (create,read)
        Assert.assertTrue( plexusSecurity.isPermitted( principal, "nexus:repositories:create" ) );
    }

    /**
     * Tests a valid privilege for an invalid user
     * 
     * @throws Exception
     */
    @Test
    public void testPrivilegesInvalidUser()
        throws Exception
    {
        SecuritySystem plexusSecurity = this.lookup( SecuritySystem.class );

        PrincipalCollection principal = new SimplePrincipalCollection( "INVALID", SecuritySystem.class.getSimpleName() );

        // test one of the privleges
        Assert.assertFalse( plexusSecurity.isPermitted( principal, "nexus:repositories:create" ) );// Repositories -
        // (create,read)

    }
}
