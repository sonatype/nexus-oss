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

import static org.easymock.EasyMock.createMock;

import org.easymock.EasyMock;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

public abstract class AbstractStorageItemTest
    extends AbstractNexusTestEnvironment
{
    protected Repository repository;

    protected RepositoryRouter router;

    public void setUp()
        throws Exception
    {
        super.setUp();

        repository = EasyMock.createNiceMock( Repository.class );

        router = createMock( RepositoryRouter.class );
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
            assertEquals( repository.getId(), item.getRepositoryId() );
            assertEquals( repository.getId(), ( (Repository) item.getStore() ).getId() );
        }
        else
        {
            assertEquals( null, item.getRepositoryId() );
            // router is only one from now on and has no ID
            // assertEquals( router.getId(), item.getStore().getId() );
        }

        // path
        assertEquals( shouldBeName, item.getName() );
        assertEquals( shouldBePath, item.getPath() );
        assertEquals( shouldBeParentPath, item.getParentPath() );
    }

    public void testDummy()
    {
        assertEquals( "a", "a" );
    }

}
