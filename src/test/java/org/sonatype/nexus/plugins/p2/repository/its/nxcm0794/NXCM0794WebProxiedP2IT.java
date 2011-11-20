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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm0794;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.net.URL;

import org.hamcrest.Matchers;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NXCM0794WebProxiedP2IT
    extends AbstractNexusProxyP2IT
{

    private static String baseProxyURL;

    protected ProxyServer webProxyServer;

    static
    {
        baseProxyURL = TestProperties.getString( "proxy.repo.base.url" );
    }

    public NXCM0794WebProxiedP2IT()
    {
        super( "nxcm0794" );
    }

    @BeforeMethod( alwaysRun = true )
    public void startWebProxy()
        throws Exception
    {
        webProxyServer = (ProxyServer) lookup( ProxyServer.ROLE );
        webProxyServer.start();

        // ensuring the proxy is working!!!
        assertThat(
            downloadFile(
                new URL( baseProxyURL + "nxcm0794/artifacts.xml" ),
                "./target/downloads/nxcm0794/artifacts.xml.temp"
            ),
            exists()
        );
    }

    @AfterMethod( alwaysRun = true )
    public void stopWebProxy()
        throws Exception
    {
        if ( webProxyServer != null )
        {
            webProxyServer.stop();
            webProxyServer = null;
        }
    }

    @Test
    public void test()
        throws Exception
    {
        installAndVerifyP2Feature();

        assertThat(
            webProxyServer.getAccessedUris(),
            hasItem( baseProxyURL + "nxcm0794/features/com.sonatype.nexus.p2.its.feature_1.0.0.jar" )
        );

        assertThat(
            webProxyServer.getAccessedUris(),
            hasItem( baseProxyURL + "nxcm0794/plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" )
        );
    }

}
