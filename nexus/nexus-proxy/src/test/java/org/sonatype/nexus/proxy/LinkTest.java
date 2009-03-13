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
package org.sonatype.nexus.proxy;

import java.io.ByteArrayInputStream;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryRequest;

public class LinkTest
    extends AbstractProxyTestEnvironment
{
    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testRepoLinks()
        throws Exception
    {
        String contentString = "SOME_CONTENT";

        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repo1,
            "/a.txt",
            true,
            true,
            new StringContentLocator( contentString ) );
        file.setMimeType( "plain/text" );
        file.getAttributes().put( "attr1", "ATTR1" );
        repo1.storeItem( file );

        DefaultStorageLinkItem link = new DefaultStorageLinkItem( repo1, "/b.txt", true, true, file
            .getRepositoryItemUid() );
        repo1.getLocalStorage().storeItem( repo1, link );

        StorageItem item = repo1.retrieveItem( new ResourceStoreRequest( "/b.txt", true ) );
        assertEquals( DefaultStorageLinkItem.class, item.getClass() );

        StorageFileItem item1 = (StorageFileItem) repo1.retrieveItem( new RepositoryRequest( ( (StorageLinkItem) item )
            .getTarget().getRepository(), new ResourceStoreRequest(
            ( (StorageLinkItem) item ).getTarget().getPath(),
            false ) ) );

        assertStorageFileItem( item1 );
        assertTrue( contentEquals( item1.getInputStream(), new ByteArrayInputStream( contentString.getBytes() ) ) );
    }

}
