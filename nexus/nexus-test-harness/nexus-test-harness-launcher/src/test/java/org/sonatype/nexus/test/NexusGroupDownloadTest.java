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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Test;

public class NexusGroupDownloadTest
    extends AbstractNexusIntegrationTest
{
    public NexusGroupDownloadTest()
    {
        super( GROUP_REPOSITORY_RELATIVE_URL + "nexus-test/" );
    }

    @Test
    public void downloadArtifact() throws IOException
    {
        File artifact = downloadArtifact( "org.sonatype.nexus", "release-jar", "1", "jar", "./target/downloaded-jars" );

        assertTrue( artifact.exists() );

        File outputDirectory = unpackArtifact( artifact, "./target/extracted-jars" );

        try
        {
            assertTrue( outputDirectory.exists() );

            File[] files = outputDirectory.listFiles( new FilenameFilter()
            {
                public boolean accept(File dir, String name) {
                if ("nexus-test-harness-1.txt".equals( name ))
                {
                    return true;
                }

                return false;
            };} );

            assertTrue( files.length == 1 );

            complete();
        }
        finally
        {
            File[] files = outputDirectory.listFiles();
            for ( int i = 0; i < files.length; i++ )
            {
                files[i].delete();
            }
            outputDirectory.delete();
        }
    }
}
