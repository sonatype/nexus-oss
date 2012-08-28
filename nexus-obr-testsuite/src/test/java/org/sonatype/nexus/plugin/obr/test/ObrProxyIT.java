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
package org.sonatype.nexus.plugin.obr.test;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class ObrProxyIT
    extends ObrITSupport
{

    public ObrProxyIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void downloadFromProxy()
        throws Exception
    {
        final String hRId = repositoryIdForTest() + "-hosted";
        final String pRId = repositoryIdForTest() + "-proxy";

        createObrHostedRepository( hRId );

        upload( hRId, FELIX_WEBCONSOLE );
        upload( hRId, OSGI_COMPENDIUM );
        upload( hRId, GERONIMO_SERVLET );
        upload( hRId, PORTLET_API );

        createObrProxyRepository(
            pRId,
            format( "%scontent/repositories/%s/.meta/obr.xml", nexus().getUrl().toExternalForm(), hRId )
        );

        deployUsingObrIntoFelix( pRId );

        verifyExistenceInStorage( pRId, FELIX_WEBCONSOLE );
        verifyExistenceInStorage( pRId, OSGI_COMPENDIUM );
        verifyExistenceInStorage( pRId, GERONIMO_SERVLET );
        verifyExistenceInStorage( pRId, PORTLET_API );
    }

    private void verifyExistenceInStorage( final String repositoryId, final String filename )
    {
        assertThat(
            new File(
                nexus().getWorkDirectory(), format( "storage/%s/%s", repositoryId, filename )
            ),
            exists()
        );
    }

    @Test
    public void validateOBRProxyUrlChanges()
        throws Exception
    {
        final RepositoryProxyResource proxyRepo = new RepositoryProxyResource();
        proxyRepo.setRepoType( "proxy" );
        proxyRepo.setId( repositoryIdForTest() + "-proxy" );
        proxyRepo.setName( proxyRepo.getId() );
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
        repositories().createRepository( proxyRepo );

        // check for equality here
        assertObrPath( proxyRepo.getId(), "http://sigil.codecauldron.org/", "/spring-external.obr" );

        // note internal opposed to external
        proxyRepo.getRemoteStorage().setRemoteStorageUrl( "http://sigil.codecauldron.org/spring-internal.obr" );

        // update the repo
        repositories().updateRepo( proxyRepo );

        // check again for equality here
        assertObrPath( proxyRepo.getId(), "http://sigil.codecauldron.org/", "/spring-internal.obr" );

        // note sigil2
        proxyRepo.getRemoteStorage().setRemoteStorageUrl( "http://sigil2.codecauldron.org/spring-external.obr" );

        // update the repo
        repositories().updateRepo( proxyRepo );

        // check again for equality here
        assertObrPath( proxyRepo.getId(), "http://sigil2.codecauldron.org/", "/spring-external.obr" );

        // note sigil3 and external -> internal
        proxyRepo.getRemoteStorage().setRemoteStorageUrl( "http://sigil3.codecauldron.org/spring-internal.obr" );

        // update the repo
        repositories().updateRepo( proxyRepo );

        // check again for equality here
        assertObrPath( proxyRepo.getId(), "http://sigil3.codecauldron.org/", "/spring-internal.obr" );
    }

    private void assertObrPath( final String repositoryId,
                                final String expectedRemoteUrl,
                                final String expectedObrPath )
        throws Exception
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse( new File( nexus().getWorkDirectory(), "conf/nexus.xml" ) );

        final XPath xpath = XPathFactory.newInstance().newXPath();

        final Node url = (Node) xpath.evaluate(
            "/nexusConfiguration/repositories/repository[id='" + repositoryId + "']/remoteStorage/url",
            doc, XPathConstants.NODE
        );
        assertThat( url, is( notNullValue() ) );
        assertThat( url.getTextContent(), is( expectedRemoteUrl ) );

        final Node obrPath = (Node) xpath.evaluate(
            "/nexusConfiguration/repositories/repository[id='" + repositoryId + "']/externalConfiguration/obrPath",
            doc, XPathConstants.NODE
        );
        assertThat( obrPath, is( notNullValue() ) );
        assertThat( obrPath.getTextContent(), is( expectedObrPath ) );
    }

}
