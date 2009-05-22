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

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

public class RepoConversionTest
    extends AbstractProxyTestEnvironment
{
    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private RemoteRepositoryStorage remoteRepositoryStorage;

    public void setUp()
        throws Exception
    {
        super.setUp();

        remoteRepositoryStorage = lookup( RemoteRepositoryStorage.class, "apacheHttpClient3x" );
    }

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testHosted2Proxy()
        throws Exception
    {
        MavenHostedRepository inhouse =
            getRepositoryRegistry().getRepositoryWithFacet( "inhouse", MavenHostedRepository.class );

        // check
        assertTrue( "repo is hosted", inhouse.getRepositoryKind().isFacetAvailable( HostedRepository.class ) );
        assertTrue( "repo is hosted", inhouse.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) );
        assertFalse( "repo is not proxied", inhouse.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) );
        assertFalse( "repo is not proxied", inhouse.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );

        // do the conversion
        // forcing cast
        MavenProxyRepository inhouseToMadeProxy = (MavenProxyRepository) inhouse;

        inhouseToMadeProxy.setRemoteStorage( remoteRepositoryStorage );

        inhouseToMadeProxy.setRemoteUrl( "http://repo1.maven.org/maven2/" );

        getApplicationConfiguration().saveConfiguration();

        // check
        assertFalse( "repo is not hosted", inhouse.getRepositoryKind().isFacetAvailable( HostedRepository.class ) );
        assertFalse( "repo is not hosted", inhouse.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) );
        assertTrue( "repo is proxied", inhouse.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) );
        assertTrue( "repo is proxied", inhouse.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );

        // now we just walk in, like nothing of above happened :)
        MavenProxyRepository aProxy =
            getRepositoryRegistry().getRepositoryWithFacet( "inhouse", MavenProxyRepository.class );

        assertNotNull( "It should exists (heh, but NoSuchRepo exception should be thrown anyway)", aProxy );

        assertEquals( "This should match, since they should be the same!", remoteRepositoryStorage.getProviderId(),
                      aProxy.getRemoteStorage().getProviderId() );

        assertEquals( "", remoteRepositoryStorage.getProviderId(), ( (CRepository) aProxy.getCurrentCoreConfiguration()
            .getConfiguration( false ) ).getRemoteStorage().getProvider() );
    }
}
