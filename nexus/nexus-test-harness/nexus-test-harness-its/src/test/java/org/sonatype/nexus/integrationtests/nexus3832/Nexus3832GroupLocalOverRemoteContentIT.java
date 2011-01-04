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
package org.sonatype.nexus.integrationtests.nexus3832;

import static org.testng.Assert.assertTrue;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.testng.annotations.Test;

public class Nexus3832GroupLocalOverRemoteContentIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test( enabled = false )
    public void onLocalCache()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus3832", "artifact", "1.0" );
        File downloaded = downloadArtifactFromGroup( "public", gav, "target/downloads/nexus3832" );

        assertTrue( FileTestingUtils.compareFileSHA1s( downloaded, getTestResourceAsFile( "projects/p1/artifact.jar" ) ) );
    }

    @Test( enabled = false )
    public void onlyRemote()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus3832", "artifact", "2.0" );
        File localFile = getLocalFile( "release-proxy-repo-1", gav );

        File downloaded = downloadArtifactFromGroup( "public", gav, "target/downloads/nexus3832" );

        assertTrue( FileTestingUtils.compareFileSHA1s( downloaded, localFile ) );
    }

    @Test( enabled = false )
    public void onlyLocal()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus3832", "artifact", "3.0" );
        File localFile = getTestResourceAsFile( "projects/p3/artifact.jar" );

        File downloaded = downloadArtifactFromGroup( "public", gav, "target/downloads/nexus3832" );

        assertTrue( FileTestingUtils.compareFileSHA1s( downloaded, localFile ) );
    }
}
