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
package org.sonatype.nexus.integrationtests.nexus1197;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.FileNotFoundException;

import org.hamcrest.CoreMatchers;
import org.hamcrest.text.StringContains;
import org.mortbay.jetty.Server;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus1197CheckUserAgentIT
    extends AbstractNexusIntegrationTest
{

    private static RequestHandler handler;

    private static Server server;

    public Nexus1197CheckUserAgentIT()
    {
        super( "release-proxy-repo-1" );
    }

    @BeforeClass
    public static void setUp()
        throws Exception
    {
        handler = new RequestHandler();

        server = new Server( TestProperties.getInteger( "proxy.server.port" ) );
        server.setHandler( handler );
        server.start();
    }

    @AfterClass
    public static void tearDown()
        throws Exception
    {
        server.stop();
    }

    @Test
    public void downloadArtifactOverWebProxy()
        throws Exception
    {

        try
        {
            this.downloadArtifact( "nexus1197", "artifact", "1.0", "pom", null, "target/downloads" );
        }
        catch ( FileNotFoundException e )
        {
            // ok, just ignore
        }

        // Nexus/1.2.0-beta-2-SNAPSHOT (OSS; Windows XP; 5.1; x86; 1.6.0_07)
        // apacheHttpClient3x/1.2.0-beta-2-SNAPSHOT Nexus/1.0
        String userAgent = handler.getUserAgent();

        Assert.assertNotNull( userAgent );
        Assert.assertTrue( userAgent.startsWith( "Nexus/" ) );
        assertThat( userAgent, CoreMatchers.anyOf( StringContains.containsString( "(OSS" ),
                                                          StringContains.containsString( "(PRO" ) ) );

    }

}
