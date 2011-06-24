/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm0794;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.test.utils.TestProperties;

public class NXCM0794WebProxiedP2IT
    extends AbstractNexusProxyP2IT
{

    private static String baseProxyURL;

    protected ProxyServer server;

    static
    {
        baseProxyURL = TestProperties.getString( "proxy.repo.base.url" );
    }

    public NXCM0794WebProxiedP2IT()
    {
        super( "nxcm0794" );
    }

    @Before
    public void startWebProxy()
        throws Exception
    {
        server = (ProxyServer) lookup( ProxyServer.ROLE );
        server.start();

        // ensuring the proxy is working!!!
        Assert.assertTrue( downloadFile( new URL( baseProxyURL + "nxcm0794/artifacts.xml" ),
            "./target/downloads/nxcm0794/artifacts.xml.temp" ).exists() );
    }

    @After
    public void stopWebProxy()
        throws Exception
    {
        server.stop();
    }

    @Test
    public void test()
        throws Exception
    {
        final String nexusTestRepoUrl = getNexusTestRepoUrl();

        final File installDir = new File( "target/eclipse/nxcm0794" );

        installUsingP2( nexusTestRepoUrl, "com.sonatype.nexus.p2.its.feature.feature.group",
            installDir.getCanonicalPath() );

        final File feature = new File( installDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0" );
        Assert.assertTrue( feature.exists() && feature.isDirectory() );

        final File bundle = new File( installDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" );
        Assert.assertTrue( bundle.canRead() );

        String artifactUrl = baseProxyURL + "nxcm0794/features/com.sonatype.nexus.p2.its.feature_1.0.0.jar";
        Assert.assertTrue( "Proxy was not accessed: " + artifactUrl + " - accessed: " + server.getAccessedUris(),
            server.getAccessedUris().contains( artifactUrl ) );

        artifactUrl = baseProxyURL + "nxcm0794/plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar";
        Assert.assertTrue( "Proxy was not accessed: " + artifactUrl + " - accessed: " + server.getAccessedUris(),
            server.getAccessedUris().contains( artifactUrl ) );
    }
}
