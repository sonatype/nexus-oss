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
package org.sonatype.nexus.integrationtests.nexus748;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.NexusStatusUtil;

public class Nexus748MultipleStart
{

    protected static Logger logger = Logger.getLogger( Nexus748MultipleStart.class );

    @Test
    public void multipleStartTest()
        throws Exception
    {

        StopWatch stopWatch = new StopWatch();

        NexusClient client = (NexusClient) AbstractNexusIntegrationTest.getStaticITPlexusContainer().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( AbstractNexusIntegrationTest.nexusBaseUrl, context.getAdminUsername(),
                        context.getAdminPassword() );

        // enable security
        NexusConfigUtil.enableSecurity( true );

        List<Long> startTimes = new ArrayList<Long>();

        for ( int ii = 0; ii < 10; ii++ )
        {
            // start the timer
            stopWatch.reset();
            stopWatch.start();

            // start
            NexusStatusUtil.doHardStart();

            Assert.assertTrue( client.isNexusStarted( true ) );

            // get the time
            stopWatch.stop();

            // stop
            NexusStatusUtil.doHardStop();

            startTimes.add( stopWatch.getTime() );
        }

        logger.info( "\n\n**************\n Start times: \n**************" );

        logger.info( "Iter\tTime" );
        for ( int ii = 0; ii < startTimes.size(); ii++ )
        {
            Long startTime = startTimes.get( ii );
            logger.info( " " + ( ii + 1 ) + "\t " + ( startTime / 1000.0 ) + "sec." );

        }

    }
}
