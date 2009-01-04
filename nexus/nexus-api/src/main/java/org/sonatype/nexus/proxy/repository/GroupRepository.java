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
package org.sonatype.nexus.proxy.repository;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A group repository is simply as it's name says, a repository that is backed by a group of other repositories. There
 * is one big constraint, they are READ ONLY. Usually, if you try a write/delete operation against this kind of
 * repository, you are doing something wrong. Deploys/writes and deletes should be done directly against the
 * hosted/proxied repositories, not against these "aggregated" ones.
 * 
 * @author cstamas
 */
public interface GroupRepository
    extends Repository
{
    /**
     * Returns the unmodifiable list of Repositories that are group members in this GroupRepository. The repo order
     * within list is repo rank (the order how they will be processed), so processing is possible by simply iterating
     * over resulting list.
     * 
     * @return a List<Repository>
     * @throws StorageException
     */
    List<Repository> getMemberRepositories();

    /**
     * Returns the list of available items in the group for same path. The resulting list keeps the order of reposes
     * queried for path.
     * 
     * @param uid
     * @param context
     * @return
     * @throws StorageException
     */
    List<StorageItem> doRetrieveItems( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException;
}
