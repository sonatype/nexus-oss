/*
 * Copyright (c) 2007-2011 Sonatype, Inc. All rights reserved.
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

package org.sonatype.nexus.bootstrap.monitor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread.LOCALHOST;
import static org.sonatype.nexus.bootstrap.monitor.commands.StopMonitorCommand.STOP_MONITOR_COMMAND;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.sonatype.nexus.bootstrap.monitor.commands.PingCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.StopMonitorCommand;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * {@link org.sonatype.nexus.bootstrap.monitor.ShutdownIfNotAliveThread} unit tests.
 *
 * @since 2.2
 */
public class ShutdownIfNotAliveThreadTest
    extends TestSupport
{

    @Test
    public void keepAlive()
        throws Exception
    {
        CommandMonitorThread keepAliveThread = new CommandMonitorThread(
            0,
            new PingCommand(),
            new StopMonitorCommand()
        );
        keepAliveThread.start();

        final AtomicBoolean shutDown = new AtomicBoolean( false );

        ShutdownIfNotAliveThread aliveThread = new ShutdownIfNotAliveThread(
            new Runnable()
            {
                @Override
                public void run()
                {
                    shutDown.set( true );
                }
            }, LOCALHOST, keepAliveThread.getPort(), 100, 1000
        );
        aliveThread.start();

        Thread.sleep( 2000 );

        new CommandMonitorTalker( LOCALHOST, keepAliveThread.getPort() ).send( STOP_MONITOR_COMMAND );
        keepAliveThread.join();

        aliveThread.interrupt();
        aliveThread.stopRunning();
        aliveThread.join();

        assertThat( shutDown.get(), is( true ) );
    }

}
