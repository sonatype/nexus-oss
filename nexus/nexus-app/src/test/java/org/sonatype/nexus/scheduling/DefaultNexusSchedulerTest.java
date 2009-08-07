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
package org.sonatype.nexus.scheduling;

import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

public class DefaultNexusSchedulerTest
    extends AbstractNexusTestCase
{
    private NexusScheduler nexusScheduler;

    protected boolean loadConfigurationAtSetUp()
    {
        // IT IS NEEDED FROM NOW ON!
        return true;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusScheduler = lookup( NexusScheduler.class );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testDoubleSubmission()
        throws Exception
    {
        DummyWaitingNexusTask rt1 = (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class
            .getName() );

        rt1.setAllowConcurrentSubmission( true );

        ScheduledTask<?> t1 = nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 = (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class
            .getName() );

        rt2.setAllowConcurrentSubmission( false );

        ScheduledTask<?> t2 = null;
        
        try
        {
            // the second submission should fail, since there is already a task of this type submitted
            t2 = nexusScheduler.submit( "test2", rt2 );

            fail();
        }
        catch ( RejectedExecutionException e )
        {
            // cool
        }
        finally
        {
            t1.cancel();
            
            if ( t2 != null )
            {
                t2.cancel();
            }
        }
    }

    public void testDoubleSubmissionAllowed()
        throws Exception
    {
        DummyWaitingNexusTask rt1 = (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class
            .getName() );

        rt1.setAllowConcurrentSubmission( true );

        ScheduledTask<?> t1 = nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 = (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class
            .getName() );

        rt2.setAllowConcurrentSubmission( true );

        ScheduledTask<?> t2 = null;
        try
        {
            // the second submission should succeed, since it is allowed
            t2 = nexusScheduler.submit( "test2", rt2 );
        }
        catch ( RejectedExecutionException e )
        {
            fail( "Concurrent submission should succeed." );
        }
        finally
        {
            t1.cancel();
            
            if ( t2 != null )
            {
                t2.cancel();
            }
        }
    }
}
