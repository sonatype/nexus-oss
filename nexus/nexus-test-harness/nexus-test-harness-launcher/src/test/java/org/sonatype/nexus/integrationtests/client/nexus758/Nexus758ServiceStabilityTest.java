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
package org.sonatype.nexus.integrationtests.client.nexus758;

import static org.sonatype.nexus.test.utils.NexusStateUtil.doClientStart;
import static org.sonatype.nexus.test.utils.NexusStateUtil.doClientStop;
import static org.sonatype.nexus.test.utils.NexusStateUtil.doHardStart;
import static org.sonatype.nexus.test.utils.NexusStateUtil.doHardStop;
import static org.sonatype.nexus.test.utils.NexusStateUtil.doSoftStart;
import static org.sonatype.nexus.test.utils.NexusStateUtil.doSoftStop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.TestProperties;

/**
 * Tests the Soft Start, Stop, Restart, and isNexusStarted methods in the rest-client.
 */
public class Nexus758ServiceStabilityTest
{
    private static NexusClient client;

    @BeforeClass
    public static void init()
        throws Exception
    {
        client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( TestProperties.getString( "nexus.base.url" ), context.getAdminUsername(),
                        context.getAdminPassword() );
    }

    @Test
    public void hardRestarts()
        throws Exception
    {

        ForkedAppBooter app;

        // this could be done using a for, but I wanna to know how may times it run just looking to stack trace
        // 1
        app = doHardStart();
        doHardStop();

        // 2
        app = doHardStart();
        doHardStop();

        // 3
        app = doHardStart();
        doHardStop();

        // 4
        app = doHardStart();
        doHardStop();

        // 5
        app = doHardStart();
        doHardStop();

        // 6
        app = doHardStart();
        doHardStop();

        // 7
        app = doHardStart();
        doHardStop();

        // 8
        app = doHardStart();
        doHardStop();

        // 9
        app = doHardStart();
        doHardStop();

        // 10
        app = doHardStart();
        doHardStop();

    }

    @Test
    public void softRestarts()
        throws Exception
    {
        ForkedAppBooter app = doHardStart();

        doSoftStop();

        // 1
        doSoftStart();
        doSoftStop();

        // 2
        doSoftStart();
        doSoftStop();

        // 3
        doSoftStart();
        doSoftStop();

        // 4
        doSoftStart();
        doSoftStop();

        // 5
        doSoftStart();
        doSoftStop();

        // 6
        doSoftStart();
        doSoftStop();

        // 7
        doSoftStart();
        doSoftStop();

        // 8
        doSoftStart();
        doSoftStop();

        // 9
        doSoftStart();
        doSoftStop();

        // 10
        doSoftStart();
        doSoftStop();

        doSoftStart();
        doHardStop();
    }

    @Test
    public void clientRestarts()
        throws Exception
    {
        ForkedAppBooter app = doHardStart();

        doClientStop();

        // 1
        doClientStart();
        doClientStop();

        // 2
        doClientStart();
        doClientStop();

        // 3
        doClientStart();
        doClientStop();

        // 4
        doClientStart();
        doClientStop();

        // 5
        doClientStart();
        doClientStop();

        // 6
        doClientStart();
        doClientStop();

        // 7
        doClientStart();
        doClientStop();

        // 8
        doClientStart();
        doClientStop();

        // 9
        doClientStart();
        doClientStop();

        // 10
        doClientStart();
        doClientStop();

        doClientStart();
        doHardStop();
    }

}
