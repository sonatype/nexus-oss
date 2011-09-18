/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Base class for Nexus Integration Tests that starts Nexus before each test and stops it afterwards.
 *
 * @since 1.9.3
 */
public abstract class NexusRunningITSupport
        extends NexusITSupport {

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
     * @since 1.9.3
     */
    @Override
    public void setUp() {

        super.setUp();

        try {
            nexus().start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops Nexus (if running) after each test.
     * <p/>
     * {@inheritDoc}
     *
     * @since 1.9.3
     */
    @Override
    public void tearDown() {

        if (nexus() != null && nexus().getState().isRunning()) {
            try {
                nexus().stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        super.tearDown();
    }

    /**
     * Returns current Nexus. If Nexus was not yet instantiated, Nexus is created and configured.
     *
     * @return current Nexus
     * @since 1.9.3
     */
    protected final NexusBundle nexus() {
        if (nexus == null) {
            nexus = nexusProvider.get();
            NexusBundleConfiguration config = configureNexus(nexus().getConfiguration());
            if (config != null) {
                nexus().setConfiguration(config);
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
     * @since 1.9.3
     */
    protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
        // template method
        return configuration;
    }

}
