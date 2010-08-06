package org.sonatype.nexus.test.launcher;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.codehaus.plexus.classworlds.launcher.Launcher;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.appbooter.ctl.Service;
import org.sonatype.nexus.integrationtests.rt.boot.ITAppBooterCustomizer;

public class ThreadedPlexusAppBooterService
    implements Service
{
    private static Logger LOG = Logger.getLogger( ThreadedPlexusAppBooterService.class );

    private LauncherThread launcherThread;

    private Launcher launcher = new Launcher();

    private int controlPort;

    private String testId;

    private static int THREAD_COUNT = 1;

    public ThreadedPlexusAppBooterService( File classworldsConf, int controlPort, String testId )
        throws Exception
    {
        // System.setProperty( "plexus"+ PlexusAppBooterService.ENABLE_CONTROL_SOCKET, "true" );
        this.controlPort = controlPort;
        this.testId = testId;

        // we are "tricking" this line:
        // set plexus.appbooter.customizers default org.sonatype.nexus.NexusBooterCustomizer
        System.setProperty( "plexus.appbooter.customizers",
                            "org.sonatype.nexus.integrationtests.rt.boot.ITAppBooterCustomizer,org.sonatype.nexus.NexusBooterCustomizer" );

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
        if ( this.launcherThread != null && this.launcherThread.isAlive() )
        {
            synchronized ( launcherThread )
            {
                this.launcherThread.interrupt();

                try
                {
                    this.launcherThread.join( 20000 );
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

    public void start()
        throws AppBooterServiceException
    {
        if ( this.launcherThread == null )
        {
            LOG.info( "Creating LauncherThread (" + launcher + ", " + controlPort + ")" );
            this.launcherThread = new LauncherThread( launcher, controlPort, testId );
            this.launcherThread.start();
        }
        else
        {
            LOG.info( "Existing LauncherThread (" + launcher + ", " + controlPort + ")" );
        }

        if ( !this.launcherThread.isAlive() )
        {
            LOG.info( "Starting LauncherThread (" + launcher + ", " + controlPort + ")" );
            LOG.info( "LauncherThread state: " + launcherThread.getState() );
            Exception launcherThreadException = launcherThread.getException();
            if ( launcherThreadException != null )
            {
                throw new AppBooterServiceException( "LauncherThread cannot be started: "
                    + launcherThreadException.getMessage(), launcherThreadException );
            }
            else
            {
                // it died off, without any exception
                this.launcherThread = new LauncherThread( launcher, controlPort, testId );
                this.launcherThread.start();
            }
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

        private int controlPortArg;
        
        private String testIdArg;

        private Exception exception;

        public LauncherThread( Launcher launcher, int controlPort, String testId )
        {
            this.launcher = launcher;
            this.controlPortArg = controlPort;
            this.testIdArg = ITAppBooterCustomizer.TEST_ID_PREFIX + testId;
            this.setName( "LauncherThread-" + THREAD_COUNT++ );
        }

        @Override
        public void run()
        {
            try
            {
                this.launcher.launch( new String[] { Integer.toString( controlPortArg ), testIdArg } );
            }
            catch ( Exception e )
            {
                exception = e;
                e.printStackTrace();
            }
        }

        public Exception getException()
        {
            return exception;
        }
    }

    public void clean()
    {
        this.launcherThread = null;

        // TODO: this causes severe problems
        // Maybe since not all the _isolated_ classloader threads are done yet?
        // System.gc();
    }

}
