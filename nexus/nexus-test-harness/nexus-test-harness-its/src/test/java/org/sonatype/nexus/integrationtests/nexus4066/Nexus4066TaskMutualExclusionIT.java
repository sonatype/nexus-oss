package org.sonatype.nexus.integrationtests.nexus4066;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.scheduling.TaskState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Nexus4066TaskMutualExclusionIT
    extends AbstractNexusIntegrationTest
{

    @DataProvider( name = "data", parallel = false )
    public Object[][] createData()
    {
        // GofG == group of groups
        return new Object[][] {//
        { "repo", "group", true },//
            { "repo", "repo2", false },//
            { "repo", "group2", false },//
            { "group", "group2", false },//
            { "repo2", "group", false },//
            { "repo2", "group2", true },//
            { "repo", "GofG", true },//
            { "group", "GofG", true },//
            { "repo2", "GofG", false },//
            { "group2", "GofG", false },//
            { "GofG2", "GofG", false },//
            { "repo2", "GofG2", true },//
            { "group2", "GofG2", true },//
            { "repo", "GofG2", false },//
            { "group", "GofG2", false },//
        };
    }

    @BeforeMethod
    public void w8()
        throws Exception
    {
        TaskScheduleUtil.waitForAllTasksToStop();
    }

    @Test( dataProvider = "data" )
    public void run( String repo1, String repo2, boolean shouldWait )
        throws Exception
    {
        try
        {
            ScheduledServiceListResource task1 = createTask( repo1 );
            assertThat( task1.getStatus(), equalTo( TaskState.RUNNING.name() ) );

            ScheduledServiceListResource task2 = createTask( repo2 );
            if ( shouldWait )
            {
                assertThat( task2.getStatus(), equalTo( TaskState.SLEEPING.name() ) );
            }
            else
            {
                assertThat( task2.getStatus(), equalTo( TaskState.RUNNING.name() ) );
            }
        }
        catch ( java.lang.AssertionError e )
        {
            throw new RuntimeException( "Repo1: " + repo1 + " repo2: " + repo2 + " shouldWait: " + shouldWait, e );
        }
    }

    private ScheduledServiceListResource createTask( String repo )
        throws Exception
    {
        final String taskName = "SleepRepositoryTask_" + repo + "_" + System.nanoTime();
        TaskScheduleUtil.runTask( taskName, "SleepRepositoryTask", 0,
            TaskScheduleUtil.newProperty( "repositoryId", repo ),
            TaskScheduleUtil.newProperty( "time", String.valueOf( 5 ) ) );

        Thread.sleep( 2000 );

        return TaskScheduleUtil.getTask( taskName );
    }

}
