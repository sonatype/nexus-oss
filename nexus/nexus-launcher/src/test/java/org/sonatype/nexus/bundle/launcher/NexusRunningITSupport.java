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

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Base class for Nexus Integration Tests that starts Nexus before each test and stops it afterwards.
 *
 * @since 2.0
 */
public abstract class NexusRunningITSupport
    extends NexusITSupport
{

    /**
     * Provider used to create Nexus bundles on demand.
     */
    @Inject
    private Provider<NexusBundle> nexusProvider;

    /**
     * Current running Nexus. Lazy created by {@link #nexus()}.
     */
    private NexusBundle nexus;

    /**
     * Starts Nexus before each test.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public void setUp()
    {

        super.setUp();

        try
        {
            nexus().start();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Stops Nexus (if running) after each test.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public void tearDown()
    {

        if ( nexus() != null && nexus().isRunning() )
        {
            try
            {
                nexus().stop();
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }

        super.tearDown();
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
            nexus = nexusProvider.get();
            NexusBundleConfiguration config = configureNexus( nexus().getConfiguration() );
            if ( config != null )
            {
                nexus().setConfiguration( config );
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

}
