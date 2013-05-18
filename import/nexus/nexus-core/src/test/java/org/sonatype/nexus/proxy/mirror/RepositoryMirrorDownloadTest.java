/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.mirror;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.internal.MockRemoteStorage;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteProviderHintFactory;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.sisu.litmus.testsupport.mock.MockitoRule;

/**
 * Tests for repository mirror configurations.
 * <p/>
 * When a proxy repository is confgured to retrieve artifacts from a Mirror, it will consult each configured mirror in
 * the order shown in the Mirror URLs list.
 * <p/>
 * While Nexus will retrieve POM information, checksums, and PGP signatures from the original repository, it will
 * retrieve all other artfiacts directly from the first available mirror.
 */
public class RepositoryMirrorDownloadTest
    extends AbstractNexusTestEnvironment
{

    private static final String ITEM_PATH = "/path";

    private static final Mirror MIRROR1 = new Mirror( "1", "http://mirror1-url/" );

    private static final Mirror MIRROR2 = new Mirror( "2", "http://mirror2-url/" );

    private static final String CANONICAL_URL = "http://canonical-url/";

    private static final ItemNotFoundException itemNotFound = new ItemNotFoundException( new ResourceStoreRequest(
        ITEM_PATH ) );

    private static final RemoteStorageException storageException = new RemoteStorageException( ITEM_PATH );

    private static final byte[] ITEM_CONTENT = new byte[0];

    private static final String ITEM_SHA1_HASH = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

    private static final String ITEM_BAD_SHA1_HASH = "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE";

    private static final RemoteAccessException accessDenied = new RemoteAccessException( null, ITEM_PATH )
    {

    };

    private RemoteProviderHintFactory remoteProviderHintFactory;

    // this is crazy...
    private static class AssertionRequest
    {

        /**
         * Mirrors
         */
        public Mirror[] mirrors = new Mirror[]{ MIRROR1, MIRROR2 };

        /**
         * Mirror failures
         */
        public Exception[] mirrorFailures = new Exception[0];

        /**
         * If mirror is expected to retrieve item successfully after failing according to {@link #mirrorFailures}
         */
        public boolean mirrorSuccess;

        /**
         * Canonical failures
         */
        public Exception[] canonicalFailures = new Exception[0];

        /**
         * If canonical is expected to retrieve item successfully after failing according to {@link #canonicalFailures}
         */
        public boolean canonicalSuccess;

        /**
         * If mirror is expected to be blacklisted
         */
        public boolean assertMirrorBlacklisted;

        /**
         * Expected failure type or null if operation is expected to succeed
         */
        public Class<? extends Exception> assertFailureType;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        remoteProviderHintFactory = lookup( RemoteProviderHintFactory.class );
    }

    @Test
    public void testDownloadFromMirror()
        throws Exception
    {
        // given an M2Repo proxy with a single mirror configured
        // when requesting an item from the proxy
        // then the artifact is fetched from mirror
        // and the hash is retrieved from the canonical ( never mirror ) repository

        // setup -----------
        final M2Repository repo = createM2Repository( new Mirror[]{ MIRROR1 } ); // this is not a mock
        final RemoteRepositoryStorage mockRemoteStorage = mock( RemoteRepositoryStorage.class );
        //final LocalRepositoryStorage mockLocalStorage = createMockEmptyLocalStorageRepository();
        doReturn( "dummy" ).when( mockRemoteStorage ).getProviderId();
        // set our fake storage into the repo
        repo.setRemoteStorage( mockRemoteStorage );
        //repo.setLocalStorage( mockLocalStorage );

        // create a 'real' uid, from the real repo, for the fake artifact
        final RepositoryItemUid uid = repo.createUid( ITEM_PATH );
        final AbstractStorageItem storageItem = createRealRemoteStorageFileItem( uid, ITEM_CONTENT );

        // have to ask the mirror
        doReturn( storageItem ).when( mockRemoteStorage ).retrieveItem( same( repo ),
                                                                        (ResourceStoreRequest) anyObject(),
                                                                        eq( MIRROR1.getUrl() ) );

        // checksums are from canonical
        doThrow( itemNotFound ).when( mockRemoteStorage ).retrieveItem( same( repo ),
                                                                        (ResourceStoreRequest) anyObject(),
                                                                        eq( CANONICAL_URL ) );

        // requesting the item from the repo ---------------
        ResourceStoreRequest req = new ResourceStoreRequest( ITEM_PATH, false );
        repo.retrieveItem( req );

        // verify
        verify( mockRemoteStorage, times( 1 ) ).validateStorageUrl( MIRROR1.getUrl() );
        verify( mockRemoteStorage, times( 2 ) ).retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(),
                                                              eq( CANONICAL_URL ) );
        verify( mockRemoteStorage, times( 1 ) ).retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(),
                                                              eq( MIRROR1.getUrl() ) );

    }

    @Test
    public void whenCanonicalAndMirrorCannotFindArtifactThenRequestShouldFailWithItemNotFound()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ itemNotFound };
        req.canonicalFailures = new Exception[]{ itemNotFound };
        req.assertFailureType = ItemNotFoundException.class;
        assertDownloadFromMirror( req );

    }

    @Test
    public void whenMirrorCannotFindArtifactAndCanonicalCanThenShouldNotBlacklistMirror()
        throws Exception
    {
        AssertionRequest req;

        // mirror fails, but canonical succeeds => not blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ itemNotFound };
        req.canonicalSuccess = true;
        assertDownloadFromMirror( req );
    }

    @Test
    public void whenCanonicalAndMirrorDenyAccessThenRequestShouldFailWithItemNotFound()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ accessDenied };
        req.canonicalFailures = new Exception[]{ accessDenied };
        req.assertFailureType = ItemNotFoundException.class;
        assertDownloadFromMirror( req );

    }

    @Test
    public void whenMirrorDeniesAccessAndCanonicalSucceedsThenShouldBlacklistTheMirror()
        throws Exception
    {
        AssertionRequest req;

        // mirror fails, but canonical succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ accessDenied };
        req.assertMirrorBlacklisted = true;
        req.canonicalSuccess = true;
        assertDownloadFromMirror( req );
    }

    @Test
    public void mirrorAndCanonicalBothHaveInvalidContent()
        throws Exception
    {
        M2Repository repository = createTestRepository( new Mirror[]{ MIRROR1, MIRROR2 } );
        MockRemoteStorage mockStorage = (MockRemoteStorage) repository.getRemoteStorage();

        String content = "";
        String path = "path";

        // canonical
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path, content );
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path + ".sha1", ITEM_BAD_SHA1_HASH );

        // mirror1
        mockStorage.getValidUrlContentMap().put( MIRROR1.getUrl() + path, content );

        ResourceStoreRequest req = new ResourceStoreRequest( "/" + path, false );

        try
        {
            repository.retrieveItem( req );
            Assert.fail( "expected ItemNotFoundException" );
        }
        catch ( ItemNotFoundException e )
        {
            // expected
        }

        // content
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, CANONICAL_URL ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR2.getUrl() ) ) );

        // hash
        ResourceStoreRequest hashReq = new ResourceStoreRequest( "/" + path + ".sha1" );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR2.getUrl() ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, CANONICAL_URL ) ) );

        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR1 ) );
        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR2 ) );
    }

    @Test
    public void mirrorInvalidContentCanonicalValidContent()
        throws Exception
    {
        M2Repository repository = createTestRepository( new Mirror[]{ MIRROR1, MIRROR2 } );
        MockRemoteStorage mockStorage = (MockRemoteStorage) repository.getRemoteStorage();

        String content = "";
        String path = "path";

        // canonical
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path, content );
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path + ".sha1", ITEM_SHA1_HASH );

        // mirror1
        mockStorage.getValidUrlContentMap().put( MIRROR1.getUrl() + ITEM_PATH, "invalid content" );

        ResourceStoreRequest req = new ResourceStoreRequest( "/" + path, false );

        Assert.assertNotNull( repository.retrieveItem( req ) );

        // content
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, CANONICAL_URL ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR2.getUrl() ) ) );

        // hash
        ResourceStoreRequest hashReq = new ResourceStoreRequest( "/" + path + ".sha1" );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR2.getUrl() ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, CANONICAL_URL ) ) );

        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR1 ) );
        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR2 ) );
    }

    @Test
    public void testMirrorDownCanonicalRetry()
        throws Exception
    {
        M2Repository repository = createTestRepository( new Mirror[]{ MIRROR1, MIRROR2 } );
        MockRemoteStorage mockStorage = (MockRemoteStorage) repository.getRemoteStorage();

        String content = "";
        String path = "path";

        // canonical
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path, content );
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path + ".sha1", ITEM_SHA1_HASH );
        // the canonical is down the first time we hit it
        mockStorage.getValueUrlFailConfigMap().put( CANONICAL_URL + path, 1 );

        // mirror1
        mockStorage.getDownUrls().add( MIRROR1.getUrl() );

        ResourceStoreRequest req = new ResourceStoreRequest( "/" + path, false );
        Assert.assertNotNull( repository.retrieveItem( req ) );

        // content
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, CANONICAL_URL ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR2.getUrl() ) ) );

        // hash
        ResourceStoreRequest hashReq = new ResourceStoreRequest( "/" + path + ".sha1" );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR2.getUrl() ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, CANONICAL_URL ) ) );

        Assert.assertTrue( repository.getDownloadMirrors().isBlacklisted( MIRROR1 ) );
        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR2 ) );
    }

    @Test
    public void testMirrorRetry()
        throws Exception
    {
        M2Repository repository = createTestRepository( new Mirror[]{ MIRROR1, MIRROR2 } );
        MockRemoteStorage mockStorage = (MockRemoteStorage) repository.getRemoteStorage();

        String content = "";
        String path = "path";

        // canonical
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path, content );
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path + ".sha1", ITEM_SHA1_HASH );

        // mirror1 is down the first time we hit it
        mockStorage.getValueUrlFailConfigMap().put( MIRROR1.getUrl() + path, 1 );
        mockStorage.getValidUrlContentMap().put( MIRROR1.getUrl() + path, content );

        ResourceStoreRequest req = new ResourceStoreRequest( "/" + path, false );
        Assert.assertNotNull( repository.retrieveItem( req ) );

        // content
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, MIRROR2.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, CANONICAL_URL ) ) );

        // hash
        ResourceStoreRequest hashReq = new ResourceStoreRequest( "/" + path + ".sha1" );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR1.getUrl() ) ) );
        Assert.assertFalse( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, MIRROR2.getUrl() ) ) );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, CANONICAL_URL ) ) );

        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR1 ) );
        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR2 ) );
    }

    @Test
    public void testInvalidChecksumCanonicalNoAutoBlock()
        throws Exception
    {
        M2Repository repository = createTestRepository( new Mirror[0] );
        MockRemoteStorage mockStorage = (MockRemoteStorage) repository.getRemoteStorage();

        String content = "";
        String path = "path";

        // canonical
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path, content );
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + path + ".sha1", ITEM_BAD_SHA1_HASH );

        // status ?
        mockStorage.getValidUrlContentMap().put( CANONICAL_URL + "status", content );

        ResourceStoreRequest req = new ResourceStoreRequest( "/" + path, false );
        try
        {
            repository.retrieveItem( req );
            Assert.fail( "Expected Exception" );
        }
        catch ( Exception e )
        {
            // expected
        }

        // content
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, req, CANONICAL_URL ) ) );

        // hash
        ResourceStoreRequest hashReq = new ResourceStoreRequest( "/" + path + ".sha1" );
        Assert.assertTrue( mockStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, hashReq, CANONICAL_URL ) ) );

        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR1 ) );
        Assert.assertFalse( repository.getDownloadMirrors().isBlacklisted( MIRROR2 ) );
        Assert.assertEquals( LocalStatus.IN_SERVICE, repository.getLocalStatus() );

        // checkAutoBlock
        Assert.assertEquals( "Repository should NOT be autoblocked", ProxyMode.ALLOW, repository.getProxyMode() );
    }

    private M2Repository createTestRepository( Mirror[] mirrors )
        throws Exception
    {
        M2Repository repository = this.createM2Repository( mirrors );

        // change the remote storage impl ( something we have control over
        String remoteUrl = repository.getRemoteUrl();
        MockRemoteStorage mockStorage = (MockRemoteStorage) this.lookup( RemoteRepositoryStorage.class, "mock" );
        repository.setRemoteUrl( remoteUrl );
        repository.setRemoteStorage( mockStorage );

        // localstorage needs to be something OTHER then mock
        LocalRepositoryStorage ls =
            this.lookup( LocalRepositoryStorage.class, DefaultFSLocalRepositoryStorage.PROVIDER_STRING );
        repository.setLocalStorage( ls );

        return repository;
    }

    @Test
    public void testGenericStorageException()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail (two retries each)
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ storageException, storageException };
        req.canonicalFailures = new Exception[]{ storageException, storageException };
        req.assertFailureType = ItemNotFoundException.class; // original InvalidItemContentException is swallowed
        req.assertMirrorBlacklisted = false;
        assertDownloadFromMirror( req );

        // mirror fails twice, but canonical succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ storageException, storageException };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );

        // mirror fails twice, canonical fails once, then succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ storageException, storageException };
        req.canonicalFailures = new Exception[]{ storageException };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );

        // mirror fails once, then succeeds
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ storageException };
        req.mirrorSuccess = true;
        req.assertMirrorBlacklisted = false;
        assertDownloadFromMirror( req );
    }

    @Test
    public void testRuntimeException()
        throws Exception
    {
        AssertionRequest req = new AssertionRequest();
        req.mirrorFailures = new Exception[]{ new RuntimeException( "testRuntimeException" ) };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );
    }

    // FIXME mocking is way too deep here and verification order is impossible
    private void assertDownloadFromMirror( AssertionRequest request )
        throws Exception
    {
        // also checks fallback to canonical after first mirror fails
        final M2Repository realM2Repo = createM2Repository( request.mirrors );
        realM2Repo.setChecksumPolicy( ChecksumPolicy.STRICT );
        realM2Repo.getCurrentCoreConfiguration().commitChanges();

        final RepositoryItemUid itemUid = realM2Repo.createUid( ITEM_PATH );
        final RepositoryItemUid itemHashUid = realM2Repo.createUid( itemUid.getPath() + ".sha1" );
        final RemoteRepositoryStorage mockRemoteStorage = mock( RemoteRepositoryStorage.class );
        doReturn( "mock" ).when( mockRemoteStorage ).getProviderId();

        for ( Exception exception : request.mirrorFailures )
        {
            if ( exception instanceof ItemNotFoundException )
            {
               doThrow( exception ).when( mockRemoteStorage ).retrieveItem( same( realM2Repo ),
                                                                             (ResourceStoreRequest) anyObject(),
                                                                             eq( request.mirrors[0].getUrl() ) );
               doReturn( createRealRemoteStorageFileItem( itemHashUid, ITEM_BAD_SHA1_HASH.getBytes() ) ).when(
                    mockRemoteStorage ).retrieveItem(
                    same( realM2Repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) );
            }
            else
            {
                doThrow( exception ).when( mockRemoteStorage ).retrieveItem( same( realM2Repo ),
                                                                             (ResourceStoreRequest) anyObject(),
                                                                             eq( request.mirrors[0].getUrl() ) );
            }
        }

        if ( request.mirrorSuccess )
        {
            doReturn( createRealRemoteStorageFileItem( itemUid, ITEM_CONTENT ) ).when( mockRemoteStorage ).retrieveItem(
                same( realM2Repo ),
                (ResourceStoreRequest) anyObject(),
                eq( request.mirrors[0].getUrl() ) );
            doReturn( createRealRemoteStorageFileItem( itemHashUid, ITEM_SHA1_HASH.getBytes() ) ).when(
                mockRemoteStorage ).retrieveItem(
                same( realM2Repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) );
        }

        for ( Exception exception : request.canonicalFailures )
        {
            if ( exception instanceof ItemNotFoundException )
            {
               doReturn( createRealRemoteStorageFileItem( itemHashUid, ITEM_BAD_SHA1_HASH.getBytes() ) ).when(
                    mockRemoteStorage ).retrieveItem(
                    same( realM2Repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) );
               doThrow( exception ).when( mockRemoteStorage ).retrieveItem( same( realM2Repo ),
                                                                             (ResourceStoreRequest) anyObject(),
                                                                             eq( CANONICAL_URL ) );
            }
            else
            {
                doThrow( exception ).when( mockRemoteStorage ).retrieveItem( same( realM2Repo ),
                                                                             (ResourceStoreRequest) anyObject(),
                                                                             eq( CANONICAL_URL ) );
            }
        }

        if ( request.canonicalSuccess )
        {
            doReturn( createRealRemoteStorageFileItem( itemUid, ITEM_CONTENT ) ).when( mockRemoteStorage ).retrieveItem(
                same( realM2Repo ),
                (ResourceStoreRequest) anyObject(),
                eq( CANONICAL_URL ) );
            doReturn( createRealRemoteStorageFileItem( itemHashUid, ITEM_SHA1_HASH.getBytes() ) ).when(
                mockRemoteStorage ).retrieveItem(
                same( realM2Repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) );
        }

        realM2Repo.setRemoteStorage( mockRemoteStorage );
        ResourceStoreRequest req = new ResourceStoreRequest( ITEM_PATH, false );

        try
        {
            StorageItem item = realM2Repo.retrieveItem( req );
            if ( request.assertFailureType != null )
            {
                fail( "Operation was expected to fail" );
            }
            assertNotNull( item );
        }
        catch ( Exception failure )
        {
            if ( request.assertFailureType == null )
            {
                throw failure;
            }
            assertEquals( request.assertFailureType, failure.getClass() );
        }
        assertEquals( request.assertMirrorBlacklisted, realM2Repo.getDownloadMirrors().isBlacklisted( MIRROR1 ) );

    }

    // ////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Create a real M2Repository from container, configured with the specified mirrors
     *
     * @param mirrors the mirrors to add in the specified order
     * @return configured repo
     * @throws Exception
     */
    private M2Repository createM2Repository( final Mirror[] mirrors )
        throws Exception
    {

        // FIXME should use mocks entirely instead imo
        M2Repository repo = (M2Repository) getContainer().lookup( Repository.class, "maven2" );

        // config
        CRepository repoConf = new DefaultCRepository();
        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( "repo" );

        // FIXME why are we setting this if we are setting mock local storage later
        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );

        repoConf.setRemoteStorage( new CRemoteStorage() );
        repoConf.getRemoteStorage().setProvider( remoteProviderHintFactory.getDefaultHttpRoleHint() );
        repoConf.getRemoteStorage().setUrl( CANONICAL_URL );
        repoConf.getRemoteStorage().setConnectionSettings( new CRemoteConnectionSettings() );
        repoConf.getRemoteStorage().getConnectionSettings().setRetrievalRetryCount( 2 );

        // add mirrors
        if ( mirrors != null )
        {
            List<CMirror> cmirrors = new ArrayList<CMirror>( mirrors.length );
            for ( Mirror mirror : mirrors )
            {
                CMirror cmirror = new CMirror();
                cmirror.setId( mirror.getId() );
                cmirror.setUrl( mirror.getUrl() );
                cmirrors.add( cmirror );
            }
            repoConf.getRemoteStorage().setMirrors( cmirrors );
        }

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );

        // FIXME ?????????
        M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

        repo.configure( repoConf );
        repo.getNotFoundCache().purge();

        // mock local repo
        final LocalRepositoryStorage mockLocalStorage = createMockEmptyLocalStorageRepository();
        repo.setLocalStorage( mockLocalStorage );
        return repo;
    }

    private AbstractStorageItem createRealRemoteStorageFileItem( RepositoryItemUid uid, byte[] bytes )
    {
        ContentLocator content = new ByteArrayContentLocator( bytes, getMimeUtil().getMimeType( uid.getPath() ) );
        DefaultStorageFileItem item =
            new DefaultStorageFileItem( uid.getRepository(), new ResourceStoreRequest( uid.getPath() ), true, false,
                                        content );
        if ( bytes.length == 0 )
        {
            item.getAttributes().put( "digest.sha1", ITEM_SHA1_HASH );
        }
        return item;
    }

    private LocalRepositoryStorage createMockEmptyLocalStorageRepository()
        throws LocalStorageException, ItemNotFoundException, UnsupportedStorageOperationException
    {
        LocalRepositoryStorage localRepositoryStorage = mock( LocalRepositoryStorage.class );
        doReturn( "dummy" ).when( localRepositoryStorage ).getProviderId();
        doReturn( false ).when( localRepositoryStorage ).containsItem( any( Repository.class ),
                                                                       any( ResourceStoreRequest.class ) );
        doThrow( itemNotFound ).when( localRepositoryStorage ).retrieveItem( any( Repository.class ),
                                                                             any( ResourceStoreRequest.class ) );
        doThrow( itemNotFound ).when( localRepositoryStorage ).deleteItem( any( Repository.class ),
                                                                           any( ResourceStoreRequest.class ) );
        return localRepositoryStorage;
    }

}
