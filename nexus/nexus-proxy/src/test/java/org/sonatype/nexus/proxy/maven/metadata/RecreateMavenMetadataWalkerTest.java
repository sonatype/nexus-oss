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
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreUpdate;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.RecreateMavenMetadataWalkerProcessor;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalkerTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private Repository inhouseRelease;

    private Repository inhouseSnapshot;

    private File repoBase;

    private Walker walker;

    private String[] releaseArtifactFiles = { "/junit/junit/3.8.1/junit-3.8.1.jar",
        "/junit/junit/3.8.1/junit-3.8.1.pom", "/junit/junit/3.8.2/junit-3.8.2.jar",
        "/junit/junit/3.8.2/junit-3.8.2.pom", "/junit/junit/4.0/junit-4.0.jar", "/junit/junit/4.0/junit-4.0.pom",
        "/junit/junit/4.4/junit-4.4.jar", "/junit/junit/4.4/junit-4.4.pom",
        "/junit/junit/4.4/junit-4.4.sources.jar.md5", "/junit/junit-mock/maven-metadata.xml",
        "/junit/junit-mock/1.1/readme.txt", "/com/mycom/proj1/1.0/proj1-1.0.jar", "/com/mycom/proj1/1.0/proj1-1.0.pom",
        "/com/mycom/proj1/2.0/proj1-2.0.jar", "/com/mycom/proj1/2.0/proj1-2.0.pom",
        "/com/mycom/proj1/maven-metadata.xml", "/com/mycom/proj5/1.0/proj5-1.0.jar",
        "/com/mycom/proj5/1.0/proj5-1.0.pom", "/com/mycom/proj6/1.0/proj6-1.0.jar",
        "/com/mycom/proj6/1.0/proj6-1.0.pom", "/com/mycom1/1-proj/1.0/1-proj-1.0.pom",
        "/com/mycom1/1.0/mycom1-1.0.pom", "/com/mycom1/2-proj/1.0/2-proj-1.0.pom", "/com/mycom1/2.0/mycom1-2.0.pom",
        "/com/mycom2/proj-1/1.0/proj-1-1.0.pom", "/com/mycom2/proj-1/2.0/proj-1-2.0.pom",
        "/com/mycom2/proj-1/maven-metadata.xml" };

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
        "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.jar", "/com/mycom/proj2/1.0-SNAPSHOT/proj2-1.0-SNAPSHOT.pom",
        "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.jar", "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.pom",
        "/com/mycom/proj2/maven-metadata.xml", "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.jar",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080923.191343-1.pom",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.jar",
        "/com/mycom/proj3/1.0-SNAPSHOT/proj3-1.0-20080924.191343-2.pom",
        "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.jar",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080923.191343-1.pom",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.jar",
        "/com/mycom/proj4/1.0-SNAPSHOT/proj4-1.0-20080924.191343-2.pom",
        "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml",
        "/com/mycom/proj7/1.0-SNAPSHOT/proj7-1.0-20080924.191343-2.pom",
        "/com/mycom/proj7/1.0-SNAPSHOT/proj7-1.0-20080924.191343-2.jar" };

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
        "/com/mycom/group1/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom", "/com/mycom/group1/maven-metadata.xml",
        "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.jar",
        "/com/mycom/group2/maven-p1-plugin/1.0/maven-p1-plugin-1.0.pom",
        "/com/mycom/group2/maven-p1-plugin/2.0/maven-p1-plugin-2.0.jar",
        "/com/mycom/group2/maven-p1-plugin/2.0/maven-p1-plugin-2.0.pom",
        "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.jar",
        "/com/mycom/group2/maven-p2-plugin/1.0/maven-p2-plugin-1.0.pom", "/com/mycom/group2/maven-metadata.xml",
        "/com/mycom/group3/maven-a1-plugin/1.0/maven-a1-plugin-1.0.pom",
        "/com/mycom/group3/maven-a1-plugin/2.0/maven-a1-plugin-2.0.pom",
        "/com/mycom/group3/maven-a1-plugin/3.0/maven-a1-plugin-3.0.pom",
        "/com/mycom/group3/maven-b1-plugin/1.0/maven-b1-plugin-1.0.pom",
        "/com/mycom/group3/maven-c1-plugin/1.0/maven-c1-plugin-1.0.pom",
        "/com/mycom/group3/maven-d1-plugin/1.0/maven-d1-plugin-1.0.pom" };

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

        repoBase = new File( getBasedir(), "target/test-classes/mavenMetadataTestRepo" );

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
        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor( getLogger() );

        DefaultWalkerContext ctx =
            new DefaultWalkerContext( repo, new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

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
                ResourceStoreRequest req = new ResourceStoreRequest( entry.getKey(), true );

                repository.retrieveItem( req );

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
        throws MetadataException, IOException
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

    // ==

    /**
     * This test is to assert that there is no unjustified checksum file creation happening on system (existing
     * _correct_ checksums are not overwritten with new files having SAME content).
     */
    @Test
    public void testRebuildMavenMetadataIsSmarter()
        throws Exception
    {
        final Repository repo = inhouseRelease;

        // == 1st pass: we recreate all the maven metadata for given repo to have them all in place
        {
            final DefaultWalkerContext ctx =
                new DefaultWalkerContext( repo, new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );
            ctx.getProcessors().add( new RecreateMavenMetadataWalkerProcessor( getLogger() ) );
            walker.walk( ctx );
        }

        // === 2nd pass: all MD is recreated, and they are valid, NO overwrite should happen at all!
        {
            final DefaultWalkerContext ctx =
                new DefaultWalkerContext( repo, new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );
            ctx.getProcessors().add( new RecreateMavenMetadataWalkerProcessor( getLogger() ) );
            final ValidationEventListener validationEventListener = new ValidationEventListener();
            getApplicationEventMulticaster().addEventListener( validationEventListener );
            walker.walk( ctx );
            getApplicationEventMulticaster().removeEventListener( validationEventListener );
            assertFalse( "We should not record any STORE!", validationEventListener.hasStoresRecorded() );
        }

        // === 3rd pass: e manually "break" one checksum, and expect that one only to be overwritten
        {
            final String checksumPath = "/com/mycom/group1/maven-metadata.xml.sha1";
            final String checksumPathMd5 = "/com/mycom/group1/maven-metadata.xml.md5";
            // coming from http://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.pom.sha1
            final String wrongChecksum = "93c66c9afd6cf7b91bd4ecf38a60ca48fc5f2078";

            repo.storeItem( new ResourceStoreRequest( checksumPath ),
                new ByteArrayInputStream( wrongChecksum.getBytes( "UTF-8" ) ), null );

            final DefaultWalkerContext ctx =
                new DefaultWalkerContext( repo, new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );
            ctx.getProcessors().add( new RecreateMavenMetadataWalkerProcessor( getLogger() ) );
            final ValidationEventListener validationEventListener = new ValidationEventListener();
            getApplicationEventMulticaster().addEventListener( validationEventListener );
            walker.walk( ctx );
            getApplicationEventMulticaster().removeEventListener( validationEventListener );
            assertTrue( "We should record one STORE!", validationEventListener.hasStoresRecorded() );
            assertEquals( "There should be only 2 STOREs!", 2, validationEventListener.storeCount() );
            assertTrue( "This checksum should be recreated!", validationEventListener.isOverwritten( checksumPath ) );
            // if SHA1 detected a broken, BOTH sha1 and md5 are recreated
            assertTrue( "This checksum should be recreated!", validationEventListener.isOverwritten( checksumPathMd5 ) );
        }

    }

    // ==

    /**
     * We are listening for store events, and are gathering them... FOUL happens if a checksum storeUpdate (so,
     * overwrite happens) event flies in, without having it's main file already gathered stored (it's path is already
     * gathered). We do this for SHA1's only since both checksums are handled at same place by DefaultMetadataHelper,
     * hence, if one changes, both are changing.
     * 
     * @author cstamas
     */
    public static class ValidationEventListener
        implements EventListener
    {
        private HashSet<String> pathsFromStoreEvents;

        public ValidationEventListener()
        {
            this.pathsFromStoreEvents = new HashSet<String>();
        }

        public boolean isOverwritten( final String path )
        {
            return pathsFromStoreEvents.contains( path );
        }

        public boolean hasStoresRecorded()
        {
            return !pathsFromStoreEvents.isEmpty();
        }

        public int storeCount()
        {
            return pathsFromStoreEvents.size();
        }

        @Override
        public void onEvent( Event<?> evt )
        {
            if ( evt instanceof RepositoryItemEventStore )
            {
                final RepositoryItemEventStore sevt = (RepositoryItemEventStore) evt;
                pathsFromStoreEvents.add( sevt.getItem().getRepositoryItemUid().getPath() );
            }
        }
    }

    // ==

    @Test
    public void testRecreateMavenMetadataWalkerWalkerRelease()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest( "/junit/junit/maven-metadata.xml", false ) ) );

    }

    @Test
    public void testRecreateMavenMetadataWalkerWalkerSnapshot()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-api/maven-metadata.xml", false ) ) );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/maven-metadata.xml", false ) ) );
    }

    public void BROKENtestRecreateMavenMetadataWalkerWalkerSnapshotWithInterpolation()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/nexus1332/artifact-interp-main/maven-metadata.xml", false ) ) );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/nexus1332/artifact-interp-main/14.0.0-SNAPSHOT/maven-metadata.xml", false ) ) );
    }

    @Test
    public void testRecreateMavenMetadataWalkerWalkerPlugin()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        final String path = "/org/apache/maven/plugins/maven-metadata.xml";
        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest( path, false ) ) );

        Metadata md = readMavenMetadata( retrieveFile( inhouseRelease, path ) );
        List<Plugin> plugins = md.getPlugins();
        assertNotNull( plugins );
        assertEquals( 5, plugins.size() );

        // cstamas: the plugin prefix is usually _same_ across versions
        // Here, Velo changed prefix -- it corresponds to "p<VersionWithoutDots>.
        // Since AddPluginOperation adds only the 1st plugin it encounters (equality is checked by GA)
        // and since Gian's fix for _ordered_ file input, the "first plugin prefix" wins case happens here.
        // so, the line below is wrong, since maven-plugin-plugin 2.4.1 is added 1st time, and it's metadata
        // will get into group level metadata.xml, while next one -- with different prefix -- will be just ignored.
        // assertEquals( "p243", pluginPlugin.getPrefix() );

        // more fixes: it turned out that AddPluginOperation was wrongly implemented: plugins with same artifactId and
        // different prefixes should be enlisted as _two_ (or as many as many prefixes found) times.
        // So below, the test changed to check that both prefixes are enlisted!

        boolean contains;

        contains = false;
        for ( Plugin plugin : plugins )
        {
            if ( "p241".equals( plugin.getPrefix() ) )
            {
                contains = true;
                break;
            }
        }

        assertTrue( "p241 is not enlisted as prefix!", contains );

        contains = false;
        for ( Plugin plugin : plugins )
        {
            if ( "p243".equals( plugin.getPrefix() ) )
            {
                contains = true;
                break;
            }
        }

        assertTrue( "p243 is not enlisted as prefix!", contains );
    }

    @Test
    public void testRebuildChecksumFiles()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest( "/junit/junit/3.8.1/junit-3.8.1.jar.md5",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/junit/junit/3.8.1/junit-3.8.1.jar.sha1", false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest( "/junit/junit/4.0/junit-4.0.pom.md5",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest( "/junit/junit/maven-metadata.xml.md5",
            false ) ) );

        assertNotNull( inhouseRelease.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/plugins/maven-metadata.xml.sha1", false ) ) );
    }

    @Test
    public void testRemoveObsoleteFiles()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();

        expected.put( "/junit/junit/4.4/junit-4.4.sources.jar.md5", Boolean.FALSE );
        expected.put( "/junit/junit-mock/maven-metadata.xml", Boolean.FALSE );
        expected.put( "/junit/junit/3.8.1/maven-metadata.xml", Boolean.FALSE );
        expected.put( "/junit/junit/3.8.1/maven-metadata.xml.md5", Boolean.FALSE );
        expected.put( "/junit/junit/3.8.1/maven-metadata.xml.sha1", Boolean.FALSE );

        validateResults( inhouseRelease, expected );
    }

    @Test
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
        expected.put( "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/2.0SNAPSHOT/proj2-2.0SNAPSHOT.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj2/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseSnapshot, expected );

        Metadata md = readMavenMetadata( retrieveFile( inhouseSnapshot, "/com/mycom/proj2/maven-metadata.xml" ) );

        // NEXUS-3148
        // the MD has to be updated, the 2.0SNAPSHOT was added
        Assert.assertFalse( "20090226060812".equals( md.getVersioning().getLastUpdated() ) );
        Assert.assertEquals( "2.0SNAPSHOT", md.getVersioning().getLatest() );
    }

    @Test
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

    @Test
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

        Metadata md =
            readMavenMetadata( retrieveFile( inhouseSnapshot, "/com/mycom/proj3/1.0-SNAPSHOT/maven-metadata.xml" ) );

        Assert.assertEquals( "20090226060812", md.getVersioning().getLastUpdated() );
        Assert.assertTrue( "We have two snapshots", md.getVersioning().getSnapshotVersions().size() == 2 );
    }

    @Test
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

        Metadata md =
            readMavenMetadata( retrieveFile( inhouseSnapshot, "/com/mycom/proj4/1.0-SNAPSHOT/maven-metadata.xml" ) );

        // everytime nexus thouch the metadata it bumps the last update time
        // Assert.assertEquals( "20090226060812", md.getVersioning().getLastUpdated() );
        Assert.assertEquals( "com.mycom", md.getGroupId() );
        Assert.assertEquals( "proj4", md.getArtifactId() );
        Assert.assertEquals( "1.0-SNAPSHOT", md.getVersion() );
        Assert.assertEquals( "20080924.191343", md.getVersioning().getSnapshot().getTimestamp() );
        Assert.assertEquals( 2, md.getVersioning().getSnapshot().getBuildNumber() );
        Assert.assertEquals( "1.1.0", md.getModelVersion() );
    }

    @Test
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

    @Test
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

    @Test
    public void testGroupDirMdElementUniqueAndSorted()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom/group3/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/group3/maven-metadata.xml.md5", Boolean.TRUE );
        expected.put( "/com/mycom/group3/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( inhouseRelease, expected );

        Metadata md = readMavenMetadata( retrieveFile( inhouseRelease, "/com/mycom/group3/maven-metadata.xml" ) );

        assertEquals( 4, md.getPlugins().size() );

        assertEquals( "maven-a1-plugin", ( md.getPlugins().get( 0 ) ).getArtifactId() );
        assertEquals( "maven-b1-plugin", ( md.getPlugins().get( 1 ) ).getArtifactId() );
        assertEquals( "maven-c1-plugin", ( md.getPlugins().get( 2 ) ).getArtifactId() );
        assertEquals( "maven-d1-plugin", ( md.getPlugins().get( 3 ) ).getArtifactId() );
    }

    @Test
    public void testRecreatingOnBadPOM()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom/proj1/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj1/1.0/proj1-1.0.pom.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/1.0/proj5-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/1.0/proj5-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/1.0/proj5-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/1.0/proj5-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/1.0/proj5-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj5/1.0/proj5-1.0.pom.sha1", Boolean.TRUE );

        validateResults( inhouseRelease, expected );

        // should see warning log here
    }

    @Test
    public void testReleasePOMWithInterpolation()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom/proj6/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj6/1.0/proj6-1.0.jar", Boolean.TRUE );
        expected.put( "/com/mycom/proj6/1.0/proj6-1.0.jar.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj6/1.0/proj6-1.0.jar.sha1", Boolean.TRUE );
        expected.put( "/com/mycom/proj6/1.0/proj6-1.0.pom", Boolean.TRUE );
        expected.put( "/com/mycom/proj6/1.0/proj6-1.0.pom.md5", Boolean.TRUE );
        expected.put( "/com/mycom/proj6/1.0/proj6-1.0.pom.sha1", Boolean.TRUE );

        validateResults( inhouseRelease, expected );
    }

    @Test
    public void testSnapshotPOMWithInterpolation()
        throws Exception
    {
        rebuildMavenMetadata( inhouseSnapshot );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();

        expected.put( "/com/mycom/proj7/maven-metadata.xml", Boolean.TRUE );
        expected.put( "/com/mycom/proj7/1.0-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );

        validateResults( inhouseSnapshot, expected );
    }

    @Test
    public void testGroupPathIsArtifactPathAtTheSameTime()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom1/maven-metadata.xml", Boolean.TRUE );
        validateResults( inhouseRelease, expected );

        Metadata md = readMavenMetadata( retrieveFile( inhouseRelease, "/com/mycom1/maven-metadata.xml" ) );
        assertEquals( "com", md.getGroupId() );
        assertEquals( "mycom1", md.getArtifactId() );
        assertEquals( "2.0", md.getVersioning().getLatest() );
        assertEquals( "2.0", md.getVersioning().getRelease() );

        List<String> versions = new ArrayList<String>( 2 );
        versions.add( "1.0" );
        versions.add( "2.0" );
        assertEquals( versions, md.getVersioning().getVersions() );
    }

    @Test
    public void testMetadata0Bytes()
        throws Exception
    {
        rebuildMavenMetadata( inhouseRelease );

        Map<String, Boolean> expected = new LinkedHashMap<String, Boolean>();
        expected.put( "/com/mycom2/proj-1/maven-metadata.xml", Boolean.TRUE );
        validateResults( inhouseRelease, expected );

        Metadata md = readMavenMetadata( retrieveFile( inhouseRelease, "/com/mycom2/proj-1/maven-metadata.xml" ) );
        assertEquals( "com.mycom2", md.getGroupId() );
        assertEquals( "proj-1", md.getArtifactId() );
        assertEquals( "2.0", md.getVersioning().getLatest() );
        assertEquals( "2.0", md.getVersioning().getRelease() );

        List<String> versions = new ArrayList<String>( 2 );
        versions.add( "1.0" );
        versions.add( "2.0" );
        assertEquals( versions, md.getVersioning().getVersions() );
    }

}
