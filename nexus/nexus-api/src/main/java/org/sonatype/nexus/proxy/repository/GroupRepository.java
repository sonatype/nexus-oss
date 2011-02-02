/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.plugin.ExtensionPoint;

/**
 * A group repository is simply as it's name says, a repository that is backed by a group of other repositories. There
 * is one big constraint, they are READ ONLY. Usually, if you try a write/delete operation against this kind of
 * repository, you are doing something wrong. Deploys/writes and deletes should be done directly against the
 * hosted/proxied repositories, not against these "aggregated" ones.
 * 
 * @author cstamas
 */
@ExtensionPoint
public interface GroupRepository
    extends Repository
{
    /**
     * Returns the unmodifiable ID list of the members of this group.
     * 
     * @return
     */
    List<String> getMemberRepositoryIds();

    /**
     * Sets the members of this group.
     * 
     * @param repositories
     */
    void setMemberRepositoryIds( List<String> repositories )
        throws NoSuchRepositoryException, InvalidGroupingException;

    /**
     * Adds a member to this group.
     * 
     * @param repositoryId
     */
    void addMemberRepositoryId( String repositoryId )
        throws NoSuchRepositoryException, InvalidGroupingException;

    /**
     * Removes a member from this group.
     * 
     * @param repositoryId
     */
    void removeMemberRepositoryId( String repositoryId );

    /**
     * Returns the unmodifiable list of Repositories that are group members in this GroupRepository. The repo order
     * within list is repo rank (the order how they will be processed), so processing is possible by simply iterating
     * over resulting list.
     * 
     * @return a List<Repository>
     */
    List<Repository> getMemberRepositories();

    /**
     * Returns the unmodifiable list of Transitive Repositories that are group members in this GroupRepository. This
     * method differs from {@link #getMemberRepositories()} by resolving all inner groups member as well. <b>The
     * resulting list won't contain any GroupRepository.</b>
     * 
     * @return a List<Repository>
     */
    List<Repository> getTransitiveMemberRepositories();

    /**
     * Returns the unmodifiable ID list of the transitive members of this group. This method differs from
     * {@link #getMemberRepositoryIds()} by resolving all inner groups member as well. <b>The resulting list won't
     * contain any GroupRepository.</b>
     * 
     * @return a List<Repository>
     */
    List<String> getTransitiveMemberRepositoryIds();

    /**
     * Returns the list of available items in the group for same path. The resulting list keeps the order of reposes
     * queried for path.
     * 
     * @param uid
     * @param context
     * @return
     * @throws StorageException
     */
    List<StorageItem> doRetrieveItems( ResourceStoreRequest request )
        throws StorageException;
}
