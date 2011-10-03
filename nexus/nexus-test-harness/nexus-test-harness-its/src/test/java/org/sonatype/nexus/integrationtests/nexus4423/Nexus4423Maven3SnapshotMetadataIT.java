/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4423;

import static org.apache.commons.io.FileUtils.copyDirectoryToDirectory;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.testng.annotations.Test;

/**
 * See NEXUS-4423: in short, Nexus suffers from same problem as Maven2 did: snapshots with classifiers not deployed with
 * latest deploying build (like per-OS artifacts) are not found.
 * 
 * @author cstamas
 */
public class Nexus4423Maven3SnapshotMetadataIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testMaven3SnapshotMetadata()
        throws Exception
    {
        // copy the "repo" to it's place, we need no index etc so this is fine
        File repo = new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_SNAPSHOT_REPO );
        copyDirectoryToDirectory( getTestFile( "org" ), repo );

        // TODO: resolve? See nexus-proxy:org.sonatype.nexus.proxy.maven.Nexus4423Maven3MetadataTest UT
        // just repeat the same but with /artifact/maven/resolve REST resource and check same asserts (version, buildTs, buildNo)
        // for both queries
    }
}
