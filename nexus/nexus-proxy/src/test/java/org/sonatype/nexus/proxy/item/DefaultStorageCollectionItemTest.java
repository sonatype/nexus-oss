/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.anyBoolean;
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
        expect( repository.createUid( "/" ) ).andReturn( new DefaultRepositoryItemUid( null,  repository, "/" ) );

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
            new DefaultRepositoryItemUid( null, repository, "/a/some/dir/coll" ) );
        expect( repository.createUid( "/a/some/dir/coll/A" ) ).andReturn(
            new DefaultRepositoryItemUid( null, repository, "/a/some/dir/coll/A" ) );
        expect( repository.createUid( "/a/some/dir/coll/B" ) ).andReturn(
            new DefaultRepositoryItemUid( null, repository, "/a/some/dir/coll/B" ) );
        expect( repository.createUid( "/a/some/dir/coll/C" ) ).andReturn(
            new DefaultRepositoryItemUid( null, repository, "/a/some/dir/coll/C" ) );
        expect( repository.list( anyBoolean(), isA( StorageCollectionItem.class ) ) ).andReturn( result );

        replay( repository );

        // and now fill in result, since repo is active
        result.add( new DefaultStorageFileItem( repository, "/a/some/dir/coll/A", true, true, new StringContentLocator(
            "A" ) ) );
        result.add( new DefaultStorageFileItem( repository, "/a/some/dir/coll/B", true, true, new StringContentLocator(
            "B" ) ) );
        result.add( new DefaultStorageFileItem( repository, "/a/some/dir/coll/C", true, true, new StringContentLocator(
            "C" ) ) );

        DefaultStorageCollectionItem coll =
            new DefaultStorageCollectionItem( repository, "/a/some/dir/coll", true, true );
        checkAbstractStorageItem( repository, coll, false, "coll", "/a/some/dir/coll", "/a/some/dir" );

        Collection<StorageItem> items = coll.list();
        assertEquals( 3, items.size() );
    }

    public void testVirtualCollectionSimple()
    {
        replay( router );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( router, "/", true, true );
        checkAbstractStorageItem( router, coll, true, "", "/", "/" );
    }

    public void testVirtualCollectionList()
        throws Exception
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        expect( router.list( isA( ResourceStoreRequest.class ) ) ).andReturn( result );

        replay( router );

        // and now fill in result, since repo is active
        result.add( new DefaultStorageFileItem( router, "/a/some/dir/coll/A", true, true,
            new StringContentLocator( "A" ) ) );
        result.add( new DefaultStorageFileItem( router, "/a/some/dir/coll/B", true, true,
            new StringContentLocator( "B" ) ) );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( router, "/and/another/coll", true, true );
        checkAbstractStorageItem( router, coll, true, "coll", "/and/another/coll", "/and/another" );

        Collection<StorageItem> items = coll.list();
        assertEquals( 2, items.size() );
    }

}
