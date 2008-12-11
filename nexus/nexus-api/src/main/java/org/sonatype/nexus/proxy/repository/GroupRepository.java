/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;

/**
 * A group repository is simply as it's name says, a repository that is backed by a group of other repositories. There
 * is one big constraint, they are READ ONLY. Usually, if you try a write/delete operation against this kind of
 * repository, you are doing something wrong. Deploys/writed and deletes should be done directly against the
 * hosted/proxied repositories, not against these "aggregated" ones.
 * 
 * @author cstamas
 */
public interface GroupRepository
    extends Repository
{
    /**
     * This is the "class" of the repository content. It is used in grouping, only same content reposes may be grouped.
     * The group may have set contentClass before adding reposes, then it will only those reposes to be members that
     * have the set contentClass. Otherwise, it will pick the contentClass of 1st repository added as member.
     * 
     * @return
     */
    void setRepositoryContentClass( ContentClass contentClass );

    /**
     * Returns the unmodifiable list of Repositories that are group members in this GroupRepository. The repo order
     * within list is repo rank (the order how they will be processed), so processing is possible by simply iterating
     * over resulting list.
     * 
     * @return a List<Repository>
     */
    List<Repository> getMemberRepositories();

    /**
     * Adds a new Repository as member to this group.
     * 
     * @param repository
     * @throws InvalidGroupingException
     */
    void addMemberRepository( Repository repository )
        throws InvalidGroupingException;

    /**
     * Adds several Repositories as members to this group. All reposes in the list have to have compatible ContentClass
     * within each other.
     * 
     * @param repositories
     * @throws InvalidGroupingException
     */
    void addMemberRepositories( List<Repository> repositories )
        throws InvalidGroupingException;

    /**
     * Removed the repository from this group.
     * 
     * @param repository
     * @throws NoSuchRepositoryException
     * @throws InvalidGroupingException
     */
    void removeMemberRepository( Repository repository )
        throws NoSuchRepositoryException,
            InvalidGroupingException;
}
