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
package org.sonatype.nexus.proxy;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.httpclient.CustomMultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.DefaultRemoteProxySettings;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.HttpClientProxyUtil;

public class NonProxyHostsTest
    extends AbstractProxyTestEnvironment
{

    private ProxyServer proxyServer;

    private ServletServer servletServer;

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    @Override
    public void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        ctx.put( "proxy-server-port", Integer.toString( this.getAFreePort() ) );
        ctx.put( "servlet-server-port", Integer.toString( this.getAFreePort() ) );
        ctx.put( "server-content-path", "target/test-reposes/repo1/" );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        // Lesson1 - know what you want
        // usually, you want to prepare the "playground", to be able to play by knowing the rules
        FileUtils.copyDirectoryStructure( new File( getBasedir(), "target/test-classes/repo1" ), new File(
            getBasedir(), "target/test-reposes/repo1" ) );

        proxyServer = lookup( ProxyServer.class );
        proxyServer.start();

        servletServer = lookup( ServletServer.class );
        servletServer.start();
    }

    public void tearDown()
        throws Exception
    {
        super.tearDown();
        if ( proxyServer != null )
        {
            proxyServer.stop();
        }

        if ( servletServer != null )
        {
            servletServer.stop();
        }

        // Lesson2 - be polite
        // usually you also want to clean up
        FileUtils.forceDelete( new File( getBasedir(), "target/test-reposes/repo1" ) );
    }

    @Test
    public void testGlobalWithNoNonProxyHosts()
        throws Exception
    {
        ApplicationConfiguration nexusConfig = this.lookup( ApplicationConfiguration.class );

        RemoteProxySettings rps = new DefaultRemoteProxySettings();
        rps.setHostname( "localhost" );
        rps.setPort( this.proxyServer.getPort() );
        nexusConfig.getGlobalRemoteStorageContext().setRemoteProxySettings( rps );

        // now we need to manually setup a http client;
        HttpClient httpClient = new HttpClient( new CustomMultiThreadedHttpConnectionManager() );
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, nexusConfig.getGlobalRemoteStorageContext(), null );

        String url = this.servletServer.getUrl( "remote/" );
        GetMethod get = new GetMethod( url );
        Assert.assertEquals( "status for URL: " + url, 200, httpClient.executeMethod( get ) );

        Assert.assertTrue(
            "AccessUris does not contain: " + url + " actual uris: " + this.proxyServer.getAccessedUris(),
            this.proxyServer.getAccessedUris().contains( url ) );

    }

    @Test
    public void testGlobalWithNonProxyHosts()
        throws Exception
    {
        ApplicationConfiguration nexusConfig = this.lookup( ApplicationConfiguration.class );

        RemoteProxySettings rps = new DefaultRemoteProxySettings();
        rps.setHostname( "localhost" );
        rps.setPort( this.proxyServer.getPort() );
        rps.getNonProxyHosts().add( "localhost" );
        nexusConfig.getGlobalRemoteStorageContext().setRemoteProxySettings( rps );

        // now we need to manually setup a http client;
        HttpClient httpClient = new HttpClient( new CustomMultiThreadedHttpConnectionManager() );
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, nexusConfig.getGlobalRemoteStorageContext(), null );

        String url = this.servletServer.getUrl( "remote/" );
        GetMethod get = new GetMethod( url );
        Assert.assertEquals( "status for URL: " + url, 200, httpClient.executeMethod( get ) );
        Assert.assertEquals( "AccessUris should be empty, actual uris: " + this.proxyServer.getAccessedUris(), 0,
            this.proxyServer.getAccessedUris().size() );
    }

    @Test
    public void testGlobalWithNonProxyHostsRegex()
        throws Exception
    {
        ApplicationConfiguration nexusConfig = this.lookup( ApplicationConfiguration.class );

        RemoteProxySettings rps = new DefaultRemoteProxySettings();
        rps.setHostname( "localhost" );
        rps.setPort( this.proxyServer.getPort() );
        rps.getNonProxyHosts().add( ".*host" );
        nexusConfig.getGlobalRemoteStorageContext().setRemoteProxySettings( rps );

        // now we need to manually setup a http client;
        HttpClient httpClient = new HttpClient( new CustomMultiThreadedHttpConnectionManager() );
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, nexusConfig.getGlobalRemoteStorageContext(), null );

        String url = this.servletServer.getUrl( "remote/" );
        GetMethod get = new GetMethod( url );
        Assert.assertEquals( "status for URL: " + url, 200, httpClient.executeMethod( get ) );
        Assert.assertEquals( "AccessUris should be empty, actual uris: " + this.proxyServer.getAccessedUris(), 0,
            this.proxyServer.getAccessedUris().size() );
    }

    private int getAFreePort()
    {
        int port = 0;
        ServerSocket serverSocket = null;
        try
        {
            serverSocket = new ServerSocket( 0 );
            port = serverSocket.getLocalPort();
        }
        catch ( IOException e )
        {
            Assert.fail( "Failed to find free port." + e.getMessage() );
        }
        finally
        {
            try
            {
                serverSocket.close();
            }
            catch ( IOException e )
            {
                Assert.fail( "Failed to close port." + e.getMessage() );
            }
        }
        return port;
    }

    @Test
    public void testRepoRemoteContext()
        throws Exception
    {
        ApplicationConfiguration nexusConfig = this.lookup( ApplicationConfiguration.class );

        RemoteProxySettings rps = new DefaultRemoteProxySettings();
        rps.setHostname( "localhost" );
        rps.setPort( this.proxyServer.getPort() );
        rps.getNonProxyHosts().add( ".*host" );
        nexusConfig.getGlobalRemoteStorageContext().setRemoteProxySettings( rps );

        ProxyRepository repo = (ProxyRepository) getRepositoryRegistry().getRepository( "remote" );
        Assert.assertNotNull( repo.getRemoteStorageContext() );

        Assert.assertEquals( 1, repo.getRemoteStorageContext().getRemoteProxySettings().getNonProxyHosts().size() );
        Assert.assertEquals( nexusConfig.getGlobalRemoteStorageContext().getRemoteProxySettings().getNonProxyHosts(),
            repo.getRemoteStorageContext().getRemoteProxySettings().getNonProxyHosts() );
    }

}
