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
package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;

/**
 * Reindex task.
 *
 * @author cstamas
 * @author Alin Dreghiciu
 */
@Component( role = ReindexTaskHandler.class, hint = "legacy" )
public class ReindexTaskHandlerLegacy
    implements ReindexTaskHandler
{

    @Requirement
    private IndexerManager indexerManager;

    /**
     * Delegates to indexer manager.
     *
     * {@inheritDoc}
     */
    public void reindexAllRepositories( final String path,
                                        final boolean fullReindex )
        throws Exception
    {
        indexerManager.reindexAllRepositories( path, fullReindex );
    }

    /**
     * Delegates to indexer manager.
     *
     * {@inheritDoc}
     */
    public void reindexRepository( final String repositoryId,
                                   final String path,
                                   final boolean fullReindex )
        throws Exception
    {
        indexerManager.reindexRepository( path, repositoryId, fullReindex );
    }

    /**
     * Delegates to indexer manager.
     *
     * {@inheritDoc}
     */
    public void reindexRepositoryGroup( final String repositoryId,
                                        final String path,
                                        final boolean fullReindex )
        throws Exception
    {
        indexerManager.reindexRepositoryGroup( path, repositoryId, fullReindex );
    }

}
