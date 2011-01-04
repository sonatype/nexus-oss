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
package org.sonatype.nexus.scheduling;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

public class DefaultNexusSchedulerTest
    extends AbstractNexusTestCase
{
    private NexusScheduler nexusScheduler;

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        // IT IS NEEDED FROM NOW ON!
        return true;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusScheduler = lookup( NexusScheduler.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testDoubleSubmission()
        throws Exception
    {
        DummyWaitingNexusTask rt1 =
            (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class.getName() );

        rt1.setAllowConcurrentSubmission( true );

        ScheduledTask<?> t1 = nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 =
            (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class.getName() );

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
        DummyWaitingNexusTask rt1 =
            (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class.getName() );

        rt1.setAllowConcurrentSubmission( true );

        ScheduledTask<?> t1 = nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 =
            (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class.getName() );

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

    public void testGetAsThreadJoinner()
        throws Exception
    {
        DummyWaitingNexusTask rt =
            (DummyWaitingNexusTask) lookup( SchedulerTask.class, DummyWaitingNexusTask.class.getName() );
        rt.setResult( "result" );
        rt.setSleepTime( 1000 );
        rt.setAllowConcurrentExecution( true );
        rt.setAllowConcurrentSubmission( true );
        long start = System.currentTimeMillis();
        ScheduledTask<Object> schedule = nexusScheduler.submit( "getTester", rt );
        assertEquals( "Invalid return from schedule.get() after " + ( (double) System.currentTimeMillis() - start )
            / 1000, "result", schedule.get() );

        double took = ( (double) System.currentTimeMillis() - start ) / 1000;
        assertTrue( took > 1 );
    }

    public void testGetAsThreadJoinnerException()
        throws Exception
    {
        ExceptionerNexusTask rt = nexusScheduler.createTaskInstance( ExceptionerNexusTask.class );
        rt.setSleepTime( 1000 );
        rt.setAllowConcurrentExecution( true );
        rt.setAllowConcurrentSubmission( true );
        long start = System.currentTimeMillis();
        ScheduledTask<Object> schedule = nexusScheduler.submit( "getException", rt );
        try
        {
            assertEquals( "Invalid return from schedule.get() after " + ( (double) System.currentTimeMillis() - start )
                / 1000, "result", schedule.get() );
            fail("Should throw error");
        }
        catch ( ExecutionException e )
        {
            assertEquals( RuntimeException.class, e.getCause().getClass() );
            assertEquals( "Error", e.getCause().getMessage() );
        }

        double took = ( (double) System.currentTimeMillis() - start ) / 1000;
        assertTrue( took > 1 );
    }
}
