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

package org.sonatype.nexus.bootstrap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * {@link KeepAliveThread} and {@link ShutdownIfNotAliveThread} unit tests.
 *
 * @since 2.2
 */
public class KeepAliveTest
    extends TestSupport
{

    @Mock
    private Launcher launcher;

    @Test
    public void keepAlive()
        throws Exception
    {
        KeepAliveThread keepAliveThread = new KeepAliveThread( 0 );
        keepAliveThread.start();

        final AtomicBoolean shutDown = new AtomicBoolean( false );

        doAnswer( new Answer()
        {
            @Override
            public Object answer( final InvocationOnMock invocation )
                throws Throwable
            {
                shutDown.set( true );
                return null;
            }
        } ).when( launcher ).commandStop();

        ShutdownIfNotAliveThread aliveThread =
            new ShutdownIfNotAliveThread( launcher, keepAliveThread.getPort(), 100, 1000 );
        aliveThread.start();

        Thread.sleep( 2000 );

        keepAliveThread.stopRunning();
        keepAliveThread.join();

        aliveThread.interrupt();
        aliveThread.stopRunning();
        aliveThread.join();

        assertThat( shutDown.get(), is( true ) );
    }

}
