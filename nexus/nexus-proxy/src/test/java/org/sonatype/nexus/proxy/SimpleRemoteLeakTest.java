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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

public class SimpleRemoteLeakTest
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

    public void testSimplerRemoteLeak()
        throws Exception
    {

        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        getRepositoryRegistry().getRepository( "repo1" ).setRemoteUrl(
            getRepositoryRegistry().getRepository( "repo1" ).getRemoteUrl().replace( "localhost", "127.0.0.1" ) );

        ResourceStoreRequest req1 = new ResourceStoreRequest(
            "/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar",
            false );
        ResourceStoreRequest req2 = new ResourceStoreRequest( "/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false );

        for ( int i = 0; i < 10; i++ )
        {
            StorageItem item1 = getRouter( "repositories" ).retrieveItem( req1 );
            checkForFileAndMatchContents( item1 );

            StorageItem item2 = getRouter( "repositories" ).retrieveItem( req2 );
            checkForFileAndMatchContents( item2 );

            // to force refetch
            getRepositoryRegistry().getRepository( item1.getRepositoryId() ).deleteItem( item1.getRepositoryItemUid() );
            getRepositoryRegistry().getRepository( item2.getRepositoryId() ).deleteItem( item2.getRepositoryItemUid() );
        }

        // get the default context, since they used it
        RemoteStorageContext ctx = getRemoteStorageContext();

        MultiThreadedHttpConnectionManager cm = (MultiThreadedHttpConnectionManager) ( (HttpClient) ctx
            .getRemoteConnectionContext().get( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
            .getHttpConnectionManager();
        assertEquals( 2, cm.getConnectionsInPool() );

    }
}
