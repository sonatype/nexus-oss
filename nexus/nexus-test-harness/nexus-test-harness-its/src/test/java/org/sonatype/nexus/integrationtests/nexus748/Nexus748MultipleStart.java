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
package org.sonatype.nexus.integrationtests.nexus748;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus748MultipleStart
    extends AbstractNexusIntegrationTest
{

    protected static Logger logger = Logger.getLogger( Nexus748MultipleStart.class );

    @Test
    public void multipleStartTest()
        throws Exception
    {

        StopWatch stopWatch = new StopWatch();

        NexusClient client = (NexusClient) getITPlexusContainer().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( AbstractNexusIntegrationTest.nexusBaseUrl, context.getAdminUsername(),
                        context.getAdminPassword() );

        // enable security
        getNexusConfigUtil().enableSecurity( true );

        List<Long> startTimes = new ArrayList<Long>();

        for ( int ii = 0; ii < 10; ii++ )
        {
            // start the timer
            stopWatch.reset();
            stopWatch.start();

            // start
            getNexusStatusUtil().start( getTestId() );

            Assert.assertTrue( client.isNexusStarted( true ) );

            // get the time
            stopWatch.stop();

            // stop
            getNexusStatusUtil().stop();

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
