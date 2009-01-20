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
package org.sonatype.nexus.proxy.registry;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.EventMulticaster;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface RepositoryRegistry. It holds the Repositories and repo groups.
 * 
 * @author cstamas
 */
public interface RepositoryRegistry
    extends EventMulticaster
{
    /**
     * Adds single repository.
     * 
     * @param repository the repository
     */
    void addRepository( Repository repository );

    /**
     * Updates (replaces) single repository, with keeping all its memberships in groups, etc.
     * 
     * @param repository the repository
     */
    void updateRepository( Repository repository )
        throws NoSuchRepositoryException;

    /**
     * Removes single repository.
     * 
     * @param repoId the repo id
     * @throws NoSuchRepositoryException the no such repository exception
     */
    void removeRepository( String repoId )
        throws NoSuchRepositoryException;

    /**
     * Removes "silently" single repository: no events will be emitted.
     * 
     * @param repoId the repo id
     * @throws NoSuchRepositoryException the no such repository exception
     */
    void removeRepositorySilently( String repoId )
        throws NoSuchRepositoryException;

    /**
     * Returns the list of Repositories that serves Proximity. The repo order within list follows repo rank, so
     * processing is possible by simply iterating over resulting list.
     * 
     * @return a List<Repository>
     */
    List<Repository> getRepositories();

    /**
     * Returns the list of Repositories that serves Proximity and have facets as T available. The repo order within list
     * follows repo rank, so processing is possible by simply iterating over resulting list.
     * 
     * @return a List<T>
     */
    <T> List<T> getRepositoriesWithFacet( Class<T> f );

    /**
     * Returns the requested Repository by ID.
     * 
     * @param repoId the repo id
     * @return the repository
     * @throws NoSuchRepositoryException the no such repository exception
     */
    Repository getRepository( String repoId )
        throws NoSuchRepositoryException;

    /**
     * Returns the requested Repository by ID.
     * 
     * @param repoId the repo id
     * @return the repository
     * @throws NoSuchRepositoryException the no such repository exception
     */
    <T> T getRepositoryWithFacet( String repoId, Class<T> f )
        throws NoSuchRepositoryException;

    /**
     * Checks for the existence of given repositoryId within this registry.
     * 
     * @param repositoryId the repository id
     * @return boolean
     */
    boolean repositoryIdExists( String repositoryId );

    /**
     * Collect the groupIds where repository is member.
     * 
     * @param repositoryId the repository id
     * @return list of groupId's where the repo appears as member
     */
    List<String> getGroupsOfRepository( String repositoryId );
}
