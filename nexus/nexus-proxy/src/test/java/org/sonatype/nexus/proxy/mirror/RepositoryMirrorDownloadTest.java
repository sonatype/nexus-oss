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
package org.sonatype.nexus.proxy.mirror;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;

import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.InvalidItemContentException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RepositoryMirrorDownloadTest
    extends AbstractNexusTestEnvironment
{

    private static final String ITEM_PATH = "/path";

    private static final Mirror MIRROR1 = new Mirror( "1", "mirror1-url" );

    private static final Mirror MIRROR2 = new Mirror( "2", "mirror2-url" );

    private static final String CANONICAL_URL = "canonical-url";

    private static final ItemNotFoundException itemNotFount = new ItemNotFoundException( ITEM_PATH );

    private static final StorageException storageException = new StorageException( ITEM_PATH );

    private static final byte[] ITEM_CONTENT = new byte[0];

    private static final String ITEM_SHA1_HASH = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

    private static final String ITEM_BAD_SHA1_HASH = "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE";

    private static final RemoteAccessException accessDenied = new RemoteAccessException( null, ITEM_PATH )
    {

    };

    private static final InvalidItemContentException invalidContent = new InvalidItemContentException( ITEM_PATH, null );

    // this is crazy...
    private static class AssertionRequest
    {
        /**
         * Mirrors
         */
        public Mirror[] mirrors = new Mirror[] { MIRROR1, MIRROR2 };

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
    }

    public void testDownloadFromMirror()
        throws Exception
    {
        M2Repository repo = createM2Repository( new Mirror[] { MIRROR1 } );

        RepositoryItemUid uid = repo.createUid( ITEM_PATH );

        RemoteRepositoryStorage rs = createMock( RemoteRepositoryStorage.class );
        expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( MIRROR1.getUrl() ) ) )
            .andReturn( newRemoteStorageFileItem( uid, ITEM_CONTENT ) );

        repo.setRemoteStorage( rs );

        replay( rs );

        ResourceStoreRequest req = new ResourceStoreRequest( ITEM_PATH, false );
        repo.retrieveItem( req );
    }

    public void testItemNotFound()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { itemNotFount };
        req.canonicalFailures = new Exception[] { itemNotFount };
        req.assertFailureType = ItemNotFoundException.class;
        assertDownloadFromMirror( req );

        // mirror fails, but canonical succeeds => not blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { itemNotFount };
        req.canonicalSuccess = true;
        assertDownloadFromMirror( req );
    }

    public void testAccessDenied()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { accessDenied };
        req.canonicalFailures = new Exception[] { accessDenied };
        req.assertFailureType = ItemNotFoundException.class;
        assertDownloadFromMirror( req );

        // mirror fails, but canonical succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { accessDenied };
        req.assertMirrorBlacklisted = true;
        req.canonicalSuccess = true;
        assertDownloadFromMirror( req );
    }

    public void testInvalidContent()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail (two retries each)
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { invalidContent, invalidContent };
        req.canonicalFailures = new Exception[] { invalidContent, invalidContent };
        req.assertFailureType = ItemNotFoundException.class; // original InvalidItemContentException is swallowed
        req.assertMirrorBlacklisted = false;
        assertDownloadFromMirror( req );

        // mirror fails twice, but canonical succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { invalidContent, invalidContent };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );

        // mirror fails twice, canonical fails once, then succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { invalidContent, invalidContent };
        req.canonicalFailures = new Exception[] { invalidContent };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );

        // mirror fails once, then succeeds
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { invalidContent };
        req.mirrorSuccess = true;
        req.assertMirrorBlacklisted = false;
        assertDownloadFromMirror( req );
    }

    public void testGenericStorageException()
        throws Exception
    {
        AssertionRequest req;

        // both mirror and canonical fail (two retries each)
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { storageException, storageException };
        req.canonicalFailures = new Exception[] { storageException, storageException };
        req.assertFailureType = ItemNotFoundException.class; // original InvalidItemContentException is swallowed
        req.assertMirrorBlacklisted = false;
        assertDownloadFromMirror( req );

        // mirror fails twice, but canonical succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { storageException, storageException };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );

        // mirror fails twice, canonical fails once, then succeeds => blacklisted
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { storageException, storageException };
        req.canonicalFailures = new Exception[] { storageException };
        req.canonicalSuccess = true;
        req.assertMirrorBlacklisted = true;
        assertDownloadFromMirror( req );

        // mirror fails once, then succeeds
        req = new AssertionRequest();
        req.mirrorFailures = new Exception[] { storageException };
        req.mirrorSuccess = true;
        req.assertMirrorBlacklisted = false;
        assertDownloadFromMirror( req );
    }

    private void assertDownloadFromMirror( AssertionRequest request )
        throws Exception
    {
        // also checks fallback to canonical after first mirror fails
        M2Repository repo = createM2Repository( request.mirrors );

        repo.setChecksumPolicy( ChecksumPolicy.STRICT );

        RepositoryItemUid uid = repo.createUid( ITEM_PATH );

        RepositoryItemUid hashUid = repo.createUid( uid.getPath() + ".sha1" );

        RemoteRepositoryStorage rs = createMock( RemoteRepositoryStorage.class );

        checkOrder( rs, true );

        for ( Exception exception : request.mirrorFailures )
        {
            if ( exception instanceof InvalidItemContentException )
            {
                expect(
                    rs
                        .retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( request.mirrors[0]
                            .getUrl() ) ) ).andReturn( newRemoteStorageFileItem( uid, ITEM_CONTENT ) );

                expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                    .andReturn( newRemoteStorageFileItem( hashUid, ITEM_BAD_SHA1_HASH.getBytes() ) );
            }
            else
            {
                expect(
                    rs
                        .retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( request.mirrors[0]
                            .getUrl() ) ) ).andThrow( exception );
            }
        }

        if ( request.mirrorSuccess )
        {
            expect(
                rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( request.mirrors[0].getUrl() ) ) )
                .andReturn( newRemoteStorageFileItem( uid, ITEM_CONTENT ) );

            expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                .andReturn( newRemoteStorageFileItem( hashUid, ITEM_SHA1_HASH.getBytes() ) );
        }

        for ( Exception exception : request.canonicalFailures )
        {
            if ( exception instanceof InvalidItemContentException )
            {
                expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                    .andReturn( newRemoteStorageFileItem( uid, ITEM_CONTENT ) );

                expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                    .andReturn( newRemoteStorageFileItem( hashUid, ITEM_BAD_SHA1_HASH.getBytes() ) );

            }
            else
            {
                expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                    .andThrow( exception );
            }
        }

        if ( request.canonicalSuccess )
        {
            expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                .andReturn( newRemoteStorageFileItem( uid, ITEM_CONTENT ) );

            expect( rs.retrieveItem( same( repo ), (ResourceStoreRequest) anyObject(), eq( CANONICAL_URL ) ) )
                .andReturn( newRemoteStorageFileItem( hashUid, ITEM_SHA1_HASH.getBytes() ) );
        }

        repo.setRemoteStorage( rs );

        replay( rs );

        ResourceStoreRequest req = new ResourceStoreRequest( ITEM_PATH, false );

        try
        {
            StorageItem item = repo.retrieveItem( req );

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

        assertEquals( request.assertMirrorBlacklisted, repo.getDownloadMirrors().isBlacklisted( MIRROR1 ) );
    }

    // ////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////

    private M2Repository createM2Repository( Mirror[] mirrors )
        throws Exception
    {
        M2Repository repo = (M2Repository) getContainer().lookup( Repository.class, "maven2" );

        repo.setId( "repo" );

        repo.getNotFoundCache().purge();

        LocalRepositoryStorage ls = createMockEmptyLocalStorage();
        repo.setLocalStorage( ls );

        repo.setRemoteStorageContext( new DefaultRemoteStorageContext( null ) );
        CRemoteConnectionSettings remoteConnectionSettings = (CRemoteConnectionSettings) repo
            .getRemoteStorageContext().getRemoteConnectionContextObject(
                RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS );
        remoteConnectionSettings.setRetrievalRetryCount( 2 );

        repo.setRemoteUrl( CANONICAL_URL );

        repo.getDownloadMirrors().setMirrors( Arrays.asList( mirrors ) );
        return repo;
    }

    private AbstractStorageItem newRemoteStorageFileItem( RepositoryItemUid uid, byte[] bytes )
    {
        ContentLocator content = new ByteArrayContentLocator( bytes );
        DefaultStorageFileItem item = new DefaultStorageFileItem( uid.getRepository(), new ResourceStoreRequest( uid
            .getPath() ), true, false, content );
        if ( bytes.length == 0 )
        {
            item.getAttributes().put( "digest.sha1", ITEM_SHA1_HASH );
        }
        return item;
    }

    private LocalRepositoryStorage createMockEmptyLocalStorage()
        throws ItemNotFoundException,
            StorageException,
            UnsupportedStorageOperationException
    {
        LocalRepositoryStorage ls = createMock( LocalRepositoryStorage.class );

        expect( ls.retrieveItem( (Repository) anyObject(), (ResourceStoreRequest) anyObject() ) ).andThrow(
            itemNotFount ).anyTimes();

        ls.deleteItem( (Repository) anyObject(), (ResourceStoreRequest) anyObject() );
        expectLastCall().andThrow( itemNotFount ).anyTimes();

        ls.storeItem( (Repository) anyObject(), (StorageItem) anyObject() );
        expectLastCall().anyTimes();

        replay( ls );

        return ls;
    }
}
