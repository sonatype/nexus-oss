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
package org.sonatype.nexus.rest.artifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.junit.Test;
import org.junit.Ignore;

public class PomArtifactManagerTest
{
    @Test
    public void testDummy()
    {

    }

    @Ignore
    @Test
    public void OFFtestGenerateGAV()
        throws IOException,
            XmlPullParserException
    {
        InputStream is = PomArtifactManagerTest.class.getResourceAsStream( "/test.pom" );

        File tmpStorage = new File( "target/pom-artifact-manager" );

        tmpStorage.mkdirs();

        PomArtifactManager manager = new PomArtifactManager( tmpStorage );

        manager.storeTempPomFile( is );

        // ArtifactStoreRequest request = manager.getGAVRequestFromTempPomFile( new ArtifactStoreRequest( "G", "A", "V"
        // ) );

        // assertTrue( "groupId should be test-group", "test-group".equals( request.getGroupId() ) );
        // assertTrue( "artifactId should be test-artifact", "test-artifact".equals( request.getArtifactId() ) );
        // assertTrue( "version should be 1.0", "1.0".equals( request.getVersion() ) );
    }

    @Ignore
    @Test
    public void OFFtestGenerateGAVComplex()
        throws IOException,
            XmlPullParserException
    {
        InputStream is = PomArtifactManagerTest.class.getResourceAsStream( "/test1.pom" );

        File tmpStorage = new File( "target/pom-artifact-manager" );

        tmpStorage.mkdirs();

        PomArtifactManager manager = new PomArtifactManager( tmpStorage );

        manager.storeTempPomFile( is );

        // ArtifactStoreRequest request = manager.getGAVRequestFromTempPomFile( new ArtifactStoreRequest( "G", "A", "V"
        // ) );

        // assertTrue( "groupId should be test-group", "test-group".equals( request.getGroupId() ) );
        // assertTrue( "artifactId should be test-artifact", "test-artifact".equals( request.getArtifactId() ) );
        // assertTrue( "version should be 1.0", "1.0".equals( request.getVersion() ) );
    }
}
