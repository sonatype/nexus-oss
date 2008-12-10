/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus412;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.junit.Test;
import org.junit.Before;
import org.sonatype.nexus.configuration.RepositoryStatusConverter;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;
import org.sonatype.nexus.test.utils.NexusConfigUtil;

public class Nexus412RemoteLeakTest
    extends AbstractNexusIntegrationTest
{
    private RepositoryStatusConverter repositoryStatusConverter;

    @Before
    protected void prepare()
        throws Exception
    {
        repositoryStatusConverter = (RepositoryStatusConverter) getContainer().lookup( RepositoryStatusConverter.class );
    }

    // DISABLED: move to IT, it takes too long (no route to host + java)
    @Test
    public void nonTestSimplerAvailabilityCheckRemoteLeak()
        throws Exception
    {

        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it fail! (unknown host, so will not be able to connect)

        Repository repo1 = this.convertRepo( "release-proxy-repo-1" );
        repo1.setRemoteUrl( repo1.getRemoteUrl().replace( "localhost", "1.1.1.1" ) );

        Repository repo2 = this.convertRepo( "tasks-snapshot-repo" );

        // loop until we have some "sensible" result (not unknown, since this is async op)
        // first unforced request will trigger the check, and wait until we have result
        RemoteStatus rs1 = RemoteStatus.UNKNOWN;
        RemoteStatus rs2 = RemoteStatus.UNKNOWN;

        while ( RemoteStatus.UNKNOWN.equals( rs1 ) || RemoteStatus.UNKNOWN.equals( rs2 ) )
        {
            rs1 = repo1.getRemoteStatus( false );
            rs2 = repo2.getRemoteStatus( false );

            Thread.sleep( 1000 );
        }

        // get the default context, since they used it
        RemoteStorageContext ctx = new DefaultRemoteStorageContext( null );

        MultiThreadedHttpConnectionManager cm = (MultiThreadedHttpConnectionManager) ( (HttpClient) ctx
            .getRemoteConnectionContext().get( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
            .getHttpConnectionManager();
        Assert.assertEquals( 2, cm.getConnectionsInPool() );

    }

    private Repository convertRepo( String repoId )
        throws IOException
    {

        Repository repo = new M2Repository();

        CRepository cRepo = NexusConfigUtil.getRepo( repoId );

        repo.setId( cRepo.getId() );
        repo.setItemMaxAge( cRepo.getArtifactMaxAge() );
        // cRepo.getChecksumPolicy() );

        repo.setLocalStatus( repositoryStatusConverter.localStatusFromModel( cRepo.getLocalStatus() ) );

        if ( cRepo.getLocalStorage() != null )
        {
            repo.setLocalUrl( cRepo.getLocalStorage().getUrl() );
        }

        // repo.set cRepo.getMetadataMaxAge() );
        repo.setName( cRepo.getName() );
        repo.setNotFoundCacheTimeToLive( cRepo.getNotFoundCacheTTL() );
        repo.setProxyMode( repositoryStatusConverter.proxyModeFromModel( cRepo.getProxyMode() ) );
        repo.setRemoteUrl( cRepo.getRemoteStorage().getUrl() );
        // cRepo.getRepositoryPolicy() );

        return repo;
    }

}
