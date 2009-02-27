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
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Juven Xu
 */
public class RepositoryConvertorTest
    extends PlexusTestCase
{
    private RepositoryConvertor convertor;

    public void setUp()
        throws Exception
    {
        convertor = (RepositoryConvertor) this.lookup( RepositoryConvertor.class );
    }
    
    public void testConvertWithCopy()
        throws Exception
    {
        File srcRepo = new File( getBasedir(), "target/test-classes/local-test-repo" );
        File target = new File( getBasedir(), "target" );

        File targetReleasesRepo = new File( getBasedir(), "target/local-test-repo-releases" );
        File targetSnapshotsRepo = new File( getBasedir(), "target/local-test-repo-snapshots" );
        
        convertor.convertRepositoryWithCopy( srcRepo, target );
        
        Map<File, Boolean> expected = new LinkedHashMap<File, Boolean>();
        
        expected.put( new File( targetReleasesRepo, "org/apache/maven/plugins/maven-javadoc-plugin/2.5/"), Boolean.TRUE );
        expected.put( new File( targetReleasesRepo, "org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.jar"), Boolean.TRUE );
        expected.put( new File( targetReleasesRepo, "org/apache/maven/plugins/maven-javadoc-plugin/2.5/maven-javadoc-plugin-2.5.pom"), Boolean.TRUE );
        expected.put( new File( targetSnapshotsRepo, "org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/"), Boolean.TRUE );
        expected.put( new File( targetSnapshotsRepo, "org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.jar"), Boolean.TRUE );
        expected.put( new File( targetSnapshotsRepo, "org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-javadoc-plugin-2.5.1-20081013.050423-737.pom"), Boolean.TRUE );
        expected.put( new File( targetSnapshotsRepo,"org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-metadata-central.xml"), Boolean.FALSE);
        expected.put( new File( targetSnapshotsRepo,"org/apache/maven/plugins/maven-javadoc-plugin/2.5.1-SNAPSHOT/maven-metadata-central.xml.sha1"), Boolean.FALSE);
        
        validateResult( expected );
    }
    
    public void testConvertWithMove()
        throws Exception
    {
        File srcRepo = new File( getBasedir(), "target/test-classes/local-test-repo" );
        File target = new File( getBasedir(), "target" );

        convertor.convertRepositoryWithMove( srcRepo, target );

        Map<File, Boolean> expected = new LinkedHashMap<File, Boolean>();

        expected.put( srcRepo, Boolean.FALSE );

        validateResult( expected );
    }

    public void testNexus1667()
        throws Exception
    {
        File srcRepo = new File( getBasedir(), "target/test-classes/nexus-1667-repo" );
        File target = new File( getBasedir(), "target" );

        File targetReleasesRepo = new File( getBasedir(), "target/nexus-1667-repo-releases" );

        convertor.convertRepositoryWithCopy( srcRepo, target );

        Map<File, Boolean> expected = new LinkedHashMap<File, Boolean>();
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/2.3.0-v200706262000/ecore-2.3.0-v200706262000.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/2.3.0-v200706262000/ecore-2.3.0-v200706262000.pom" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/change/2.3.0-v200706262000/change-2.3.0-v200706262000.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/change/2.3.0-v200706262000/change-2.3.0-v200706262000.pom" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/edit/2.3.0-v200706262000/edit-2.3.0-v200706262000.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/edit/2.3.0-v200706262000/edit-2.3.0-v200706262000.pom" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/xml/2.3.0-v200706262000/xml-2.3.0-v200706262000.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/eclipse/emf/ecore/xml/2.3.0-v200706262000/xml-2.3.0-v200706262000.pom" ), Boolean.TRUE );

        validateResult( expected );
    }
    
    public void testNonstandardVersionDirectory()
        throws Exception
    {
        File srcRepo = new File( getBasedir(), "target/test-classes/non-standard-repo" );
        File target = new File( getBasedir(), "target" );
        
        File targetReleasesRepo = new File( getBasedir(), "target/non-standard-repo-releases" );
        
        convertor.convertRepositoryWithCopy( srcRepo, target );
        
        Map<File, Boolean> expected = new LinkedHashMap<File, Boolean>();
        
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1-sources.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1-sources.jar.md5" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1-sources.jar.sha1" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1.jar.md5" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1.jar.sha1" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1.pom" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1.pom.md5" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/build212-hudson-1-1/trilead-ssh2-build212-hudson-1-1.pom.sha1" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/maven-metadata.xml" ), Boolean.FALSE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/maven-metadata.xml.md5" ), Boolean.FALSE );
        expected.put( new File(
            targetReleasesRepo,
            "org/jvnet/hudson/trilead-ssh2/maven-metadata.xml.sha1" ), Boolean.FALSE );
   
        validateResult( expected );
    }

    private void validateResult( Map<File, Boolean> expected )
    {
        for ( File file : expected.keySet() )
        {
            Boolean actual = new Boolean( file.exists() );

            assertEquals( "For file: " + file.getAbsolutePath(), expected.get( file ), actual );
        }
    }
}
