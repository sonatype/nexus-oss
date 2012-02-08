/**
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
package org.sonatype.nexus.proxy.walker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.walker.WalkerThrottleController.ThrottleInfo;
import org.sonatype.nexus.util.ConstantNumberSequence;
import org.sonatype.nexus.util.FibonacciNumberSequence;
import org.sonatype.nexus.util.NumberSequence;

/**
 * Test for fixed rate walker throttle controller.
 * 
 * @author cstamas
 */
public class FixedRateWalkerThrottleControllerTest
{
    protected FixedRateWalkerThrottleController fixedRateWalkerThrottleController;

    @Test
    public void testDoesItHelpAtAll()
    {
        // set unrealistic TPS and we do "little" (1ms) in processItem method (to get the ceiling to compare with)
        final int measuredTpsUnreal = performAndMeasureActualTps( 100000000, new ConstantNumberSequence( 1 ) );
        // set 500 TPS and we do "little" (1ms) in processItem method
        final int measuredTps500 = performAndMeasureActualTps( 500, new ConstantNumberSequence( 1 ) );
        // set 200 TPS and we do "little" (1ms) in processItem method
        final int measuredTps200 = performAndMeasureActualTps( 200, new ConstantNumberSequence( 1 ) );

        assertThat( "TPS500 should less than Unreal one", measuredTps500, lessThan( measuredTpsUnreal ) );
        assertThat( "TPS200 should less than TPS500 one", measuredTps200, lessThan( measuredTps500 ) );
    }

    // ==

    protected int performAndMeasureActualTps( final int wantedTps, final NumberSequence loadChange )
    {
        fixedRateWalkerThrottleController =
            new FixedRateWalkerThrottleController( wantedTps, new FibonacciNumberSequence( 1 ) );
        fixedRateWalkerThrottleController.setSliceSize( 1 );

        final TestThrottleInfo info = new TestThrottleInfo();
        final WalkerContext context = Mockito.mock( WalkerContext.class );
        final int iterationCount = 1000;
        final long startTime = System.currentTimeMillis();
        fixedRateWalkerThrottleController.walkStarted( context );
        for ( int i = 0; i < iterationCount; i++ )
        {
            info.simulateInvocation( loadChange.next() );
            long sleepTime = fixedRateWalkerThrottleController.throttleTime( info );
            sleep( sleepTime ); // sleep as much as throttle controller says to sleep
        }
        fixedRateWalkerThrottleController.walkEnded( context, info );

        final int measuredTps =
            fixedRateWalkerThrottleController.calculateCPS( iterationCount, System.currentTimeMillis() - startTime );

        // System.out.println( "MeasuredTps=" + measuredTps );
        // System.out.println( "lastSliceTps=" + fixedRateWalkerThrottleController.getLastSliceTps() );
        // System.out.println( "GlobalAvgTps=" + fixedRateWalkerThrottleController.getGlobalAverageTps() );
        // System.out.println( "GlobalMaxTps=" + fixedRateWalkerThrottleController.getGlobalMaximumTps() );

        return measuredTps;
    }

    // ==

    protected static void sleep( long millis )
    {
        try
        {
            Thread.sleep( millis );
        }
        catch ( InterruptedException e )
        {
            // need to kill test too
            throw new RuntimeException( e );
        }
    }

    protected static class TestThrottleInfo
        implements ThrottleInfo
    {
        private final long started;

        private long totalProcessItemSpentMillis;

        private long totalProcessItemInvocationCount;

        public TestThrottleInfo()
        {
            this.started = System.currentTimeMillis();
            this.totalProcessItemSpentMillis = 0;
            this.totalProcessItemInvocationCount = 0;
        }

        public void simulateInvocation( final long spentTimeInProcessItem )
        {
            // we need to sleep to keep getTotalTimeWalking() and totalProcessItemSpentMillis aligned
            sleep( spentTimeInProcessItem );
            totalProcessItemSpentMillis = totalProcessItemSpentMillis + spentTimeInProcessItem;
            totalProcessItemInvocationCount++;
        }

        @Override
        public long getTotalProcessItemSpentMillis()
        {
            return totalProcessItemSpentMillis;
        }

        @Override
        public long getTotalProcessItemInvocationCount()
        {
            return totalProcessItemInvocationCount;
        }

        @Override
        public long getTotalTimeWalking()
        {
            return System.currentTimeMillis() - started;
        }
    }
}
