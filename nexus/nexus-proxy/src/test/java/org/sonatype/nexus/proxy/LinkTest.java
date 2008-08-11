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
package org.sonatype.nexus.proxy;

import java.io.ByteArrayInputStream;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;

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
            new ByteArrayInputStream( contentString.getBytes() ) );
        file.setMimeType( "plain/text" );
        file.getAttributes().put( "attr1", "ATTR1" );
        repo1.storeItem( file );

        DefaultStorageLinkItem link = new DefaultStorageLinkItem( repo1, "/b.txt", true, true, file
            .getRepositoryItemUid() );
        repo1.getLocalStorage().storeItem( link );

        StorageItem item = repo1.retrieveItem( new ResourceStoreRequest( "/b.txt", true ) );
        assertEquals( DefaultStorageLinkItem.class, item.getClass() );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(
            ( (StorageLinkItem) item ).getTarget().getRepository(),
            ( (StorageLinkItem) item ).getTarget().getPath() );

        StorageFileItem item1 = (StorageFileItem) repo1.retrieveItem( true, uid, null );
        checkForFileAndMatchContents( item1, new ByteArrayInputStream( contentString.getBytes() ) );

    }

}
