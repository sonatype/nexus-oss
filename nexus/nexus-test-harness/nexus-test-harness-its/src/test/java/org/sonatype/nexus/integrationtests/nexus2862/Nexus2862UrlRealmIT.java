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
package org.sonatype.nexus.integrationtests.nexus2862;

import java.io.IOException;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus2862UrlRealmIT
    extends AbstractNexusIntegrationTest
{

    private static Integer proxyPort;

    private static AuthenticationServer server;


    static
    {
        proxyPort = TestProperties.getInteger( "proxy.server.port" );
        // System.setProperty( "plexus.authentication-url", "http://localhost:" + proxyPort );
        // System.setProperty( "plexus.url-authentication-default-role", "admin" );
        // System.setProperty( "plexus.url-authentication-email-domain", "sonatype.com" );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        this.copyConfigFile( "url-realm.xml", WORK_CONF_DIR );
    }

    @BeforeClass
    public static void startServer()
        throws Exception
    {
        proxyPort = TestProperties.getInteger( "proxy.server.port" );
        // System.setProperty( "plexus.authentication-url", "http://localhost:" + proxyPort );
        // System.setProperty( "plexus.url-authentication-default-role", "admin" );
        // System.setProperty( "plexus.url-authentication-email-domain", "sonatype.com" );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        server = new AuthenticationServer( proxyPort );
        server.addUser( "juka", "juk@", "admin" );
        server.start();
    }

    @AfterClass
    public static void stopServer()
        throws Exception
    {
        server.stop();
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();
        resource.getSecurityRealms().clear();
        resource.getSecurityRealms().add( "url" );
        Status status = SettingsMessageUtil.save( resource );
        Assert.assertTrue( status.isSuccess() );
    }

    @AfterMethod
    public void cleanAccessedUris()
    {
        server.getAccessedUri().clear();
    }

    @Test
    public void loginUrlRealm()
        throws IOException
    {
        Assert.assertTrue( UserCreationUtil.login( "juka", "juk@" ).isSuccess() );

        Assert.assertFalse( server.getAccessedUri().isEmpty() );

        Assert.assertTrue( UserCreationUtil.logout().isSuccess() );
    }

    @Test
    public void wrongPassword()
        throws IOException
    {
        Status status = UserCreationUtil.login( "juka", "juka" );
        Assert.assertFalse( status.isSuccess(), status + "" );

        Assert.assertTrue( UserCreationUtil.logout().isSuccess() );
    }

    @Test
    public void wrongUsername()
        throws IOException
    {
        Status status = UserCreationUtil.login( "anuser", "juka" );
        Assert.assertFalse( status.isSuccess(), status + "" );

        Assert.assertTrue( UserCreationUtil.logout().isSuccess() );
    }

}
