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
package org.sonatype.nexus.index;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;

public class DefaultIndexerManagerTest
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

    public void testRepoReindex()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", false );

        searchFor( "org.sonatype.nexus", 9 );

        assertTemporatyContexts( releases );
    }

    public void testRepoKeywordSearch()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", false );
        
        searchForKeywordNG( "org.sonatype.nexus", 15 );

        assertTemporatyContexts( releases );
    }

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
