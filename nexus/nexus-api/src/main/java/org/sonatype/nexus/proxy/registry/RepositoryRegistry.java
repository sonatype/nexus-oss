/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.proxy.registry;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
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
     * Adds the repository group.
     * 
     * @param groupId the group id
     * @param memberRepositories the repository IDs
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws InvalidGroupingException when reposes to group are not groupable
     */
    void addRepositoryGroup( String groupId, List<String> memberRepositories )
        throws NoSuchRepositoryException,
            InvalidGroupingException;

    /**
     * Removes a grouping of repositories. The group members are not removed.
     * 
     * @param groupId the group id
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     */
    void removeRepositoryGroup( String groupId )
        throws NoSuchRepositoryGroupException;

    /**
     * Removes a grouping of repositories, and also the member repositories if requested.
     * 
     * @param groupId the group id
     * @param withRepositories if true, the members will be removed too
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     */
    void removeRepositoryGroup( String groupId, boolean withRepositories )
        throws NoSuchRepositoryGroupException;

    /**
     * Returns the list of Repositories that serves Proximity. The repo order within list follows repo rank, so
     * processing is possible by simply iterating over resulting list.
     * 
     * @return a List<Repository>
     */
    List<Repository> getRepositories();

    /**
     * Returns the list of existing repository group IDs.
     * 
     * @return
     */
    List<String> getRepositoryGroupIds();

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
     * Returns the list of Repositories in given group. The repo order within list follows repo rank, so processing is
     * possible by simply iterating over resulting list.
     * 
     * @param groupId the group id
     * @return a List<Repository>
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     */
    List<Repository> getRepositoryGroup( String groupId )
        throws NoSuchRepositoryGroupException;

    /**
     * Returns the content class of repository group.
     * 
     * @param groupId
     * @return
     * @throws NoSuchRepositoryGroupException
     */
    ContentClass getRepositoryGroupContentClass( String groupId )
        throws NoSuchRepositoryGroupException;

    /**
     * Checks for the existence of given repositoryId within this registry.
     * 
     * @param repositoryId the repository id
     * @return boolean
     */
    boolean repositoryIdExists( String repositoryId );

    /**
     * Checks for the existence of given repositoryGroupId (group) within this registry.
     * 
     * @param repositoryGroupId the repository group id
     * @return boolean
     */
    boolean repositoryGroupIdExists( String repositoryGroupId );

    /**
     * Collect the groupIds where repository is member.
     * 
     * @param repositoryId the repository id
     * @return list of groupId's where the repo appears as member
     */
    List<String> getGroupsOfRepository( String repositoryId );
}
