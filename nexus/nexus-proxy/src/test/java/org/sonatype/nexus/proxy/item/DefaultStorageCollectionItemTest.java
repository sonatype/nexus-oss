/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sonatype.nexus.proxy.ResourceStoreRequest;

public class DefaultStorageCollectionItemTest
    extends AbstractStorageItemTest
{
    public void testNonVirtualCollectionSimple()
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/" ) );

        replay( repository );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( repository, "/", true, true );
        checkAbstractStorageItem( repository, coll, false, "", "/", "/" );
    }

    public void testNonVirtualCollectionList()
        throws Exception
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/a/some/dir/coll" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/a/some/dir/coll" ) );
        expect( repository.createUid( "/a/some/dir/coll/A" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/a/some/dir/coll/A" ) );
        expect( repository.createUid( "/a/some/dir/coll/B" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/a/some/dir/coll/B" ) );
        expect( repository.createUid( "/a/some/dir/coll/C" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/a/some/dir/coll/C" ) );
        expect( repository.list( isA( ResourceStoreRequest.class ) ) ).andReturn( result );

        replay( repository );

        // and now fill in result, since repo is active
        result.add( new DefaultStorageFileItem( repository, "/a/some/dir/coll/A", true, true ) );
        result.add( new DefaultStorageFileItem( repository, "/a/some/dir/coll/B", true, true ) );
        result.add( new DefaultStorageFileItem( repository, "/a/some/dir/coll/C", true, true ) );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
            repository,
            "/a/some/dir/coll",
            true,
            true );
        checkAbstractStorageItem( repository, coll, false, "coll", "/a/some/dir/coll", "/a/some/dir" );

        Collection<StorageItem> items = coll.list();
        assertEquals( 3, items.size() );
    }

    public void testVirtualCollectionSimple()
    {
        expect( router.getId() ).andReturn( "dummyRouter" ).anyTimes();

        replay( router );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( router, "/", true, true );
        checkAbstractStorageItem( router, coll, true, "", "/", "/" );
    }

    public void testVirtualCollectionList()
        throws Exception
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        expect( router.getId() ).andReturn( "dummyRouter" ).anyTimes();
        expect( router.list( isA( ResourceStoreRequest.class ) ) ).andReturn( result );

        replay( router );

        // and now fill in result, since repo is active
        result.add( new DefaultStorageFileItem(
            router,
            "/a/some/dir/coll/A",
            true,
            true,
            new StringContentLocator( "A" ) ) );
        result.add( new DefaultStorageFileItem(
            router,
            "/a/some/dir/coll/B",
            true,
            true,
            new StringContentLocator( "B" ) ) );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( router, "/and/another/coll", true, true );
        checkAbstractStorageItem( router, coll, true, "coll", "/and/another/coll", "/and/another" );

        Collection<StorageItem> items = coll.list();
        assertEquals( 2, items.size() );
    }

}
