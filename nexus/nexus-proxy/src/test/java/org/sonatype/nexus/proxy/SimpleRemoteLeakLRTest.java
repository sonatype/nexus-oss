/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.apache.commons.httpclient.CustomMultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

public class SimpleRemoteLeakLRTest
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
    public void simplerAvailabilityCheckRemoteLeak()
        throws Exception
    {
        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it fail! (unknown host, so will not be able to connect)
        getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class ).setRemoteUrl(
            getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class ).getRemoteUrl().replace(
                "localhost", "1.1.1.1" ) );

        ProxyRepository repo1 = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );
        ProxyRepository repo2 = getRepositoryRegistry().getRepositoryWithFacet( "repo2", ProxyRepository.class );

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
