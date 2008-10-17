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

package org.sonatype.nexus.tools.repository;

import java.io.File;

import junit.framework.TestCase;

/**
 * @author Juven Xu
 */
public class RepositoryConvertorTest
    extends TestCase
{
    private RepositoryConvertor convertor;

    public void setUp()
    {
        convertor = new DefaultRepositoryConvertor();
    }

    public void testConvert()
        throws Exception
    {
        File repository = new File( "src/test/resources/local-test-repo" );

        File targetPath = new File( "target/convert-output" );

        if (targetPath.exists())
        {
            targetPath.delete();
        }
        
        targetPath.mkdir();

        convertor.convertRepository( repository, targetPath );

        assertTrue( new File( targetPath, "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/" )
            .isDirectory() );
        
        assertTrue( new File(
            targetPath,
            "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.jar" )
            .isFile() );
        
        assertTrue( new File(
            targetPath,
            "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.pom" )
            .isFile() );
        
        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/" ).isDirectory() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.jar" )
            .isFile() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.pom" )
            .isFile() );
    }
}
