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
package org.sonatype.nexus.bundle.launcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy.Strategy.EACH_METHOD;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;

/**
 * Base class for Nexus Integration Tests that starts Nexus before each test and stops it afterwards.
 *
 * @since 2.0
 */
public abstract class NexusRunningITSupport
    extends NexusITSupport
{

    private static final Logger LOGGER = LoggerFactory.getLogger( NexusRunningITSupport.class );

    /**
     * Provider used to create Nexus bundles on demand.
     */
    @Inject
    private Provider<NexusBundle> nexusProvider;

    /**
     * Current running Nexus. Lazy created by {@link #nexus()}.
     */
    private NexusBundle nexus;

    private static NexusBundle staticNexus;

    private static NexusStartAndStopStrategy.Strategy startAndStopStrategy = EACH_METHOD;

    private static String runningNexusBundleCoordinates;

    public NexusRunningITSupport()
    {
        super();
    }

    public NexusRunningITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Before
    public void beforeTestIsRunning()
    {
        final NexusStartAndStopStrategy strategy = getStartAndStopStrategy();
        if ( strategy != null )
        {
            startAndStopStrategy = strategy.value();
        }
        if ( nexusBundleCoordinates!= null && !nexusBundleCoordinates.equals( runningNexusBundleCoordinates ) )
        {
            stopNexus( staticNexus );
            staticNexus = null;
            runningNexusBundleCoordinates = null;
        }
        startNexus( nexus() );
        assertThat( "Nexus is running before test starts", nexus().isRunning(), is( true ) );
    }

    @After
    public void afterTestWasRunning()
    {
        if ( EACH_METHOD.equals( startAndStopStrategy ) )
        {
            stopNexus( nexus );
            staticNexus = null;
            runningNexusBundleCoordinates = null;
        }
        else
        {
            staticNexus = nexus;
            runningNexusBundleCoordinates = nexusBundleCoordinates;
        }
    }

    @AfterClass
    public static void afterAllTestsWereRun()
    {
        stopNexus( staticNexus );
        staticNexus = null;
        runningNexusBundleCoordinates = null;
    }

    /**
     * Returns current Nexus. If Nexus was not yet instantiated, Nexus is created and configured.
     *
     * @return current Nexus
     * @since 2.0
     */
    protected final NexusBundle nexus()
    {
        if ( nexus == null )
        {
            if ( staticNexus == null )
            {
                nexus = nexusProvider.get();
                NexusBundleConfiguration config = configureNexus( nexus.getConfiguration() );
                if ( config != null )
                {
                    nexus.setConfiguration( config );
                }
            }
            else
            {
                nexus = staticNexus;
            }
        }
        return nexus;
    }

    /**
     * Template method to be overridden by subclasses that wish to additionally configure Nexus before starting,
     * eventually replacing it.
     *
     * @param configuration Nexus configuration
     * @return configuration that will replace current configuration. If null is returned passed in configuration will
     *         be used
     * @since 2.0
     */
    protected NexusBundleConfiguration configureNexus( NexusBundleConfiguration configuration )
    {
        // template method
        return configuration;
    }

    /**
     * Determines the start and stop strategy by looking up {@link NexusStartAndStopStrategy} annotation.
     *
     * @return start and stop strategy to pe used. If null, nexus will be started and stopped for each test method.
     * @since 2.1
     */
    protected NexusStartAndStopStrategy getStartAndStopStrategy()
    {
        return getClass().getAnnotation( NexusStartAndStopStrategy.class );
    }

    private static void startNexus( final NexusBundle nexusBundle )
    {
        if ( nexusBundle != null && !nexusBundle.isRunning() )
        {
            try
            {
                LOGGER.info( "Starting Nexus ({})", nexusBundle );
                nexusBundle.start();
            }
            catch ( Exception e )
            {
                throw Throwables.propagate( e );
            }
        }
    }

    private static void stopNexus( final NexusBundle nexusBundle )
    {
        if ( nexusBundle != null && nexusBundle.isRunning() )
        {
            try
            {
                LOGGER.info( "Stopping Nexus ({})", nexusBundle );
                nexusBundle.stop();
            }
            catch ( Exception e )
            {
                throw Throwables.propagate( e );
            }
        }
    }

}
