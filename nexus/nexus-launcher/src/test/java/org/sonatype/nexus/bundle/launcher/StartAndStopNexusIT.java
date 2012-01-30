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
 * @since 2.0
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
            nexus().stop();
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
            nexus().getConfiguration().addPlugins( resolveArtifact( groovyPluginCoordinates ) );
            nexus().start();
        }
        finally
        {
            nexus().stop();
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
