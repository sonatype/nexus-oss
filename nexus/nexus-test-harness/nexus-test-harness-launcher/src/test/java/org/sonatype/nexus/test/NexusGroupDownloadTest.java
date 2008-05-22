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
package org.sonatype.nexus.test;

import java.io.File;
import java.io.FilenameFilter;

public class NexusGroupDownloadTest
    extends AbstractNexusTest
{
    public NexusGroupDownloadTest()
    {
        super( "http://localhost:8081/nexus/content/groups/nexus-test/" );
    }
    public void testDownloadArtifact()
    {
        File artifact = downloadArtifact( "org.sonatype.nexus", "release-jar", "1", "jar", "./target/downloaded-jars" );
        
        assert( artifact.exists() );
        
        File outputDirectory = unpackArtifact( artifact, "./target/extracted-jars" );
        
        assert( outputDirectory.exists() );
        
        File[] files = outputDirectory.listFiles( new FilenameFilter()
        {
            public boolean accept(File dir, String name) {
            if ("nexus-test-harness-1.txt".equals( name ))
            {
                return true;
            }
            
            return false;
        };} );
        
        assert ( files.length == 1 );
    }
}
