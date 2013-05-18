/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty;

import java.io.File;
import java.io.IOException;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * Class extending Jetty8 and adding Java Service Wrapper support on it, making Jetty "jsw-ized". This class is used as
 * "main" launched class in Nexus bundle for example. Requirement for App is to have path to jetty.xml passed as first
 * argument. Just make this one a "main class" in JSW config and pass in the path of jetty.xml as 1st parameter and you
 * are done.
 * 
 * @author cstamas
 */
public class Jetty8WrapperListener
    extends Jetty8
    implements WrapperListener
{
    protected static final Object waitObj = new Object();

    protected Jetty8WrapperListener( final File jettyXml )
        throws IOException
    {
        // nope, do not instantiate this directly, just from main()!
        super( jettyXml );
    }

    // WrapperListener
    public Integer start( final String[] args )
    {
        WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Starting Jetty..." );

        try
        {
            startJetty();

            return null;
        }
        catch ( Exception e )
        {
            WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_FATAL, "Unable to start Jetty: " + e.getMessage() );

            e.printStackTrace();

            return 1;
        }
    }

    // WrapperListener
    public int stop( final int exitCode )
    {
        WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Stopping Jetty..." );

        try
        {
            stopJetty();

            return exitCode;
        }
        catch ( Exception e )
        {
            WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_FATAL,
                "Unable to stop Jetty cleanly: " + e.getMessage() );

            e.printStackTrace();

            return 1;
        }
        finally
        {
            synchronized ( waitObj )
            {
                waitObj.notify();
            }
        }
    }

    // WrapperListener
    public void controlEvent( final int event )
    {
        if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT ) && WrapperManager.isLaunchedAsService() )
        {
            // Ignore this event, it's just user logged out and we are a service, so continue running
            if ( WrapperManager.isDebugEnabled() )
            {
                WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_DEBUG, "Jetty8WrapperListener: controlEvent("
                    + event + ") Ignored" );
            }
        }
        else
        {
            if ( WrapperManager.isDebugEnabled() )
            {
                WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_DEBUG, "Jetty8WrapperListener: controlEvent("
                    + event + ") Stopping" );
            }

            WrapperManager.stop( 0 );
            // Will not get here.
        }
    }

    // ==

    /**
     * "Standard" main method, starts embedded Jetty "by the book", and it returns.
     * 
     * @param args
     * @throws IOException
     */
    public static void main( final String[] args )
        throws IOException
    {
        if ( args != null && args.length > 0 )
        {
            final File jettyXml = new File( args[0] );

            WrapperManager.start( new Jetty8WrapperListener( jettyXml ), args );
        }
        else
        {
            WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_FATAL,
                "First supplied app parameter should be path to existing Jetty8 XML configuration file!" );

            WrapperManager.stop( 1 );
        }
    }
}
