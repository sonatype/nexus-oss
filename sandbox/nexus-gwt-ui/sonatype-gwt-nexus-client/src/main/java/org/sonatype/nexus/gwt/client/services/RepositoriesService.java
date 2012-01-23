/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
