/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1329;

import java.io.File;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus1329ChecksumIT
    extends AbstractMirrorIT
{

    @Test
    public void downloadChecksumTest()
        throws Exception
    {
        File content = getTestFile( "basic" );
        File metadata = getTestFile( "metadata" );

        List<String> repoUrls = server.addServer( "repository", metadata );
        List<String> mirror1Urls = server.addServer( "mirror1", content );
        List<String> mirror2Urls = server.addServer( "mirror2", 500 );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, null, false, null );

        File artifactFile = this.downloadArtifactFromRepository( REPO, gav, "./target/downloads/nexus1329" );

        File originalFile = this.getTestFile( "basic/nexus1329/sample/1.0.0/sample-1.0.0.xml" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifactFile ) );

        Assert.assertTrue( "Nexus should not access second mirror " + mirror2Urls, mirror2Urls.isEmpty() );
        Assert.assertFalse( "Nexus did not download checksum " + repoUrls, repoUrls.isEmpty() );
        Assert.assertFalse( "Nexus should access first mirror " + mirror1Urls, mirror1Urls.isEmpty() );
    }

}
