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
package org.sonatype.nexus.bootstrap.monitor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread.LOCALHOST;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.sonatype.nexus.bootstrap.monitor.commands.PingCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.StopMonitorCommand;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * {@link ShutdownIfNotAliveThread} unit tests.
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

        new CommandMonitorTalker( LOCALHOST, keepAliveThread.getPort() ).send(StopMonitorCommand.NAME);
        keepAliveThread.join();

        aliveThread.interrupt();
        aliveThread.stopRunning();
        aliveThread.join();

        assertThat( shutDown.get(), is( true ) );
    }

}
