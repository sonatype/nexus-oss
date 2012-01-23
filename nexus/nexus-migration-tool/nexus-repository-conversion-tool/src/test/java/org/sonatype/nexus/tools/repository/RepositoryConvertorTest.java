/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

/**
 * @author Juven Xu
 */
public class RepositoryConvertorTest
    extends PlexusTestCaseSupport
{
    private RepositoryConvertor convertor;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        convertor = (RepositoryConvertor) this.lookup( RepositoryConvertor.class );
    }

    @Test
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

    @Test
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

    @Test
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
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/maven-apt-plugin/1.0.13/maven-apt-plugin-1.0.13.jar" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/maven-apt-plugin/1.0.13/maven-apt-plugin-1.0.13.jar.md5" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/maven-apt-plugin/1.0.13/maven-apt-plugin-1.0.13.jar.sha1" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/tobago/1.0.13/tobago-1.0.13.pom" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/tobago/1.0.13/tobago-1.0.13.pom.md5" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/tobago/1.0.13/tobago-1.0.13.pom.sha1" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/tobago-tool/1.0.13/tobago-tool-1.0.13.pom" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/tobago-tool/1.0.13/tobago-tool-1.0.13.pom.sha1" ), Boolean.TRUE );
        expected.put( new File(
            targetReleasesRepo,
            "org/apache/myfaces/tobago/tobago-tool/1.0.13/tobago-tool-1.0.13.pom.md5" ), Boolean.TRUE );

        validateResult( expected );
    }

    @Test
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

            Assert.assertEquals( "For file: " + file.getAbsolutePath(), expected.get( file ), actual );
        }
    }
}
