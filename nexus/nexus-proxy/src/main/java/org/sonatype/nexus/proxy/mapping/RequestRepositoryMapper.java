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
package org.sonatype.nexus.proxy.mapping;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface RequestRepositoryMapper. These mappers are used in Routers, to narrow the number of searched
 * repositories using some technique.
 */
public interface RequestRepositoryMapper
    extends EventListener
{
    /**
     * Gets the mapped repositories.
     * 
     * @param request the request
     * @param resolvedRepositories the resolved repositories, possibly a bigger set
     * @return the mapped repositories repoIds
     */
    List<Repository> getMappedRepositories( Repository repository, ResourceStoreRequest request,
        List<Repository> resolvedRepositories )
        throws NoSuchResourceStoreException;

}
