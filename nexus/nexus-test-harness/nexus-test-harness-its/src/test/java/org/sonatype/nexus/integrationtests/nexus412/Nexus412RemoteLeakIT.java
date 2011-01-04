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
package org.sonatype.nexus.integrationtests.nexus412;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus412RemoteLeakIT
    extends AbstractNexusIntegrationTest
{
    // TODO: This IT is not an IT and will newer work as such. This is actually an UT but was moved from there to ITs.
    // As long as Nexus and IT runs in separate container, this IT will NEVER work. This is UT!

    // DISABLED: move to IT, it takes too long (no route to host + java)
    @Test
    public void nonTestSimplerAvailabilityCheckRemoteLeak()
        throws Exception
    {
        if ( true )
        {
            // this should be an UT
            printKnownErrorButDoNotFail( Nexus412RemoteLeakIT.class, "nonTestSimplerAvailabilityCheckRemoteLeak" );
            return;
        }

        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it fail! (unknown host, so will not be able to connect)

        ProxyRepository repo1 = this.convertRepo( "release-proxy-repo-1" );
        repo1.setRemoteUrl( repo1.getRemoteUrl().replace( "localhost", "1.1.1.1" ) );

        ProxyRepository repo2 = this.convertRepo( "tasks-snapshot-repo" );

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
        RemoteStorageContext ctx = new DefaultRemoteStorageContext( null );

        MultiThreadedHttpConnectionManager cm =
            (MultiThreadedHttpConnectionManager) ( (HttpClient) ctx
                .getContextObject( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
                .getHttpConnectionManager();
        Assert.assertEquals( 2, cm.getConnectionsInPool() );

    }

    private ProxyRepository convertRepo( String repoId )
        throws Exception
    {

        ProxyRepository repo = (ProxyRepository) lookup( Repository.class, "maven2" );

        CRepository cRepo = getNexusConfigUtil().getRepo( repoId );
        M2RepositoryConfiguration cM2Repo = getNexusConfigUtil().getM2Repo( repoId );

        repo.setId( cRepo.getId() );
        repo.setItemMaxAge( cM2Repo.getArtifactMaxAge() );
        // cRepo.getChecksumPolicy() );

        repo.setLocalStatus( LocalStatus.valueOf( cRepo.getLocalStatus() ) );

        if ( cRepo.getLocalStorage() != null )
        {
            repo.setLocalUrl( cRepo.getLocalStorage().getUrl() );
        }

        // repo.set cRepo.getMetadataMaxAge() );
        repo.setName( cRepo.getName() );
        repo.setNotFoundCacheTimeToLive( cRepo.getNotFoundCacheTTL() );
        repo.setProxyMode( cM2Repo.getProxyMode() );
        repo.setRemoteUrl( cRepo.getRemoteStorage().getUrl() );
        // cRepo.getRepositoryPolicy() );

        return repo;
    }

}
