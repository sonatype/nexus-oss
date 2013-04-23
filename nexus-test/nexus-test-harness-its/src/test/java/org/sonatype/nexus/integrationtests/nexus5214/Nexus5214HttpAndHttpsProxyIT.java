/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus5214;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.ITGroups.PROXY;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;

/**
 * ITs related to which proxy is used (global or repository level defined ones), depending on url scheme (http / https).
 *
 * @since 2.5
 */
public class Nexus5214HttpAndHttpsProxyIT
    extends AbstractNexusIntegrationTest
{

    private static final int repoProxyPort;

    private static final int globalHttpProxyPort;

    private static final int globalHttpsProxyPort;

    private HttpsProxyServer repoProxy;

    private HttpsProxyServer globalHttpProxy;

    private HttpsProxyServer globalHttpsProxy;

    static
    {
        repoProxyPort = TestProperties.getInteger( "webproxy-server-port" );
        globalHttpProxyPort = TestProperties.getInteger( "email-server-port" );
        globalHttpsProxyPort = TestProperties.getInteger( "jira-server-port" );
    }

    @Before
    public void initWebProxies()
        throws Exception
    {
        repoProxy = new HttpsProxyServer();
        repoProxy.setPort( repoProxyPort );
        repoProxy.initialize();

        globalHttpProxy = new HttpsProxyServer();
        globalHttpProxy.setPort( globalHttpProxyPort );
        globalHttpProxy.initialize();

        globalHttpsProxy = new HttpsProxyServer();
        globalHttpsProxy.setPort( globalHttpsProxyPort );
        globalHttpsProxy.initialize();
    }

    @After
    public void stopWebProxies()
        throws Exception
    {
        if ( repoProxy != null )
        {
            repoProxy.stop();
        }

        if ( globalHttpProxy != null )
        {
            globalHttpProxy.stop();
        }

        if ( globalHttpsProxy != null )
        {
            globalHttpsProxy.stop();
        }
    }

    /**
     * Given:
     * - no global HTTP proxy
     * - no global HTTPS proxy
     * - repo proxy
     * <p/>
     * Verify that repository level configured proxy is used for an HTTP url.
     */
    @Test
    @Category( PROXY.class )
    public void rProxy_httpUrl()
        throws Exception
    {
        disableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        repoProxy.start();
        downloadArtifact( "with-http-proxy-http-url" );
    }

    /**
     * Given:
     * - no global HTTP proxy
     * - no global HTTPS proxy
     * - repo proxy
     * <p/>
     * Verify that repository level configured proxy is used for an HTTPS url.
     */
    @Test
    @Category( PROXY.class )
    public void rProxy_httpsUrl()
        throws Exception
    {
        disableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        repoProxy.start();
        downloadArtifact( "with-http-proxy-https-url" );
    }

    /**
     * Given:
     * - no global HTTP proxy
     * - no global HTTPS proxy
     * - no repo proxy
     * <p/>
     * Verify that no proxy is used for an HTTP url.
     */
    @Test
    @Category( PROXY.class )
    public void httpUrl()
        throws Exception
    {
        disableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        downloadArtifact( "without-http-proxy-http-url" );
    }

    /**
     * Given:
     * - no global HTTP proxy
     * - no global HTTPS proxy
     * - no repo proxy
     * <p/>
     * Verify that no proxy is used for an HTTPS url.
     */
    @Test
    @Category( PROXY.class )
    public void httpsUrl()
        throws Exception
    {
        disableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        downloadArtifact( "without-http-proxy-https-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - no global HTTPS proxy
     * - no repo proxy
     * <p/>
     * Verify that global HTTP proxy is used for an HTTP url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_httpUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        globalHttpProxy.start();
        downloadArtifact( "without-http-proxy-http-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - no global HTTPS proxy
     * - no repo proxy
     * <p/>
     * Verify that global HTTP proxy is used for an HTTPS url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_httpsUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        globalHttpProxy.start();
        downloadArtifact( "without-http-proxy-https-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - no global HTTPS proxy
     * - repo proxy
     * <p/>
     * Verify that repo HTTP proxy is used for an HTTP url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_rProxy_httpUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        repoProxy.start();
        downloadArtifact( "with-http-proxy-http-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - no global HTTPS proxy
     * - repo proxy
     * <p/>
     * Verify that repo HTTP proxy is used for an HTTPS url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_rProxy_httpsUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        disableGlobalHttpsProxy();
        repoProxy.start();
        downloadArtifact( "with-http-proxy-https-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - global HTTPS proxy
     * - repo proxy
     * <p/>
     * Verify that repo HTTP proxy is used for an HTTP url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_gHttps_rProxy_httpUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        enableGlobalHttpsProxy();
        repoProxy.start();
        downloadArtifact( "with-http-proxy-http-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - global HTTPS proxy
     * - repo proxy
     * <p/>
     * Verify that repo HTTP proxy is used for an HTTPS url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_gHttps_rProxy_httpsUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        enableGlobalHttpsProxy();
        repoProxy.start();
        downloadArtifact( "with-http-proxy-https-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - global HTTPS proxy
     * - no repo proxy
     * <p/>
     * Verify that global HTTP proxy is used for an HTTP url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_gHttps_httpUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        enableGlobalHttpsProxy();
        globalHttpProxy.start();
        downloadArtifact( "without-http-proxy-http-url" );
    }

    /**
     * Given:
     * - global HTTP proxy
     * - global HTTPS proxy
     * - no repo proxy
     * <p/>
     * Verify that global HTTPS proxy is used for an HTTPS url.
     */
    @Test
    @Category( PROXY.class )
    public void gHttp_gHttps_httpsUrl()
        throws Exception
    {
        enableGlobalHttpProxy();
        enableGlobalHttpsProxy();
        globalHttpsProxy.start();
        downloadArtifact( "without-http-proxy-https-url" );
    }

    private void enableGlobalHttpProxy()
        throws IOException
    {
        final GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        settings.setGlobalHttpProxySettings( new RemoteHttpProxySettings() );
        settings.getGlobalHttpProxySettings().setProxyHostname( "localhost" );
        settings.getGlobalHttpProxySettings().setProxyPort( globalHttpProxyPort );

        SettingsMessageUtil.save( settings );
    }

    private void disableGlobalHttpProxy()
        throws IOException
    {
        final GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        if ( settings.getGlobalHttpProxySettings() != null )
        {
            settings.setGlobalHttpProxySettings( null );

            SettingsMessageUtil.save( settings );
        }
    }

    private void enableGlobalHttpsProxy()
        throws IOException
    {
        final GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        settings.setGlobalHttpsProxySettings( new RemoteHttpProxySettings() );
        settings.getGlobalHttpsProxySettings().setProxyHostname( "localhost" );
        settings.getGlobalHttpsProxySettings().setProxyPort( globalHttpsProxyPort );

        SettingsMessageUtil.save( settings );
    }

    private void disableGlobalHttpsProxy()
        throws IOException
    {
        final GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        if ( settings.getGlobalHttpsProxySettings() != null )
        {
            settings.setGlobalHttpsProxySettings( null );

            SettingsMessageUtil.save( settings );
        }
    }

    private void downloadArtifact( final String repositoryId )
        throws IOException
    {
        downloadArtifact(
            getNexusTestRepoUrl( repositoryId ),
            "aopalliance", "aopalliance", "1.0", "pom", null, "target/downloads"
        );
        deleteFromRepository( repositoryId, "aopalliance" );
    }

}
