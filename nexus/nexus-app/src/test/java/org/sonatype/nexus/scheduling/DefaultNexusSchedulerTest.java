package org.sonatype.nexus.scheduling;

import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.AbstractNexusTestCase;
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
        DummyWaitingNexusTask rt1 = (DummyWaitingNexusTask) lookup( SchedulerTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt1.setAllowConcurrentSubmission( true );

        nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 = (DummyWaitingNexusTask) lookup( SchedulerTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt2.setAllowConcurrentSubmission( false );

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
        DummyWaitingNexusTask rt1 = (DummyWaitingNexusTask) lookup( SchedulerTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt1.setAllowConcurrentSubmission( true );

        nexusScheduler.submit( "test1", rt1 );

        DummyWaitingNexusTask rt2 = (DummyWaitingNexusTask) lookup( SchedulerTask.ROLE, DummyWaitingNexusTask.class
            .getName() );

        rt2.setAllowConcurrentSubmission( true );

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

    public void testConcurrentExecutionOfRepositoriesTask()
        throws Exception
    {
    }

}
