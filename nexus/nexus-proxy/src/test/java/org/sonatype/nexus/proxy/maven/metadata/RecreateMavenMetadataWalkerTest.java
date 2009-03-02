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
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.RecreateMavenMetadataWalkerProcessor;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalkerTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private Repository inhouseRelease;

    private Repository inhouseSnapshot;

    private File repoBase = new File( "./target/test-classes/mavenMetadataTestRepo" );

    private Walker walker;

    private String[] releaseArtifactFiles = {
        "/junit/junit/3.8.1/junit-3.8.1.jar",
        "/junit/junit/3.8.1/junit-3.8.1.pom",
        "/junit/junit/3.8.2/junit-3.8.2.jar",
        "/junit/junit/3.8.2/junit-3.8.2.pom",
        "/junit/junit/4.0/junit-4.0.jar",
        "/junit/junit/4.0/junit-4.0.pom",
        "/junit/junit/4.4/junit-4.4.jar",
        "/junit/junit/4.4/junit-4.4.pom",
        "/junit/junit/4.4/junit-4.4.sources.jar.md5",
        "/junit/junit-mock/maven-metadata.xml",
        "/junit/junit-mock/1.1/readme.txt",
        "/com/mycom/proj1/1.0/proj1-1.0.jar",
        "/com/mycom/proj1/1.0/proj1-1.0.pom",
        "/com/mycom/proj1/2.0/proj1-2.0.jar",
        "/com/mycom/proj1/2.0/proj1-2.0.pom",
        "/com/mycom/proj1/maven-metadata.xml" };

    private String[] snapshotArtifactFiles = {
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.180215-1.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.180215-1.pom",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.182430-2.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.182430-2.pom",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.184527-3.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.184527-3.pom",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081025.143218-32.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081025.143218-32.pom",
        "/nexus1332/artifact-interp-main/14.0.0-SNAPSHOT/artifact-interp-main-14.0.0-20090108.150441-1.jar",
        "/nexus1332/artifact-interp-main/14.0.0-SNAPSHOT/artifact-interp-main-14.0.0-20090108.150441-1.pom",
        "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.jar",
        "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.pom",
        "/com/mycom/proj2/maven-metadata.xml",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.jar",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.pom",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.jar",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.pom",
        "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.jar",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.pom",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.jar",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.pom",
        "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml" };

    private String[] pluginArtifactFiles = {
        "/org/apache/maven/plugins/maven-antrun-plugin/1.1/maven-antrun-plugin-1.1.jar",
        "/org/apache/maven/plugins/maven-antrun-plugin/1.1/maven-antrun-plugin-1.1.pom",
        "/org/apache/maven/plugins/maven-clean-plugin/2.2/maven-clean-plugin-2.2.jar",
        "/org/apache/maven/plugins/maven-clean-plugin/2.2/maven-clean-plugin-2.2.pom",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.1/maven-plugin-plugin-2.4.1.jar",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.1/maven-plugin-plugin-2.4.1.pom",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.3/maven-plugin-plugin-2.4.3.jar",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.3/maven-plugin-plugin-2.4.3.pom",
        "/org/apache/maven/plugins/maven-source-plugin/2.0.4/maven-source-plugin-2.0.4.jar",
        "/org/apache/maven/plugins/maven-source-plugin/2.0.4/maven-source-plugin-2.0.4.pom",
        "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar",
        "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom",
        "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar",
        "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom",
        "/com/mycom/group1/maven-metadata.xml",
        "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar",
        "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom",
        "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar",
        "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom",
        "/com/mycom/group2/maven-metadata.xml" };

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );

        return jettyTestsuiteEnvironmentBuilder;
    }

    @Override
    public void setUp()
        throws Exception
    {

        super.setUp();

        inhouseRelease = getRepositoryRegistry().getRepository( "inhouse" );

        // copy all release artifact fils hosted inhouse repo
        for ( String releaseArtifactFile : releaseArtifactFiles )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( releaseArtifactFile, true );

            FileInputStream fis = new FileInputStream( new File( repoBase, releaseArtifactFile ) );

            inhouseRelease.storeItem( request, fis, null );

            fis.close();
        }

        for ( String pluginArtifactFile : pluginArtifactFiles )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( pluginArtifactFile, true );

            FileInputStream fis = new FileInputStream( new File( repoBase, pluginArtifactFile ) );

            inhouseRelease.storeItem( request, fis, null );

            fis.close();
        }

        inhouseSnapshot = getRepositoryRegistry().getRepository( "inhouse-snapshot" );

        // copy all snapshot artifact fils hosted snapshot inhouse repo
        for ( String snapshotArtifactFile : snapshotArtifactFiles )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( snapshotArtifactFile, true );

            FileInputStream fis = new FileInputStream( new File( repoBase, snapshotArtifactFile ) );

            inhouseSnapshot.storeItem( request, fis, null );

            fis.close();
        }

        walker = lookup( Walker.class );
    }

    private void rebuildMavenMetadata( Repository repo )
    {
        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor();

        DefaultWalkerContext ctx = new DefaultWalkerContext( repo );

        ctx.getProcessors().add( wp );

        walker.walk( ctx );
    }

    private void validateResults( Repository repository, Map<String, Boolean> results )
        throws Exception
    {
        for ( Map.Entry<String, Boolean> entry : results.entrySet() )
        {
            try
            {
                repository.retrieveItem( repository.createUid( entry.getKey() ), null );

                // we succeeded, the value must be true
                assertTrue(
                    "The entry '" + entry.getKey() + "' was found in repository '" + repository.getId() + "' !",
                    entry.getValue() );
            }
            catch ( ItemNotFoundException e )
            {
                // we succeeded, the value must be true
                assertFalse( "The entry '" + entry.getKey() + "' was not found in repository '" + repository.getId()
                    + "' !", entry.getValue() );
            }
        }
    }

    protected File retrieveFile( Repository repo, String path )
        throws Exception
    {
        File root = new File( new URL( repo.getLocalUrl() ).toURI() );

        File result = new File( root, path );

        if ( result.exists() )
        {
            return result;
        }

        throw new FileNotFoundException( "File with path '" + path + "' in repository '" + repo.getId()
            + "' does not exist!" );
    }

    private Metadata readMavenMetadata( File mdFle )
        throws FileNotFoundException,
            MetadataException
    {
        FileInputStream inputStream = new FileInputStream( mdFle );
        Metadata md = null;

        try
        {
            md = MetadataBuilder.read( inputStream );
        }
        finally
        {
            if ( inputStream != null )
            {
                try
                {
                    inputStream.close();
                }
                catch ( IOException e1 )
                {
                }
            }
        }
        return md;
    }

    public void testRecreateMavenMetadataWalkerWalkerRelease()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        assertNotNull( inhouseRelease
            .retrieveItem( new ResourceStoreRequest( "/junit/junit/maven-metadata.xml", false ) ) );

    }

    public void testRecreateMavenMetadataWalkerWalkerSnapshot()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-api/maven-metadata.xml",
            false ) ) );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/maven-metadata.xml",
            false ) ) );
    }

    public void BROKENtestRecreateMavenMetadataWalkerWalkerSnapshotWithInterpolation()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/nexus1332/artifact-interp-main/maven-metadata.xml",
            false ) ) );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/nexus1332/artifact-interp-main/14.0.0-SNAPSHOT/maven-metadata.xml",
            false ) ) );
    }

    public void testRecreateMavenMetadataWalkerWalkerPlugin()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/plugins/maven-metadata.xml",
            false ) ) );
    }

    public void testRebuildChecksumFiles()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/junit/junit/3.8.1/junit-3.8.1.jar.md5",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/junit/junit/3.8.1/junit-3.8.1.jar.sha1",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/junit/junit/4.0/junit-4.0.pom.md5",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/junit/junit/maven-metadata.xml.md5",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/plugins/maven-metadata.xml.sha1",
            false ) ) );
    }

    public void testRemoveObsoleteFiles()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );
        
        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        
        expected.put("/junit/junit/4.4/junit-4.4.sources.jar.md5", Boolean.FALSE);
        expected.put("/junit/junit-mock/maven-metadata.xml", Boolean.FALSE);
        expected.put("/junit/junit/3.8.1/maven-metadata.xml", Boolean.FALSE);
        expected.put("/junit/junit/3.8.1/maven-metadata.xml.md5", Boolean.FALSE);
        expected.put("/junit/junit/3.8.1/maven-metadata.xml.sha1", Boolean.FALSE);
        
        validateResults( inhouseRelease, expected );;
    }

    public void testArtifactDirMdCorrect()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        Map<String, Boolean> expected = new HashMap<String, Boolean>();
        expected.put( "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseSnapshot, expected );

        Metadata md = readMavenMetadata( retrieveFile( inhouseSnapshot, "/com/mycom/proj2/maven-metadata.xml" ) );

        Assert.assertEquals( "20090226060812", md.getVersioning().getLastUpdated() );

    }

    public void testArtifactDirMdIncorrect()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new HashMap<String, Boolean>();
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/2.0/proj1-2.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/2.0/proj1-2.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/2.0/proj1-2.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/2.0/proj1-2.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/2.0/proj1-2.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/2.0/proj1-2.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseRelease, expected );

        Metadata md = readMavenMetadata( retrieveFile( inhouseRelease, "/com/mycom/proj1/maven-metadata.xml" ) );

        Assert.assertFalse( md.getVersioning().getLastUpdated().equals( "20090226060812" ) );
        Assert.assertEquals( "com.mycom", md.getGroupId() );
        Assert.assertEquals( "proj1", md.getArtifactId() );
        Assert.assertEquals( "2.0", md.getVersioning().getLatest() );
        Assert.assertEquals( "2.0", md.getVersioning().getRelease() );
        Assert.assertEquals( 2, md.getVersioning().getVersions().size() );
        Assert.assertTrue( md.getVersioning().getVersions().contains( "1.0" ) );
        Assert.assertTrue( md.getVersioning().getVersions().contains( "2.0" ) );
    }

    public void testVersionDirMdCorrect()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        Map<String, Boolean> expected = new HashMap<String, Boolean>();
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseSnapshot, expected );

        Metadata md = readMavenMetadata( retrieveFile(
            inhouseSnapshot,
            "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml" ) );

        Assert.assertEquals( "20090226060812", md.getVersioning().getLastUpdated() );
    }

    public void testVersionDirMdIncorrect()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseSnapshot, expected );

        Metadata md = readMavenMetadata( retrieveFile(
            inhouseSnapshot,
            "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml" ) );

        Assert.assertFalse( md.getVersioning().getLastUpdated().equals( "20090226060812" ) );
        Assert.assertEquals( "com.mycom", md.getGroupId() );
        Assert.assertEquals( "proj4", md.getArtifactId() );
        Assert.assertEquals( "1.0-SNAPSHOT", md.getVersion() );
        Assert.assertEquals( "20080924.191343", md.getVersioning().getSnapshot().getTimestamp() );
        Assert.assertEquals( 2, md.getVersioning().getSnapshot().getBuildNumber() );
    }

    public void testGroupDirMdCorrect()
        throws Exception
    {
        long oldTimestamp = retrieveFile( inhouseRelease, "/com/mycom/group1/maven-metadata.xml" ).lastModified();

        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group1/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseRelease, expected );

        long timeStamp = retrieveFile( inhouseRelease, "/com/mycom/group1/maven-metadata.xml" ).lastModified();

        Assert.assertEquals( oldTimestamp, timeStamp );
    }

    public void testGroupDirMdIncorrect()
        throws Exception
    {
        File oldFile = retrieveFile( inhouseRelease, "/com/mycom/group2/maven-metadata.xml" );
        long oldTimestamp = System.currentTimeMillis() - 10000L;
        oldFile.setLastModified( oldTimestamp );

        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group2/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseRelease, expected );

        long timeStamp = retrieveFile( inhouseRelease, "/com/mycom/group2/maven-metadata.xml" ).lastModified();

        Assert.assertFalse( oldTimestamp == timeStamp );

        Metadata md = readMavenMetadata( retrieveFile( inhouseRelease, "/com/mycom/group2/maven-metadata.xml" ) );

        Assert.assertEquals( 2, md.getPlugins().size() );

        for ( Object o : md.getPlugins() )
        {
            Plugin plugin = (Plugin) o;

            if ( plugin.getArtifactId().equals( "maven-p1-plugin" ) )
            {
                Assert.assertEquals( "Plugin P1", plugin.getName() );
                Assert.assertEquals( "p1", plugin.getPrefix() );
            }
            else if ( plugin.getArtifactId().equals( "maven-p2-plugin" ) )
            {
                Assert.assertTrue( StringUtils.isEmpty( plugin.getName() ) );
                Assert.assertEquals( "p2", plugin.getPrefix() );
            }
            else
            {
                Assert.fail( "The plugin '" + plugin.getArtifactId() + "' is incorrect" );
            }
        }
    }

}
