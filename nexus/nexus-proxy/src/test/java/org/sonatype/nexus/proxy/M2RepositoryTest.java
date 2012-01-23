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
package org.sonatype.nexus.proxy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

public class M2RepositoryTest
    extends M2ResourceStoreTest
{
    private static final long A_DAY = 24L * 60L * 60L * 1000L;

    protected static final String SPOOF_RELEASE = "/spoof/spoof/1.0/spoof-1.0.txt";

    protected static final String SPOOF_SNAPSHOT = "/spoof/spoof/1.0-SNAPSHOT/spoof-1.0-SNAPSHOT.txt";

    @Override
    protected String getItemPath()
    {
        return "/activemq/activemq-core/1.2/activemq-core-1.2.jar";
    }

    @Override
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryException, IOException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        repo1.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE );

        getApplicationConfiguration().saveConfiguration();

        return repo1;
    }

    @Test
    public void testPoliciesWithRetrieve()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        // a "release"
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        repository.getCurrentCoreConfiguration().commitChanges();

        StorageItem item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );

            fail( "Should not be able to get snapshot from release repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // reset NFC
        repository.expireCaches( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        repository.getCurrentCoreConfiguration().commitChanges();

        item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );

            fail( "Should not be able to get release from snapshot repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    @Test
    public void testPoliciesWithStore()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        // a "release"
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        repository.getCurrentCoreConfiguration().commitChanges();

        DefaultStorageFileItem item =
            new DefaultStorageFileItem( repository, SPOOF_RELEASE, true, true, new StringContentLocator( SPOOF_RELEASE ) );

        repository.storeItem( false, item );

        try
        {
            item =
                new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new StringContentLocator(
                    SPOOF_SNAPSHOT ) );

            repository.storeItem( false, item );

            fail( "Should not be able to store snapshot to release repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }

        // reset NFC
        repository.expireCaches( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        repository.getCurrentCoreConfiguration().commitChanges();

        item =
            new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new StringContentLocator(
                SPOOF_SNAPSHOT ) );

        repository.storeItem( false, item );

        try
        {
            item =
                new DefaultStorageFileItem( repository, SPOOF_RELEASE, true, true, new StringContentLocator(
                    SPOOF_RELEASE ) );

            repository.storeItem( false, item );

            fail( "Should not be able to store release to snapshot repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }
    }

    @Test
    public void testShouldServeByPolicies()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        String releasePom =
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.pom";
        String releaseArtifact =
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.jar";
        String snapshotPom =
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.pom";
        String snapshotArtifact =
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.jar";
        String metadata1 = "/org/codehaus/plexus/plexus-container-default/maven-metadata.xml";
        String metadataR = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/maven-metadata.xml";
        String metadataS = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/maven-metadata.xml";
        String someDirectory = "/classworlds/";
        String anyNonArtifactFile = "/any/file.txt";

        ResourceStoreRequest request = new ResourceStoreRequest( "" );

        // it is equiv of repo type: RELEASE
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        repository.getCurrentCoreConfiguration().commitChanges();

        request.setRequestPath( releasePom );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( releaseArtifact );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( snapshotPom );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( snapshotArtifact );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( metadata1 );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( metadataR );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( metadataS );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( someDirectory );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( anyNonArtifactFile );
        assertEquals( true, repository.shouldServeByPolicies( request ) );

        // it is equiv of repo type: SNAPSHOT
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        repository.getCurrentCoreConfiguration().commitChanges();

        request.setRequestPath( releasePom );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( releaseArtifact );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( snapshotPom );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( snapshotArtifact );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( metadata1 );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( metadataR );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( metadataS );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( someDirectory );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.setRequestPath( anyNonArtifactFile );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
    }

    @Test
    public void testGetLatestVersionSimple()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0.0" );
        versions.add( "1.0.1" );
        versions.add( "1.0.2" );
        versions.add( "1.1.2" );
        assertEquals( "1.1.2", repository.getLatestVersion( versions ) );
    }

    @Test
    public void testGetLatestVersionClassifiers()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0-alpha-19" );
        versions.add( "1.0-alpha-9-stable-1" );
        versions.add( "1.0-alpha-20" );
        versions.add( "1.0-alpha-21" );
        versions.add( "1.0-alpha-22" );
        versions.add( "1.0-alpha-40" );
        assertEquals( "1.0-alpha-40", repository.getLatestVersion( versions ) );
    }

    @Test
    public void testIsSnapshot()
        throws Exception
    {
        // M2Repository repository = (M2Repository) getResourceStore();

        assertEquals( false, Gav.isSnapshot( "1.0.0" ) );
        assertEquals( true, Gav.isSnapshot( "1.0.0-SNAPSHOT" ) );
        assertEquals( false, Gav.isSnapshot( "1.0-alpha-25" ) );
        assertEquals( true, Gav.isSnapshot( "1.0-alpha-25-20070518.002146-2" ) );
    }

    @Test
    public void testExpiration_NEXUS1675()
        throws Exception
    {
        doTestExpiration( "/spoof/maven-metadata.xml", 0, 3, 5, 1 );
    }

    @Test
    public void testExpiration_NEXUS3065()
        throws Exception
    {
        // "defaults"
        // enforce = true, hence even if 1stround age = 0 (always), enforce will prevent redownload, so 1st round will
        // have 1 remote hits
        doTestExpiration( "/spoof/spoof/1.0/spoof-1.0.txt", 0, 3, -1, 1 );

        // "overrides"
        // enforce = false, hence since 1stround age = 0 (always), 1st round will
        // have 3 remote hits
        // doTestExpiration( "/spoof/spoof/1.0/spoof-1.0.txt", false, 0, 3, -1, 1 );
    }

    public void doTestExpiration( String path, int age1stround, int remoteHitsExpected1stround, int age2ndround,
                                  int remoteHitsExpected2ndround )
        throws Exception
    {
        CounterListener ch = new CounterListener();

        M2Repository repository = (M2Repository) getResourceStore();

        getApplicationEventMulticaster().addEventListener( ch );

        File mdFile = new File( new File( getBasedir() ), "target/test-classes/repo1" + path );

        assertTrue( mdFile.exists() );

        // ==

        try
        {
            repository.deleteItem( new ResourceStoreRequest( "/spoof", true ) );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore
        }

        repository.setMetadataMaxAge( age1stround );
        repository.setArtifactMaxAge( age1stround );
        // not configurable
        // repository.setEnforceReleaseRedownloadPolicy( enforceReleaseRedownloadPolicy );
        repository.getCurrentCoreConfiguration().commitChanges();

        mdFile.setLastModified( System.currentTimeMillis() - ( 3L * 24L * 60L * 60L * 1000L ) );

        Thread.sleep( 500 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( path, false ) );

        mdFile.setLastModified( System.currentTimeMillis() - ( 2L * 24L * 60L * 60L * 1000L ) );

        Thread.sleep( 500 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( path, false ) );

        mdFile.setLastModified( System.currentTimeMillis() - ( 1L * 24L * 60L * 60L * 1000L ) );

        Thread.sleep( 500 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( path, false ) );

        assertEquals( "Remote hits cound fail (1st round)!", remoteHitsExpected1stround, ch.getRequestCount() );

        // ==

        ch.reset();

        try
        {
            repository.deleteItem( new ResourceStoreRequest( "/spoof", true ) );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore
        }

        repository.setMetadataMaxAge( age2ndround );
        repository.setArtifactMaxAge( age2ndround );
        // not configurable
        // repository.setEnforceReleaseRedownloadPolicy( enforceReleaseRedownloadPolicy );
        repository.getCurrentCoreConfiguration().commitChanges();

        mdFile.setLastModified( System.currentTimeMillis() );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( path, false ) );

        mdFile.setLastModified( System.currentTimeMillis() );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( path, false ) );

        mdFile.setLastModified( System.currentTimeMillis() );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( path, false ) );

        assertEquals( "Remote hits cound fail (2nd round)!", remoteHitsExpected2ndround, ch.getRequestCount() );
    }

    @Test
    public void testLocalStorageChanges()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        String changedUrl = repository.getLocalUrl() + "foo";

        repository.setLocalUrl( changedUrl );

        assertFalse( "Should not be the same!", changedUrl.equals( repository.getLocalUrl() ) );

        repository.getCurrentCoreConfiguration().commitChanges();

        assertTrue( "Should be the same!", changedUrl.equals( repository.getLocalUrl() ) );
    }

    @Test
    public void testRemoteStorageChanges()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        String changedUrl = repository.getRemoteUrl() + "/foo/";

        repository.setRemoteUrl( changedUrl );

        assertFalse( "Should not be the same!", changedUrl.equals( repository.getRemoteUrl() ) );

        repository.getCurrentCoreConfiguration().commitChanges();

        assertTrue( "Should be the same!", changedUrl.equals( repository.getRemoteUrl() ) );
    }

    @Test
    public void testProxyLastRequestedAttribute()
        throws Exception
    {
        M2Repository repository = (M2Repository) this.getRepositoryRegistry().getRepository( "repo1" );

        String item = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.pom";
        ResourceStoreRequest request = new ResourceStoreRequest( item );
        request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        StorageItem storageItem = repository.retrieveItem( request );
        long lastRequest = System.currentTimeMillis() - 10 * A_DAY;
        storageItem.setLastRequested( lastRequest );
        repository.storeItem( false, storageItem );

        // now request the object, the lastRequested timestamp should be updated
        StorageItem resultItem = repository.retrieveItem( request );
        Assert.assertTrue( resultItem.getLastRequested() > lastRequest );

        // check the shadow attributes
        Attributes shadowStorageItem =
            repository.getAttributesHandler().getAttributeStorage().getAttributes(
                repository.createUid( request.getRequestPath() ) );
        Assert.assertEquals( resultItem.getLastRequested(), shadowStorageItem.getLastRequested() );
    }

    @Test
    public void testHostedLastRequestedAttribute()
        throws Exception
    {
        String itemPath = "/org/test/foo.junk";

        M2Repository repository = (M2Repository) this.getRepositoryRegistry().getRepository( "inhouse" );
        File inhouseLocalStorageDir =
            new File(
                new URL( ( (CRepositoryCoreConfiguration) repository.getCurrentCoreConfiguration() ).getConfiguration(
                    false ).getLocalStorage().getUrl() ).getFile() );

        File artifactFile = new File( inhouseLocalStorageDir, itemPath );
        artifactFile.getParentFile().mkdirs();

        FileUtils.fileWrite( artifactFile.getAbsolutePath(), "Some Text so the file is not empty" );

        ResourceStoreRequest request = new ResourceStoreRequest( itemPath );
        request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        StorageItem storageItem = repository.retrieveItem( request );
        long lastRequest = System.currentTimeMillis() - 10 * A_DAY;
        storageItem.setLastRequested( lastRequest );
        repository.storeItem( false, storageItem );

        // now request the object, the lastRequested timestamp should be updated
        StorageItem resultItem = repository.retrieveItem( request );
        Assert.assertTrue( resultItem.getLastRequested() > lastRequest );

        // check the shadow attributes
        Attributes shadowStorageItem =
            repository.getAttributesHandler().getAttributeStorage().getAttributes(
                repository.createUid( request.getRequestPath() ) );
        Assert.assertEquals( resultItem.getLastRequested(), shadowStorageItem.getLastRequested() );
    }

    // NEXUS-4218 BEGIN

    @Test
    public void testNEXUS4218HowWeSpottedItMetadataChecksum()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata checksum, and there is no UA neither
        // remote address
        // this is the actual case made us realize the existence of the bug
        final String path = "/org/slf4j/slf4j-api/1.4.3/maven-metadata.xml.sha1";

        doTestNEXUS4218( path, null, null );
    }

    @Test
    public void testNEXUS4218HowWeSpottedItMetadata()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata, and there is no UA neither remote
        // address
        final String path = "/org/slf4j/slf4j-api/1.4.3/maven-metadata.xml";

        doTestNEXUS4218( path, null, null );
    }

    @Test
    public void testNEXUS4218JustAnArtifactChecksum()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven artifact checksum, and there is no UA neither
        // remote address
        final String path = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.jar.sha1";

        doTestNEXUS4218( path, null, null );
    }

    @Test
    public void testNEXUS4218JustAnArtifact()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata, and there is no UA neither remote
        // address
        final String path = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.jar";

        doTestNEXUS4218( path, null, null );
    }

    @Test
    public void testNEXUS4218HowWeSpottedItMetadataChecksumWithNotTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata checksum, and there is no UA neither
        // remote address
        // this case made us realize the existince of the bug
        final String path = "/org/slf4j/slf4j-api/1.4.3/maven-metadata.xml.sha1";

        doTestNEXUS4218( path, "Apache-Maven/3", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218HowWeSpottedItMetadataWithNotTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata, and there is no UA neither remote
        // address
        final String path = "/org/slf4j/slf4j-api/1.4.3/maven-metadata.xml";

        doTestNEXUS4218( path, "Apache-Maven/3", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218JustAnArtifactChecksumWithNotTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven artifact checksum, and there is no UA neither
        // remote address
        final String path = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.jar.sha1";

        doTestNEXUS4218( path, "Apache-Maven/3", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218JustAnArtifactWithNotTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata, and there is no UA neither remote
        // address
        final String path = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.jar";

        doTestNEXUS4218( path, "Apache-Maven/3", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218HowWeSpottedItMetadataChecksumWithTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata checksum, and there is no UA neither
        // remote address
        // this case made us realize the existince of the bug
        final String path = "/org/slf4j/slf4j-api/1.4.3/maven-metadata.xml.sha1";

        doTestNEXUS4218( path, "Apache-Maven/2", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218HowWeSpottedItMetadataWithTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata, and there is no UA neither remote
        // address
        final String path = "/org/slf4j/slf4j-api/1.4.3/maven-metadata.xml";

        doTestNEXUS4218( path, "Apache-Maven/2", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218JustAnArtifactChecksumWithTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven artifact checksum, and there is no UA neither
        // remote address
        final String path = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.jar.sha1";

        doTestNEXUS4218( path, "Apache-Maven/2", "127.0.0.1" );
    }

    @Test
    public void testNEXUS4218JustAnArtifactWithTriggeringUAAndIP()
        throws Exception
    {
        // here, we use path that does conform to M2 layout, but is maven metadata, and there is no UA neither remote
        // address
        final String path = "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.jar";

        doTestNEXUS4218( path, "Apache-Maven/2", "127.0.0.1" );
    }

    public void doTestNEXUS4218( final String path, final String userAgent, final String remoteAddress )
        throws Exception
    {
        final M2Repository repository = (M2Repository) getResourceStore();
        // we check our expectation agains repository: it has to be Maven2 proxy repository
        Assert.assertTrue( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );
        Assert.assertTrue( Maven2ContentClass.ID.equals( repository.getRepositoryContentClass().getId() ) );
        File repositoryLocalStorageDir =
            new File(
                new URL( ( (CRepositoryCoreConfiguration) repository.getCurrentCoreConfiguration() ).getConfiguration(
                    false ).getLocalStorage().getUrl() ).getFile() );

        // create a request and equip it as needed
        final ResourceStoreRequest request = new ResourceStoreRequest( path );
        if ( userAgent != null )
        {
            request.getRequestContext().put( AccessManager.REQUEST_AGENT, userAgent );
        }
        if ( remoteAddress != null )
        {
            request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, remoteAddress );
        }

        // invoke expire caches
        repository.expireCaches( request );

        // NO file should be pulled down
        {
            File artifactFile = new File( repositoryLocalStorageDir, path.substring( 1 ) );
            Assert.assertFalse( artifactFile.exists() );

            if ( M2ArtifactRecognizer.isChecksum( path ) )
            {
                File artifactMainFile =
                    new File( repositoryLocalStorageDir, path.substring( 1, path.length() - ".sha1".length() ) );
                Assert.assertFalse( artifactMainFile.exists() );
            }
        }

        // create a request and equip it as needed (requests should NOT be reused!)
        final ResourceStoreRequest retrieveRequest = new ResourceStoreRequest( path );
        if ( userAgent != null )
        {
            retrieveRequest.getRequestContext().put( AccessManager.REQUEST_AGENT, userAgent );
        }
        if ( remoteAddress != null )
        {
            retrieveRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, remoteAddress );
        }

        // now do a fetch
        // this verifies that an assertion ("path in question does not exists in proxy cache but does exists on remote")
        // also verifies that cstamas did not mistype the paths in @Test method ;)
        repository.retrieveItem( retrieveRequest );

        // files SHOULD be pulled down
        {
            File artifactFile = new File( repositoryLocalStorageDir, path.substring( 1 ) );
            Assert.assertTrue( artifactFile.exists() );

            if ( !M2ArtifactRecognizer.isChecksum( path ) )
            {
                File artifactMainFile =
                    new File( repositoryLocalStorageDir, path.substring( 1, path.length() ) +".sha1" );
                Assert.assertTrue( artifactMainFile.exists() );
            }
        }
    }

    // NEXUS-4218 END

    // ==

    protected class CounterListener
        implements EventListener
    {
        private int requestCount = 0;

        public int getRequestCount()
        {
            return this.requestCount;
        }

        public void reset()
        {
            this.requestCount = 0;
        }

        public void onEvent( Event<?> evt )
        {
            if ( evt instanceof RepositoryItemEventCache
                && ( ( (RepositoryItemEventCache) evt ).getItem().getPath().endsWith( "maven-metadata.xml" ) || ( (RepositoryItemEventCache) evt ).getItem().getPath().endsWith(
                    "spoof-1.0.txt" ) ) )
            {
                requestCount = requestCount + 1;
            }
        }
    }
}
