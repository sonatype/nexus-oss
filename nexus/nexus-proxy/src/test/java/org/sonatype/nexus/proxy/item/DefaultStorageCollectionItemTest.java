/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.item;

import java.util.Collection;


public class DefaultStorageCollectionItemTest
    extends AbstractStorageItemTest
{

    public void testNonVirtualCollectionSimple()
    {
        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( getRepository(), "/", true, true );
        checkAbstractStorageItem( getRepository(), coll, false, "", "/", "/" );
    }

    public void testNonVirtualCollectionList()
    {
        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( getRepository(), "/a/some/dir/coll", true, true );
        checkAbstractStorageItem( getRepository(), coll, false, "coll", "/a/some/dir/coll", "/a/some/dir" );
        
        Collection<StorageItem> items= coll.list();
        assertEquals( 3, items.size() );
    }

    public void testVirtualCollectionSimple()
    {
        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( getRouter(), "/", true, true );
        checkAbstractStorageItem( getRouter(), coll, true, "", "/", "/" );
    }

    public void testVirtualCollectionList()
    {
        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( getRouter(), "/and/another/coll", true, true );
        checkAbstractStorageItem( getRouter(), coll, true, "coll", "/and/another/coll", "/and/another" );

        Collection<StorageItem> items= coll.list();
        assertEquals( 2, items.size() );
    }

}
