/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.client.nexus758;

import static org.sonatype.nexus.test.utils.NexusStatusUtil.doHardStart;
import static org.sonatype.nexus.test.utils.NexusStatusUtil.doHardStop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;

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
        client = (NexusClient) AbstractNexusIntegrationTest.getStaticContainer().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( AbstractNexusIntegrationTest.baseNexusUrl, context.getAdminUsername(),
                        context.getAdminPassword() );
    }

    @Test
    public void hardRestarts()
        throws Exception
    {

        // this could be done using a for, but I wanna to know how may times it run just looking to stack trace
        // 1
        doHardStart();
        doHardStop();

        // 2
        doHardStart();
        doHardStop();

        // 3
        doHardStart();
        doHardStop();

        // 4
        doHardStart();
        doHardStop();

        // 5
        doHardStart();
        doHardStop();

        // 6
        doHardStart();
        doHardStop();

        // 7
        doHardStart();
        doHardStop();

        // 8
        doHardStart();
        doHardStop();

        // 9
        doHardStart();
        doHardStop();

        // 10
        doHardStart();
        doHardStop();

    }

    // cstamas - disabled it, it tests a feature not present???
    //@Test
    /*
    public void clientRestarts()
        throws Exception
    {
        doHardStart();

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
    */

}
