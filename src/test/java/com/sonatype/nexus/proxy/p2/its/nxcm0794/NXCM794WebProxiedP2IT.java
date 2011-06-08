/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its.nxcm0794;

import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.nexus.test.utils.TestProperties;

import com.sonatype.nexus.proxy.p2.its.nxcm0072.NXCM72P2ProxyIT;

public class NXCM794WebProxiedP2IT
    extends NXCM72P2ProxyIT
{

    private static String baseProxyURL;

    protected ProxyServer server;

    static
    {
        baseProxyURL = TestProperties.getString( "proxy.repo.base.url" );
    }

    @Before
    public void startWebProxy()
        throws Exception
    {
        server = (ProxyServer) lookup( ProxyServer.ROLE );
        server.start();

        // ensuring the proxy is working!!!
        Assert.assertTrue( downloadFile( new URL( baseProxyURL + "p2repo/artifacts.xml" ),
                                         "./target/nxcm794/artifacts.xml.temp" ).exists() );
    }

    @After
    public void stopWebProxy()
        throws Exception
    {
        server.stop();
    }

    @Override
    @Test
    public void p2repository()
        throws Exception
    {
        super.p2repository();

        String artifactUrl = baseProxyURL + "p2repo/features/com.sonatype.nexus.p2.its.feature_1.0.0.jar";
        Assert.assertTrue( "Proxy was not accessed: " + artifactUrl + " - accessed: " + server.getAccessedUris(),
                           server.getAccessedUris().contains( artifactUrl ) );

        artifactUrl = baseProxyURL + "p2repo/plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar";
        Assert.assertTrue( "Proxy was not accessed: " + artifactUrl + " - accessed: " + server.getAccessedUris(),
                           server.getAccessedUris().contains( artifactUrl ) );
    }
}
