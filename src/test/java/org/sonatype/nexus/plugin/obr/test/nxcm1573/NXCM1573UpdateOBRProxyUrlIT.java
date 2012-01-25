/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.obr.test.nxcm1573;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Assert;
import org.restlet.data.MediaType;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.plugin.obr.test.AbstractOBRIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.annotations.Test;

public class NXCM1573UpdateOBRProxyUrlIT
    extends AbstractOBRIntegrationTest
{
    RepositoryMessageUtil repoMessageUtil;

    public NXCM1573UpdateOBRProxyUrlIT()
        throws Exception
    {
        repoMessageUtil = new RepositoryMessageUtil( this, getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void validateOBRProxyUrlChanges()
        throws Exception
    {
        final RepositoryProxyResource proxyRepo = new RepositoryProxyResource();
        proxyRepo.setRepoType( "proxy" );
        proxyRepo.setId( "obr" );
        proxyRepo.setName( "obr" );
        proxyRepo.setBrowseable( true );
        proxyRepo.setIndexable( false );
        proxyRepo.setNotFoundCacheTTL( 1440 );
        proxyRepo.setArtifactMaxAge( -1 );
        proxyRepo.setMetadataMaxAge( 1440 );
        proxyRepo.setRepoPolicy( "RELEASE" );
        proxyRepo.setProvider( "obr-proxy" );
        proxyRepo.setProviderRole( "org.sonatype.nexus.proxy.repository.Repository" );
        proxyRepo.setOverrideLocalStorageUrl( null );
        proxyRepo.setDefaultLocalStorageUrl( null );
        proxyRepo.setDownloadRemoteIndexes( false );
        proxyRepo.setExposed( true );
        proxyRepo.setChecksumPolicy( "WARN" );

        final RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://sigil.codecauldron.org/spring-external.obr" );
        remoteStorage.setAuthentication( null );
        remoteStorage.setConnectionSettings( null );
        remoteStorage.setHttpProxySettings( null );

        proxyRepo.setRemoteStorage( remoteStorage );

        // create the repo
        repoMessageUtil.createRepository( proxyRepo, false );

        // check for equality here
        assertObrPath( getNexusConfigUtil().getRepo( "obr" ), "http://sigil.codecauldron.org/", "/spring-external.obr" );

        // note internal opposed to external
        proxyRepo.getRemoteStorage().setRemoteStorageUrl( "http://sigil.codecauldron.org/spring-internal.obr" );

        // update the repo
        repoMessageUtil.updateRepo( proxyRepo, false );

        // check again for equality here
        assertObrPath( getNexusConfigUtil().getRepo( "obr" ), "http://sigil.codecauldron.org/", "/spring-internal.obr" );

        // note sigil2
        proxyRepo.getRemoteStorage().setRemoteStorageUrl( "http://sigil2.codecauldron.org/spring-external.obr" );

        // update the repo
        repoMessageUtil.updateRepo( proxyRepo, false );

        // check again for equality here
        assertObrPath( getNexusConfigUtil().getRepo( "obr" ), "http://sigil2.codecauldron.org/", "/spring-external.obr" );

        // note sigil3 and external -> internal
        proxyRepo.getRemoteStorage().setRemoteStorageUrl( "http://sigil3.codecauldron.org/spring-internal.obr" );

        // update the repo
        repoMessageUtil.updateRepo( proxyRepo, false );

        // check again for equality here
        assertObrPath( getNexusConfigUtil().getRepo( "obr" ), "http://sigil3.codecauldron.org/", "/spring-internal.obr" );
    }

    private void assertObrPath( final CRepository repository, final String remoteUrlShouldBe,
                                final String obrPathShouldBe )
    {
        Assert.assertEquals( remoteUrlShouldBe, repository.getRemoteStorage().getUrl() );

        final Xpp3Dom dom = (Xpp3Dom) repository.getExternalConfiguration();

        final Xpp3Dom child = dom.getChild( "obrPath" );

        Assert.assertEquals( obrPathShouldBe, child.getValue() );
    }
}
