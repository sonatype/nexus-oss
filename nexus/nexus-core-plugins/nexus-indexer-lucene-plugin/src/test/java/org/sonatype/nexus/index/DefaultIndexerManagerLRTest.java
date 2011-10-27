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
package org.sonatype.nexus.index;

import java.util.Collection;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.junit.Test;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;

// This is an IT just because it runs longer then 15 seconds
public class DefaultIndexerManagerLRTest
    extends AbstractIndexerManagerTest
{

    private Nexus nexus;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexus = lookup( Nexus.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        nexus = null;

        super.tearDown();
    }

    @Test
    public void testRepoReindex()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", true );

        searchFor( "org.sonatype.nexus", 10 );

        assertTemporatyContexts( releases );
    }

    @Test
    public void testRepoKeywordSearch()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", true );

        searchForKeywordNG( "org.sonatype.nexus", 10 );

        assertTemporatyContexts( releases );
    }

    @Test
    public void testRepoSha1Search()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", true );

        // org.sonatype.nexus : nexus-indexer : 1.0-beta-4
        // sha1: 86e12071021fa0be4ec809d4d2e08f07b80d4877

        Collection<ArtifactInfo> ais = indexerManager.identifyArtifact( MAVEN.SHA1, "86e12071021fa0be4ec809d4d2e08f07b80d4877" );

        assertTrue( "The artifact has to be found!", ais.size() == 1 );

        IteratorSearchResponse response;

        // this will be EXACT search, since we gave full SHA1 checksum of 40 chars
        response =
            indexerManager.searchArtifactSha1ChecksumIterator( "86e12071021fa0be4ec809d4d2e08f07b80d4877", null, null,
                null, null, null );

        assertEquals( "There should be one hit!", 1, response.getTotalHits() );

        response.close();

        // this will be SCORED search, since we have just part of the SHA1 checksum
        response = indexerManager.searchArtifactSha1ChecksumIterator( "86e12071021", null, null, null, null, null );

        assertEquals( "There should be still one hit!", 1, response.getTotalHits() );

        response.close();
    }

    @Test
    public void testInvalidRemoteUrl()
        throws Exception
    {
        Maven2ProxyRepositoryTemplate t =
            (Maven2ProxyRepositoryTemplate) nexus.getRepositoryTemplateById( "default_proxy_snapshot" );
        t.getConfigurableRepository().setId( "invalidUrlRepo" );
        ProxyRepository r = t.create().adaptToFacet( ProxyRepository.class );
        r.setRemoteUrl( "http://repository.sonatyp.org/content/repositories/snapshots" );

        nexusConfiguration.saveConfiguration();

        indexerManager.reindexRepository( "/", r.getId(), true );
    }
}
