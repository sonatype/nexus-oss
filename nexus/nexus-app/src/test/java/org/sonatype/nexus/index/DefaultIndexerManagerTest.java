/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.index;

import java.util.Collection;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.scheduling.ScheduledTask;

public class DefaultIndexerManagerTest
    extends AbstractMavenRepoContentTests
{
    private IndexerManager indexerManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexerManager = (IndexerManager) lookup( IndexerManager.class );
    }

    protected void tearDown()
        throws Exception
    {
        indexerManager.shutdown( false );

        super.tearDown();
    }

    public void testRepoReindex()
        throws Exception
    {
        fillInRepo();

        ReindexTask reindexTask = defaultNexus.createTaskInstance( ReindexTask.class );

        ScheduledTask<Object> st = defaultNexus.submit( "reindexAll", reindexTask );

        // make it block until finished
        st.get();

        Collection<ArtifactInfo> result = indexerManager.getNexusIndexer().searchFlat(
            indexerManager.getNexusIndexer().constructQuery( ArtifactInfo.GROUP_ID, "org.sonatype.nexus" ) );

        // expected result set
        // org.sonatype.nexus:nexus-indexer:1.0-beta-5-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT :: cli
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT :: jdk14
        assertEquals( 5, result.size() );
    }
}
