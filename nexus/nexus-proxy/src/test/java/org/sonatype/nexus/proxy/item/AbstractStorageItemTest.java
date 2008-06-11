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

import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.repository.DummyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.DummyRepositoryRouter;

public class AbstractStorageItemTest
    extends AbstractNexusTestEnvironment
{

    protected DummyRepository repository = new DummyRepository();

    protected DummyRepositoryRouter router = new DummyRepositoryRouter();

    public DummyRepository getRepository()
    {
        return repository;
    }

    public DummyRepositoryRouter getRouter()
    {
        return router;
    }

    public void checkAbstractStorageItem( ResourceStore store, AbstractStorageItem item, boolean isVirtual,
        String shouldBeName, String shouldBePath, String shouldBeParentPath )
    {
        // it is backed by repo
        assertEquals( isVirtual, item.isVirtual() );
        assertEquals( !isVirtual, item.getRepositoryItemUid() != null );

        // repo should be eq
        assertEquals( store, item.getStore() );

        if ( Repository.class.isAssignableFrom( store.getClass() ) )
        {
            // repo stuff eq
            assertEquals( getRepository().getId(), item.getRepositoryId() );
            assertEquals( getRepository().getId(), item.getStore().getId() );
        }
        else
        {
            assertEquals( null, item.getRepositoryId() );
            assertEquals( getRouter().getId(), item.getStore().getId() );
        }

        // path
        assertEquals( shouldBeName, item.getName() );
        assertEquals( shouldBePath, item.getPath() );
        assertEquals( shouldBeParentPath, item.getParentPath() );
    }
    
    public void testDummy() {
        assertEquals( "a", "a" );
    }

}
