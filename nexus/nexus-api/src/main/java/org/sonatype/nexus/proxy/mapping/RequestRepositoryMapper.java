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
package org.sonatype.nexus.proxy.mapping;

import java.util.List;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface RequestRepositoryMapper. These mappers are used in Routers, to narrow the number of searched
 * repositories using some technique.
 */
public interface RequestRepositoryMapper
    extends Configurable
{
    /**
     * Returns an unmodifiable Map of mappings.
     * 
     * @return
     */
    Map<String, RepositoryPathMapping> getMappings();

    /**
     * Adds new mapping.
     * 
     * @param mapping
     * @throws ConfigurationException 
     */
    boolean addMapping( RepositoryPathMapping mapping ) throws ConfigurationException;

    /**
     * Removes mapping.
     * 
     * @param id
     */
    boolean removeMapping( String id );

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
