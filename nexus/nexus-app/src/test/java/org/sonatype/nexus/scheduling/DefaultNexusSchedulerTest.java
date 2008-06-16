package org.sonatype.nexus.scheduling;

import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.AbstractNexusTestCase;

public class DefaultNexusSchedulerTest
    extends AbstractNexusTestCase
{
    private NexusScheduler nexusScheduler;

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusScheduler = (NexusScheduler) lookup( NexusScheduler.ROLE );

        nexusScheduler.startService();
    }

    protected void tearDown()
        throws Exception
    {
        nexusScheduler.stopService();

        super.tearDown();
    }

    public void testDoubleSubmission()
        throws Exception
    {
        DummyWaitingNexusTask rt1 = (DummyWaitingNexusTask) lookup( NexusTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt1.setAllowConcurrentExecution( false );

        nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 = (DummyWaitingNexusTask) lookup( NexusTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt2.setAllowConcurrentExecution( false );

        try
        {
            // the second submission should fail, since there is already a task of this type submitted
            nexusScheduler.submit( "test2", rt2 );

            fail();
        }
        catch ( RejectedExecutionException e )
        {
            // cool
        }
    }

    public void testDoubleSubmissionAllowed()
        throws Exception
    {
        DummyWaitingNexusTask rt1 = (DummyWaitingNexusTask) lookup( NexusTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt1.setAllowConcurrentExecution( true );

        nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 = (DummyWaitingNexusTask) lookup( NexusTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt2.setAllowConcurrentExecution( true );

        try
        {
            // the second submission should succeed, since it is allowed
            nexusScheduler.submit( "test2", rt2 );
        }
        catch ( RejectedExecutionException e )
        {
            fail( "Concurrent submission should succeed." );
        }
    }

}
