/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.gwt.client.services;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.Representation;

/**
 * Nexus Repositories service.
 * 
 * @author cstamas
 */
public interface RepositoriesService
{
    /**
     * List the available repositories.
     * 
     * @param handler
     */
    void getRepositories( EntityResponseHandler handler );

    /**
     * Gets a requested repository by path.
     * 
     * @param path
     */
    RepositoryService getRepositoryByPath( String path );

    /**
     * Gets a requested repository by id.
     * 
     * @param path
     */
    RepositoryService getRepositoryById( String id );

    /**
     * Creates a repository and returns it's Service.
     * 
     * @param representation
     * @return
     */
    RepositoryService createRepository( String id, Representation representation, StatusResponseHandler handler );
}
