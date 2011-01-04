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
package org.sonatype.nexus.integrationtests.nexus3638;

import java.net.URL;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class Downloader
    extends Thread
{

    private Throwable[] errors;

    private int i;

    private Gav gav;

    private Nexus3638IndexProxiedMavenPluginIT it;

    public Downloader( Nexus3638IndexProxiedMavenPluginIT it, Gav gav, int i, Throwable[] errors )
    {
        this.gav = gav;
        this.i = i;
        this.errors = errors;
        this.it = it;
    }

    @Override
    public void run()
    {
        try
        {
            // it.downloadSnapshotArtifact( "nexus3638", gav, new File( "target/downloads/nexus3638/" + i ) );
            it.downloadFile(
                new URL( AbstractNexusIntegrationTest.nexusBaseUrl
                    + AbstractNexusIntegrationTest.REPOSITORY_RELATIVE_URL + "nexus3638"
                        + "/org/apache/maven/plugins/maven-invoker-plugin/1.6-SNAPSHOT/maven-invoker-plugin-1.6-20100922.124315-3.jar" ),
                "target/downloads/nexus3638" );

        }
        catch ( Throwable t )
        {
            errors[i] = t;
        }
    }

}
