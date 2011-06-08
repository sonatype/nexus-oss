/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its.nxcm1903;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class NXCM1903ValidateMaxAgeSettingsIT
    extends AbstractNexusIntegrationTest
{
    private final RepositoryMessageUtil repoUtil;

    public NXCM1903ValidateMaxAgeSettingsIT()
        throws Exception
    {
        repoUtil = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void validateUpdateSiteMaxAgeConfig()
        throws Exception
    {
        RepositoryProxyResource updatesiterepo = new RepositoryProxyResource();

        updatesiterepo.setRepoType( "proxy" );
        updatesiterepo.setId( "updatesite" );
        updatesiterepo.setName( "updatesite" );
        updatesiterepo.setBrowseable( true );
        updatesiterepo.setIndexable( false );
        updatesiterepo.setNotFoundCacheTTL( 1440 );
        updatesiterepo.setArtifactMaxAge( 100 );
        updatesiterepo.setMetadataMaxAge( 200 );
        updatesiterepo.setRepoPolicy( "RELEASE" );
        updatesiterepo.setProvider( "eclipse-update-site" );
        updatesiterepo.setProviderRole( "org.sonatype.nexus.proxy.repository.Repository" );
        updatesiterepo.setOverrideLocalStorageUrl( null );
        updatesiterepo.setDefaultLocalStorageUrl( null );
        updatesiterepo.setDownloadRemoteIndexes( false );
        updatesiterepo.setExposed( true );
        updatesiterepo.setChecksumPolicy( "WARN" );

        final RepositoryResourceRemoteStorage p2proxyRemoteStorage = new RepositoryResourceRemoteStorage();
        p2proxyRemoteStorage.setRemoteStorageUrl( "http://updatesite" );
        p2proxyRemoteStorage.setAuthentication( null );
        p2proxyRemoteStorage.setConnectionSettings( null );
        p2proxyRemoteStorage.setHttpProxySettings( null );

        updatesiterepo.setRemoteStorage( p2proxyRemoteStorage );

        updatesiterepo = (RepositoryProxyResource) repoUtil.createRepository( updatesiterepo, false );

        updatesiterepo = (RepositoryProxyResource) repoUtil.getRepository( updatesiterepo.getId() );

        Assert.assertEquals( 100, updatesiterepo.getArtifactMaxAge() );
        Assert.assertEquals( 200, updatesiterepo.getMetadataMaxAge() );

        // now do an update
        updatesiterepo.setArtifactMaxAge( 300 );
        updatesiterepo.setMetadataMaxAge( 400 );

        repoUtil.updateRepo( updatesiterepo, false );

        updatesiterepo = (RepositoryProxyResource) repoUtil.getRepository( updatesiterepo.getId() );

        Assert.assertEquals( 300, updatesiterepo.getArtifactMaxAge() );
        Assert.assertEquals( 400, updatesiterepo.getMetadataMaxAge() );
    }

    @Test
    public void validateP2ProxyMaxAgeConfig()
        throws Exception
    {
        RepositoryProxyResource p2repo = new RepositoryProxyResource();

        p2repo.setRepoType( "proxy" );
        p2repo.setId( "p2proxy" );
        p2repo.setName( "p2proxy" );
        p2repo.setBrowseable( true );
        p2repo.setIndexable( false );
        p2repo.setNotFoundCacheTTL( 1440 );
        p2repo.setArtifactMaxAge( 100 );
        p2repo.setMetadataMaxAge( 200 );
        p2repo.setRepoPolicy( "RELEASE" );
        p2repo.setProvider( "p2" );
        p2repo.setProviderRole( "org.sonatype.nexus.proxy.repository.Repository" );
        p2repo.setOverrideLocalStorageUrl( null );
        p2repo.setDefaultLocalStorageUrl( null );
        p2repo.setDownloadRemoteIndexes( false );
        p2repo.setExposed( true );
        p2repo.setChecksumPolicy( "WARN" );

        final RepositoryResourceRemoteStorage p2proxyRemoteStorage = new RepositoryResourceRemoteStorage();
        p2proxyRemoteStorage.setRemoteStorageUrl( "http://p2proxy" );
        p2proxyRemoteStorage.setAuthentication( null );
        p2proxyRemoteStorage.setConnectionSettings( null );
        p2proxyRemoteStorage.setHttpProxySettings( null );

        p2repo.setRemoteStorage( p2proxyRemoteStorage );

        p2repo = (RepositoryProxyResource) repoUtil.createRepository( p2repo, false );

        p2repo = (RepositoryProxyResource) repoUtil.getRepository( p2repo.getId() );

        Assert.assertEquals( 100, p2repo.getArtifactMaxAge() );
        Assert.assertEquals( 200, p2repo.getMetadataMaxAge() );

        // now do an update
        p2repo.setArtifactMaxAge( 300 );
        p2repo.setMetadataMaxAge( 400 );

        repoUtil.updateRepo( p2repo, false );

        p2repo = (RepositoryProxyResource) repoUtil.getRepository( p2repo.getId() );

        Assert.assertEquals( 300, p2repo.getArtifactMaxAge() );
        Assert.assertEquals( 400, p2repo.getMetadataMaxAge() );
    }
}
