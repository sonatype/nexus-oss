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

import java.io.FileInputStream;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryRedeployTest
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

    protected Repository getRepository()
        throws NoSuchResourceStoreException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        repo1.setAllowWrite( true );

        return repo1;
    }

    public void retrieveItem()
        throws Exception
    {
        StorageItem item = getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );

        checkForFileAndMatchContents( item );
    }

    public void testSimple()
        throws Exception
    {
        // get some initial proxied content
        retrieveItem();

        StorageFileItem item = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", true ) );

        ResourceStoreRequest to = new ResourceStoreRequest(
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy",
            true );

        getRepository().storeItem( to, item.getInputStream(), null );

        // and repeat all this
        item = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", true ) );

        to = new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy", true );

        getRepository().storeItem( to, item.getInputStream(), null );

        StorageFileItem dest = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy", true ) );

        checkForFileAndMatchContents( dest, new FileInputStream( getRemoteFile( getRepositoryRegistry().getRepository(
            "repo1" ), "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ) ) );

    }

}
