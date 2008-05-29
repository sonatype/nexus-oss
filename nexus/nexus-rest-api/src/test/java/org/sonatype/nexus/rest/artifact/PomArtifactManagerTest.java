/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.artifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.maven.GAVRequest;

public class PomArtifactManagerTest
    extends TestCase
{
    public void testGenerateGAV()
        throws IOException,
            XmlPullParserException
    {
        InputStream is = PomArtifactManagerTest.class.getResourceAsStream( "/test.pom" );

        File tmpStorage = new File( "target/pom-artifact-manager" );

        tmpStorage.mkdirs();

        PomArtifactManager manager = new PomArtifactManager( tmpStorage );

        manager.storeTempPomFile( is );

        GAVRequest request = manager.getGAVRequestFromTempPomFile();

        assertTrue( "groupId should be test-group", "test-group".equals( request.getGroupId() ) );
        assertTrue( "artifactId should be test-artifact", "test-artifact".equals( request.getArtifactId() ) );
        assertTrue( "version should be 1.0", "1.0".equals( request.getVersion() ) );
    }

    public void testGenerateGAVComplex()
        throws IOException,
            XmlPullParserException
    {
        InputStream is = PomArtifactManagerTest.class.getResourceAsStream( "/test1.pom" );

        File tmpStorage = new File( "target/pom-artifact-manager" );

        tmpStorage.mkdirs();

        PomArtifactManager manager = new PomArtifactManager( tmpStorage );

        manager.storeTempPomFile( is );

        GAVRequest request = manager.getGAVRequestFromTempPomFile();

        assertTrue( "groupId should be test-group", "test-group".equals( request.getGroupId() ) );
        assertTrue( "artifactId should be test-artifact", "test-artifact".equals( request.getArtifactId() ) );
        assertTrue( "version should be 1.0", "1.0".equals( request.getVersion() ) );
    }
}
