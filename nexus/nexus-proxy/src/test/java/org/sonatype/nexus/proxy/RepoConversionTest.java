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
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
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

    // WARNING!
    // ////////////////////////////
    // The casts you see in this code should be considered ILLEGAL!
    // This code simply tests some "spicy" nature, but Nexus plugins and other API consumers should
    // NEVER use casts like these below!

    protected void convertHosted2Proxy( MavenHostedRepository patient )
        throws Exception
    {
        // check
        assertTrue( "repo is hosted", patient.getRepositoryKind().isFacetAvailable( HostedRepository.class ) );
        assertTrue( "repo is hosted", patient.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) );
        assertFalse( "repo is not proxied", patient.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) );
        assertFalse( "repo is not proxied", patient.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );

        // do the conversion
        // forcing cast
        MavenProxyRepository repoToBeTreated = (MavenProxyRepository) patient;

        repoToBeTreated.setRemoteStorage( remoteRepositoryStorage );

        repoToBeTreated.setRemoteUrl( "http://repo1.maven.org/maven2/" );

        getApplicationConfiguration().saveConfiguration();

        // check
        assertFalse( "repo is not hosted", patient.getRepositoryKind().isFacetAvailable( HostedRepository.class ) );
        assertFalse( "repo is not hosted", patient.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) );
        assertTrue( "repo is proxied", patient.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) );
        assertTrue( "repo is proxied", patient.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );

        // now we just walk in, like nothing of above happened :)
        MavenProxyRepository afterTreatment =
            getRepositoryRegistry().getRepositoryWithFacet( patient.getId(), MavenProxyRepository.class );

        assertNotNull( "It should exists (heh, but NoSuchRepo exception should be thrown anyway)", afterTreatment );

        assertEquals( "This should match, since they should be the same!", remoteRepositoryStorage.getProviderId(),
                      afterTreatment.getRemoteStorage().getProviderId() );

        assertEquals( "Config should state the same as object is", afterTreatment.getRemoteStorage().getProviderId(),
                      ( ( (CRepositoryCoreConfiguration) afterTreatment.getCurrentCoreConfiguration() )
                          .getConfiguration( false ) ).getRemoteStorage().getProvider() );

        assertEquals( "Config should state the same as object is", afterTreatment.getRemoteUrl(),
                      ( ( (CRepositoryCoreConfiguration) afterTreatment.getCurrentCoreConfiguration() )
                          .getConfiguration( false ) ).getRemoteStorage().getUrl() );
    }

    protected void convertProxy2Hosted( MavenProxyRepository patient )
        throws Exception
    {
        // check
        assertFalse( "repo is not hosted", patient.getRepositoryKind().isFacetAvailable( HostedRepository.class ) );
        assertFalse( "repo is not hosted", patient.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) );
        assertTrue( "repo is proxied", patient.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) );
        assertTrue( "repo is proxied", patient.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );

        // do the conversion
        patient.setRemoteStorage( null );

        getApplicationConfiguration().saveConfiguration();

        // check
        assertTrue( "repo is hosted", patient.getRepositoryKind().isFacetAvailable( HostedRepository.class ) );
        assertTrue( "repo is hosted", patient.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) );
        assertFalse( "repo is not proxied", patient.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) );
        assertFalse( "repo is not proxied", patient.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) );

        // now we just walk in, like nothing of above happened :)
        MavenHostedRepository afterTreatment =
            getRepositoryRegistry().getRepositoryWithFacet( patient.getId(), MavenHostedRepository.class );

        assertNotNull( "It should exists (heh, but NoSuchRepo exception should be thrown anyway)", afterTreatment );
    }

    public void testHosted2Proxy()
        throws Exception
    {
        Repository patient = getRepositoryRegistry().getRepositoryWithFacet( "inhouse", MavenHostedRepository.class );
        
        assertTrue( "This repo should not be READ only!", RepositoryWritePolicy.READ_ONLY != patient.getWritePolicy() );

        convertHosted2Proxy( (MavenHostedRepository) patient );

        assertTrue( "Partient should be proxy", patient.getRepositoryKind()
            .isFacetAvailable( MavenProxyRepository.class ) );

        assertTrue( "This repo should be READ only!", RepositoryWritePolicy.READ_ONLY == patient.getWritePolicy() );
    }

    public void testProxy2Hosted()
        throws Exception
    {
        Repository patient = getRepositoryRegistry().getRepositoryWithFacet( "repo1", MavenProxyRepository.class );

        assertTrue( "This repo should be READ only!", RepositoryWritePolicy.READ_ONLY == patient.getWritePolicy() );

        convertProxy2Hosted( (MavenProxyRepository) patient );

        assertTrue( "Partient should be hosted", patient.getRepositoryKind()
            .isFacetAvailable( MavenHostedRepository.class ) );
    }

    public void testHosted2Proxy2Hosted()
        throws Exception
    {
        Repository patient = getRepositoryRegistry().getRepositoryWithFacet( "inhouse", MavenHostedRepository.class );

        convertHosted2Proxy( (MavenHostedRepository) patient );

        convertProxy2Hosted( (MavenProxyRepository) patient );

        assertTrue( "Partient should be hosted", patient.getRepositoryKind()
            .isFacetAvailable( MavenHostedRepository.class ) );
    }

    public void testProxy2Hosted2Proxy()
        throws Exception
    {
        Repository patient = getRepositoryRegistry().getRepositoryWithFacet( "repo1", MavenProxyRepository.class );

        convertProxy2Hosted( (MavenProxyRepository) patient );

        convertHosted2Proxy( (MavenHostedRepository) patient );

        assertTrue( "Partient should be proxy", patient.getRepositoryKind()
            .isFacetAvailable( MavenProxyRepository.class ) );
    }

}
