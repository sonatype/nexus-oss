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
