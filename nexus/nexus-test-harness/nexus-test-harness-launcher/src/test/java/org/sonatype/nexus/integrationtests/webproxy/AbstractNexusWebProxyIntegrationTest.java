/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.webproxy;

import org.junit.After;
import org.junit.Before;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public abstract class AbstractNexusWebProxyIntegrationTest
    extends AbstractNexusProxyIntegrationTest
{

    protected static final int webProxyPort;

    protected ProxyServer server;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy.server.port" );
    }

    @Before
    public void startWebProxy()
        throws Exception
    {
        server = (ProxyServer) lookup( ProxyServer.ROLE );
        server.start();
    }

    @After
    public void stopWebProxy()
        throws Exception
    {
        server.stop();
    }

}
