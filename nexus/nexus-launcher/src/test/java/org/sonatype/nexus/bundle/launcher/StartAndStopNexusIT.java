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

import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.URLMatchers.respondsWithStatus;

/**
 * Test starting and launching of Nexus.
 *
 * @since 1.9.3
 */
public class StartAndStopNexusIT
    extends NexusITSupport
{

    /**
     * Nexus bundle to be used during tests.
     */
    @Inject
    private NexusBundle nexus;

    @Inject
    @Named( "${NexusITSupport.groovyPluginCoordinates}" )
    private String groovyPluginCoordinates;

    /**
     * Starts/Stops Nexus while it checks that:<br/>
     * - Nexus instance is set<br/>
     * - Nexus state is set<br/>
     * - Nexus state confirms that is running<br/>
     * - Nexus responds with 200 at provided URL
     *
     * @throws Exception re-thrown
     */
    @Test
    public void startAndStop()
        throws Exception
    {
        try
        {
            nexus().start();

            assertThat( nexus(), is( notNullValue() ) );
            assertThat( nexus().isRunning(), is( true ) );

            assertThat( nexus().getUrl(), respondsWithStatus( 200 ) );
        }
        finally
        {
            nexus.stop();
        }
    }

    /**
     * Start/Stop Nexus configured with an additional plugin.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void installPlugins()
        throws Exception
    {
        try
        {
            nexus.getConfiguration().addPlugins( resolveArtifact( groovyPluginCoordinates ) );
            nexus().start();
        }
        finally
        {
            nexus.stop();
        }
    }

    /**
     * Returns Nexus instance.
     *
     * @return nexus instance
     */

    private NexusBundle nexus()
    {
        return nexus;
    }

}
