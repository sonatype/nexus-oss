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
package org.sonatype.nexus.gwt.client.services;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.Representation;

/**
 * Nexus Repository Service.
 * 
 * @author cstamas
 */
public interface RepositoryService
{
    /**
     * Creates a repository based on representation.
     * 
     * @param representation
     */
    void create( Representation representation, StatusResponseHandler handler );

    /**
     * Reads the repository State Object.
     * 
     * @param handler
     */
    void read( EntityResponseHandler handler );

    /**
     * Updates the repository state with representation.
     * 
     * @param representation
     */
    void update( Representation representation, StatusResponseHandler handler );

    /**
     * Deletes this repository.
     * 
     * @param handler
     */
    void delete( StatusResponseHandler handler );

    /**
     * Reads this repository meta data.
     * 
     * @param handler
     */
    void readRepositoryMeta( EntityResponseHandler handler );

    /**
     * Reads the current status of this repository.
     * 
     * @param handler
     */
    void readRepositoryStatus( EntityResponseHandler handler );

    /**
     * Updates the status of this repository and returns the new status.
     * 
     * @param representation
     * @param handler
     */
    void updateRepositoryStatus( Representation representation, EntityResponseHandler handler );
}
