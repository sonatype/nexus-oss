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

package org.sonatype.nexus.bundle.launcher.internal;

import java.io.IOException;
import java.lang.reflect.Method;

import org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread;
import org.sonatype.nexus.bootstrap.monitor.ShutdownIfNotAliveThread;
import org.sonatype.nexus.bootstrap.monitor.commands.PingCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.StopApplicationCommand;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * TODO
 *
 * @since 1.0
 */
public class NexusITLauncher
{

    public static final String LAUNCHER = NexusITLauncher.class.getName() + ".launcher";

    public static final String COMMAND_MONITOR_PORT = NexusITLauncher.class.getName() + ".monitor.port";

    public static final String KEEP_ALIVE_PORT = NexusITLauncher.class.getName() + ".keepAlive.port";

    public static final String KEEP_ALIVE_PING_INTERVAL = ShutdownIfNotAliveThread.class.getName() + ".pingInterval";

    public static final String KEEP_ALIVE_TIMEOUT = ShutdownIfNotAliveThread.class.getName() + ".timeout";

    public static final String FIVE_SECONDS = "5000";

    public static final String ONE_SECOND = "1000";

    public Integer start( final String[] args )
        throws Exception
    {
        // find wrapped launcher
        String launcher = System.getProperty( LAUNCHER );
        if ( launcher == null )
        {
            throw new IllegalStateException( "Launcher must be specified via system property: " + LAUNCHER );
        }

        Class<?> launcherClass = getClass().getClassLoader().loadClass( launcher );
        Method main = launcherClass.getMethod( "main", args.getClass() );

        maybeEnableCommandMonitor();
        maybeEnableShutdownIfNotAlive();

        main.invoke( null, new Object[]{ args } );

        return null; // continue running
    }

    protected void maybeEnableCommandMonitor()
        throws IOException
    {
        String commandMonitorPort = System.getProperty( COMMAND_MONITOR_PORT );
        if ( commandMonitorPort == null )
        {
            commandMonitorPort = System.getenv( COMMAND_MONITOR_PORT );
        }
        if ( commandMonitorPort != null )
        {
            new CommandMonitorThread(
                Integer.parseInt( commandMonitorPort ),
                new StopApplicationCommand( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        WrapperManager.stopAndReturn( 0 );
                    }
                } ),
                new PingCommand()
            ).start();
        }
    }

    protected void maybeEnableShutdownIfNotAlive()
        throws IOException
    {
        String port = System.getProperty( KEEP_ALIVE_PORT );
        if ( port == null )
        {
            port = System.getenv( KEEP_ALIVE_PORT );
        }
        if ( port != null )
        {
            String pingInterval = System.getProperty( KEEP_ALIVE_PING_INTERVAL );
            if ( pingInterval == null )
            {
                pingInterval = System.getenv( KEEP_ALIVE_PING_INTERVAL );
                if ( pingInterval == null )
                {
                    pingInterval = FIVE_SECONDS;
                }
            }
            String timeout = System.getProperty( KEEP_ALIVE_TIMEOUT );
            if ( timeout == null )
            {
                timeout = System.getenv( KEEP_ALIVE_TIMEOUT );
                if ( timeout == null )
                {
                    timeout = ONE_SECOND;
                }
            }
            new ShutdownIfNotAliveThread(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        WrapperManager.stopAndReturn( 0 );
                    }
                },
                Integer.parseInt( port ),
                Integer.parseInt( pingInterval ),
                Integer.parseInt( timeout )
            ).start();
        }
    }

    public static void main( final String[] args )
        throws Exception
    {
        new NexusITLauncher().start( args );
    }

}
