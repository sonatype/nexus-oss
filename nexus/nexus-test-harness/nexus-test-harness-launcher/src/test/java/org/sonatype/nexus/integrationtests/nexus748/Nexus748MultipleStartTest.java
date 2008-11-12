package org.sonatype.nexus.integrationtests.nexus748;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.NexusStateUtil;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus748MultipleStartTest
{

    @Test
    public void multipleStartTest()
        throws Exception
    {

        StopWatch stopWatch = new StopWatch();

        NexusClient client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( TestProperties.getString( "nexus.base.url" ), context.getAdminUsername(),
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
            ForkedAppBooter appBooter = NexusStateUtil.doHardStart();

            Assert.assertTrue( client.isNexusStarted( true ) );

            // get the time
            stopWatch.stop();

            // stop
            NexusStateUtil.doHardStop( appBooter );

            startTimes.add( stopWatch.getTime() );
        }

        System.out.println( "\n\n**************\n Start times: \n**************" );

        System.out.println( "Iter\tTime" );
        for ( int ii = 0; ii < startTimes.size(); ii++ )
        {
            Long startTime = startTimes.get( ii );
            System.out.println( " " + ( ii + 1 ) + "\t " + ( startTime / 1000.0 ) + "sec." );

        }

    }
}
