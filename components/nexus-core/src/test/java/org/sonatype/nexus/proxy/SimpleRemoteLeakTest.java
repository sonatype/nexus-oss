/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.apache.commons.httpclient.CustomMultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

/**
 * Httpclient caches connection in a connection pool. This test ensures we are using HttpClient API in correct way,
 * and that the pool does not introduce a "leak" (ie. after a method is executed, the connection is closed and returned
 * to pool). This test enforces real HTTP transport to happen multiple times, and checks for pool elements, there should
 * be no more than 1 connection. Naturally, this test deeply depends on HttpClient 3.x API, so it will work only if
 * RemoteRepositoryStorage is CommonsHttpClientRemoteStorage. The test is repeated twice, once with "good" transport
 * and once with "bad" transport (by setting hostname intentionally to a unknown one).
 */
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

    @Test
    public void checkForConnectionPoolLeakWithGoodTransport()
        throws Exception
    {
        ProxyRepository repo1 = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );
        ProxyRepository repo2 = getRepositoryRegistry().getRepositoryWithFacet( "repo2", ProxyRepository.class );

        // this test is CommonsHttpclient3x dependent, it uses it's internals in assertions!
        // So run it only when that RRS is in use
        if ( !( repo1.getRemoteStorage() instanceof CommonsHttpClientRemoteStorage ) )
        {
            System.out.println( "Test disabled, RRS implementation is not of class "
                                    + CommonsHttpClientRemoteStorage.class.getName() + " but "
                                    + repo1.getRemoteStorage().getClass().getName() );

            return;
        }

        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it succeed! (127.0.0.1 is localhost, so will be able to connect)
        repo1.setRemoteUrl(
            getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class ).getRemoteUrl().replace(
                "localhost", "127.0.0.1" ) );
        repo1.commitChanges();

        ResourceStoreRequest req1 =
            new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false );
        ResourceStoreRequest req2 =
            new ResourceStoreRequest( "/repositories/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false );

        for ( int i = 0; i < 10; i++ )
        {
            StorageItem item1 = getRootRouter().retrieveItem( req1 );
            checkForFileAndMatchContents( item1 );

            StorageItem item2 = getRootRouter().retrieveItem( req2 );
            checkForFileAndMatchContents( item2 );

            // to force refetch
            getRepositoryRegistry().getRepository( item1.getRepositoryId() ).deleteItem( false,
                                                                                         new ResourceStoreRequest(
                                                                                             item1 ) );

            getRepositoryRegistry().getRepository( item2.getRepositoryId() ).deleteItem( false,
                                                                                         new ResourceStoreRequest(
                                                                                             item2 ) );
        }

        // get the default context, since they used it
        RemoteStorageContext ctx1 = repo1.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm1 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx1.getContextObject(
                CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) ).getHttpConnectionManager();

        MatcherAssert.assertThat( cm1.getConnectionsInPool(), is( equalTo( 1 ) ) );

        RemoteStorageContext ctx2 = repo2.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm2 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx2.getContextObject(
                CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) ).getHttpConnectionManager();

        MatcherAssert.assertThat( cm2.getConnectionsInPool(), is( equalTo( 1 ) ) );
    }


    @Test
    public void checkForConnectionPoolLeakWithFailingTransport()
        throws Exception
    {
        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it fail! (unknown host, so will not be able to connect)
        getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class ).setRemoteUrl(
            getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class ).getRemoteUrl().replace(
                "localhost", "1.1.1.1" ) );

        ProxyRepository repo1 = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );
        ProxyRepository repo2 = getRepositoryRegistry().getRepositoryWithFacet( "repo2", ProxyRepository.class );

        // this test is CommonsHttpclient3x dependent, it uses it's internals in assertions!
        // So run it only when that RRS is in use
        if ( !( repo1.getRemoteStorage() instanceof CommonsHttpClientRemoteStorage ) )
        {
            System.out.println( "Test disabled, RRS implementation is not of class "
                                    + CommonsHttpClientRemoteStorage.class.getName() + " but "
                                    + repo1.getRemoteStorage().getClass().getName() );

            return;
        }

        // loop until we have some "sensible" result (not unknown, since this is async op)
        // first unforced request will trigger the check, and wait until we have result
        RemoteStatus rs1 = RemoteStatus.UNKNOWN;
        RemoteStatus rs2 = RemoteStatus.UNKNOWN;

        while ( RemoteStatus.UNKNOWN.equals( rs1 ) || RemoteStatus.UNKNOWN.equals( rs2 ) )
        {
            rs1 = repo1.getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), false );
            rs2 = repo2.getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), false );

            Thread.sleep( 1000 );
        }

        // get the default context, since they used it
        RemoteStorageContext ctx1 = repo1.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm1 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx1.getContextObject(
                CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) ).getHttpConnectionManager();

        MatcherAssert.assertThat( cm1.getConnectionsInPool(), is( equalTo( 1 ) ) );

        RemoteStorageContext ctx2 = repo2.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm2 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx2.getContextObject(
                CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) ).getHttpConnectionManager();

        MatcherAssert.assertThat( cm2.getConnectionsInPool(), is( equalTo( 1 ) ) );
    }
}
