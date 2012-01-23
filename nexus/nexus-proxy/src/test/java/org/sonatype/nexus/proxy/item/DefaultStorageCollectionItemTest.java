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
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.easymock.IAnswer;
import org.junit.Test;
import org.sonatype.nexus.proxy.ResourceStoreRequest;

public class DefaultStorageCollectionItemTest
    extends AbstractStorageItemTest
{
    @Test
    public void testNonVirtualCollectionSimple()
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/" ) ).andAnswer( new IAnswer<RepositoryItemUid>()
        {
            @Override
            public RepositoryItemUid answer()
                throws Throwable
            {
                return getRepositoryItemUidFactory().createUid( repository, "/" );
            }
        } );

        replay( repository );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( repository, "/", true, true );
        checkAbstractStorageItem( repository, coll, false, "", "/", "/" );
    }

    @Test
    public void testNonVirtualCollectionList()
        throws Exception
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/a/some/dir/coll" ) ).andAnswer( new IAnswer<RepositoryItemUid>()
        {
            @Override
            public RepositoryItemUid answer()
                throws Throwable
            {
                return getRepositoryItemUidFactory().createUid( repository, "/a/some/dir/coll" );
            }
        } );
        expect( repository.createUid( "/a/some/dir/coll/A" ) ).andAnswer( new IAnswer<RepositoryItemUid>()
        {
            @Override
            public RepositoryItemUid answer()
                throws Throwable
            {
                return getRepositoryItemUidFactory().createUid( repository, "/a/some/dir/coll/A" );
            }
        } );
        expect( repository.createUid( "/a/some/dir/coll/B" ) ).andAnswer( new IAnswer<RepositoryItemUid>()
        {
            @Override
            public RepositoryItemUid answer()
                throws Throwable
            {
                return getRepositoryItemUidFactory().createUid( repository, "/a/some/dir/coll/B" );
            }
        } );
        expect( repository.createUid( "/a/some/dir/coll/C" ) ).andAnswer( new IAnswer<RepositoryItemUid>()
        {
            @Override
            public RepositoryItemUid answer()
                throws Throwable
            {
                return getRepositoryItemUidFactory().createUid( repository, "/a/some/dir/coll/C" );
            }
        } );
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

    @Test
    public void testVirtualCollectionSimple()
    {
        replay( router );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( router, "/", true, true );
        checkAbstractStorageItem( router, coll, true, "", "/", "/" );
    }

    @Test
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
