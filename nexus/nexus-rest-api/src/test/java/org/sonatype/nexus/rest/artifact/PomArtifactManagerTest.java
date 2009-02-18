/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.artifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;

public class PomArtifactManagerTest
    extends TestCase
{
    public void testDummy()
    {

    }

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
