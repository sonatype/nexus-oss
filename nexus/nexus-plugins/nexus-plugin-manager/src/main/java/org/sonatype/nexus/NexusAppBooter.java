package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.sonatype.appbooter.jsw.PlexusAppBooterJSWListener;
import org.sonatype.nexus.plugins.NexusPluginsComponentRepository;
import org.tanukisoftware.wrapper.WrapperManager;

public class NexusAppBooter
    extends PlexusAppBooterJSWListener
{
    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        super.customizeContainerConfiguration( containerConfiguration );

        containerConfiguration.setComponentRepository( new NexusPluginsComponentRepository() );
    }

    // ====
    // UGLY HACK FOLLOWS (copy+paste from PlexusAppBooterJswListener, fix this, move out from static method!)

    /**
     * This is the main entry point. It creates an instance of this class and registers it as a listener with the
     * WrapperManager. It then blocks on waitObj indefinitely. The system will be shutdown when the stop() method is
     * called.
     * 
     * @param args
     */
    public static void main( String[] args, ClassWorld classWorld )
    {
        // Start everything up and register as a listener.
        NexusAppBooter jswListener = new NexusAppBooter();

        jswListener.setWorld( classWorld );

        WrapperManager.start( jswListener, args );

        // once the wrapper is booted, the start() method will be called to actually get everything running.

        // now wait forever.
        try
        {
            synchronized ( waitObj )
            {
                waitObj.wait();
            }
        }
        catch ( InterruptedException e )
        {
        }
    }

    /**
     * This is the main entry point. It creates an instance of this class and registers it as a listener with the
     * WrapperManager. It then blocks on waitObj indefinitely. The system will be shutdown when the stop() method is
     * called.
     * 
     * @param args
     */
    public static void main( String[] args )
    {
        main( args, null );
    }

}
