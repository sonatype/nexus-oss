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
package org.sonatype.nexus.tools.repository;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Juven Xu
 */
public class RepositoryConvertorTest
    extends PlexusTestCase
{
    private RepositoryConvertor convertor;

    private File repository = new File( "src/test/resources/local-test-repo" );

    private File moveTargetPath = new File( "target/convert-output-move" );

    private File copyTargetPath = new File( "target/convert-output-copy" );

    public void setUp() throws Exception
    {
        convertor = (RepositoryConvertor) this.lookup( RepositoryConvertor.class );

        if ( copyTargetPath.exists() )
        {
            deleteFile( copyTargetPath );
        }

        copyTargetPath.mkdir();

        if ( moveTargetPath.exists() )
        {
            deleteFile( moveTargetPath );
        }

        moveTargetPath.mkdir();
    }

    public void testConvert()
        throws Exception
    {

        convertor.convertRepositoryWithCopy( repository, copyTargetPath );

        assertTargetRepositoryContent( copyTargetPath );

    }

    private void deleteFile( File file )
    {
        if ( file.isDirectory() )
        {
            for ( File subFile : file.listFiles() )
            {
                deleteFile( subFile );
            }
        }
        file.delete();
    }

    private void assertTargetRepositoryContent( File targetPath )
    {
        assertTrue( new File( targetPath, "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/" )
            .isDirectory() );
        assertTrue( new File( targetPath, "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/" )
            .exists() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.jar" )
            .isFile() );
        assertTrue( new File(
            targetPath,
            "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.jar" )
            .exists() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.pom" )
            .isFile() );
        assertTrue( new File(
            targetPath,
            "local-test-repo-releases/org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.pom" )
            .exists() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/" ).isDirectory() );
        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/" ).exists() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.jar" )
            .isFile() );
        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.jar" )
            .exists() );

        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.pom" )
            .isFile() );
        assertTrue( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.pom" )
            .exists() );
        
        // maven-metadata files should be filtered
        assertFalse( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-metadata-central.xml" )
            .exists() );
        assertFalse( new File(
            targetPath,
            "local-test-repo-snapshots/org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-metadata-central.xml.sha1" )
            .exists() );
    }
}
