/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;

/**
 * UT just for reference what is happening in SOME cases of OOM with scheduled task (related to NXCM-4979). This test is
 * disabled as "testing OOM" is not quite reliable, but the code should explain itself.
 * 
 * @author cstamas
 */
@Ignore( "This test intentionally produces OOM and hence is unreliable, run it locally if needed" )
public class OOMTest
    extends TestSupport
{
    public static class OOMRunnable
        implements Runnable
    {

        public void run()
        {
            List<String> leaker = Lists.newArrayList( "This is list element" );
            while ( true )
            {
                final List<String> newLeaker = Lists.newArrayList( leaker );
                newLeaker.addAll( leaker );
                leaker = newLeaker;
            }
        }
    }

    // ==

    protected final DefaultScheduler defaultScheduler;

    public OOMTest()
    {
        defaultScheduler = new DefaultScheduler( new SimpleTaskConfigManager() );
    }

    /**
     * The purpose of this test is to represent why OOMed tasks are not "visible" nor detectable in Nexus. While
     * querying {@link ScheduledTask} works, it is usable only if you hold the "handle" (that instance) you got back
     * from {@link Scheduler} once you submitted it. That's not the case in Nexus. If you try to ask for the
     * {@link ScheduledTask} from {@link Scheduler} by ID, you got an exception, as the
     * "task is not here anymore, baby".
     * 
     * @throws Exception
     */
    @Test
    public void taskWithOOMIsDetectableOnlyIfYouHaveAHandleForIt()
        throws Exception
    {
        final ScheduledTask<Object> scheduledTask = defaultScheduler.submit( "OOM", new OOMRunnable() );
        TaskExecutionException brokenCause = null;
        try
        {
            // block until we are "done" (whether job done or died)
            scheduledTask.get();
        }
        catch ( ExecutionException e )
        {
            // this is Future API, hence our "wrapped" exception will be wrapped into concurrent ExecutionException
            brokenCause = (TaskExecutionException) e.getCause();
        }
        assertThat( brokenCause, notNullValue() );
        assertThat( brokenCause.getCause(), instanceOf( OutOfMemoryError.class ) );

        // scheduled task tells the "naked truth": the actual cause
        assertThat( scheduledTask.getBrokenCause(), instanceOf( OutOfMemoryError.class ) );
        // both of exceptions actually points to the same instance of OOMError
        assertThat( scheduledTask.getBrokenCause(), equalTo( brokenCause.getCause() ) );

        // finally, after OOM happened, and you don't have scheduledTask instance,
        // you'd might want to ask Scheduler for it, but nada
        try
        {
            defaultScheduler.getTaskById( scheduledTask.getId() );
            assertThat( "We should not get here!", false );
        }
        catch ( NoSuchTaskException e )
        {
            // as the scheduler does not have it anymore
        }
    }
}
