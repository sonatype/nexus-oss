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
package org.sonatype.nexus.integrationtests.nexus2862;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.test.utils.StatusMatchers.isSuccess;

import java.io.IOException;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.UserCreationUtil;


/**
 * Ad1: what does this test do here? Ad2: UrlRealm leaks badly
 * TODO: as of introduction of restlet1x plugin, this IT fails for unknown reasons.
 * Disabled for now.
 *
 * @author cstamas
 *
 */
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

    @After
    public void cleanAccessedUris()
    {
        server.getAccessedUri().clear();
    }

    @Test
    public void loginUrlRealm()
        throws IOException
    {
        Status status = UserCreationUtil.login( "juka", "juk@" );
        assertThat( status, isSuccess() );
        assertThat( server.getAccessedUri(), not( IsEmptyCollection.<String> empty() ) );

        UserCreationUtil.logout();
    }

    @Test
    public void wrongPassword()
        throws IOException
    {
        Status status = UserCreationUtil.login( "juka", "juka" );
        assertThat( status, not( isSuccess() ) );

        UserCreationUtil.logout();
    }

    @Test
    public void wrongUsername()
        throws IOException
    {
        Status status = UserCreationUtil.login( "anuser", "juka" );
        assertThat( status, not( isSuccess() ) );

        UserCreationUtil.logout();
    }

}
