package org.sonatype.nexus.test.launcher;

import java.io.File;
import java.io.FileInputStream;

import org.codehaus.plexus.classworlds.launcher.Launcher;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.appbooter.ctl.Service;

public class ThreadedPlexusAppBooterService
    implements Service
{
    private LauncherThread launcherThread;

    private Launcher launcher = new Launcher();

    private int controlPort;

    private static int THREAD_COUNT = 1;

    public ThreadedPlexusAppBooterService( File classworldsConf, int controlPort )
        throws Exception
    {
        // System.setProperty( "plexus"+ PlexusAppBooterService.ENABLE_CONTROL_SOCKET, "true" );
        this.controlPort = controlPort;

        this.launcher.configure( new FileInputStream( classworldsConf ) );
        this.launcher.setAppMain( "org.sonatype.appbooter.PlexusAppBooter", "plexus.core" );
    }

    public boolean isShutdown()
    {
        return this.launcherThread == null || !this.launcherThread.isAlive();
    }

    public boolean isStopped()
    {
        return this.isShutdown();
    }

    public void shutdown()
        throws AppBooterServiceException
    {

        // ControllerClient client;
        // try
        // {
        // client = new ControllerClient( controlPort );
        // client.shutdown();
        // }
        // catch ( Exception e )
        // {
        // throw new AppBooterServiceException( "Failed to connect to client", e );
        // }
        // this.launcherThread = null;
        try
        {
            if ( this.launcherThread != null && this.launcherThread.isAlive() )
            {
                synchronized ( launcherThread )
                {
                    this.launcherThread.interrupt();

                    try
                    {
                        this.launcherThread.join( 2000 );
                    }
                    catch ( InterruptedException e )
                    {
                        System.err.println( "Error waiting for launcher Thread to finish: " + e.getMessage() );
                        // pass it on.
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        finally
        {
            this.launcherThread = null;
        }
    }

    public void start()
        throws AppBooterServiceException
    {
        if ( this.launcherThread == null )
        {
            this.launcherThread = new LauncherThread( launcher, controlPort );
        }

        if ( !this.launcherThread.isAlive() )
        {
            this.launcherThread.start();
        }
    }

    public void stop()
        throws AppBooterServiceException
    {
        this.shutdown();
    }

    @SuppressWarnings( "deprecation" )
    public void forceStop()
    {
        if ( this.launcherThread != null )
        {
            this.launcherThread.stop();
            this.launcherThread = null;
        }
    }

    class LauncherThread
        extends Thread
    {
        private Launcher launcher;

        private int controlPort;

        public LauncherThread( Launcher launcher, int controlPort )
        {
            this.launcher = launcher;
            this.controlPort = controlPort;
            this.setName( "LauncherThread-" + THREAD_COUNT++ );
        }

        @Override
        public void run()
        {
            try
            {
                this.launcher.launch( new String[] { Integer.toString( controlPort ) } );
            }
            catch ( Exception e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
