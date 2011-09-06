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
package org.sonatype.nexus.rest;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;

public interface RepositoryURLBuilder
{
    /**
     * Builds the content URL of a repository identified by Id. See {@link #getRepositoryContentUrl(Repository)} for
     * full description.
     * 
     * @param repositoryId
     * @return the content URL.
     */
    String getRepositoryContentUrl( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Builds the content URL of a repository. Under some circumstances, it is impossible to build the URL for
     * Repository (example: this call does not happen in a HTTP Request context and baseUrl is not set), in such cases
     * this method returns {@code null}. Word of warning: the fact that a content URL is returned for a Repository does
     * not imply that the same repository is reachable over that repository! It still depends is the Repository exposed
     * or not {@link Repository#isExposed()}.
     * 
     * @param repository
     * @return the content URL or {@code null}.
     */
    String getRepositoryContentUrl( Repository repository );

    /**
     * Builds the exposed content URL of a repository. Same as {@link #getRepositoryContentUrl(Repository)} but honors
     * {@link Repository#isExposed()}, by returning {@code null} when repository is not exposed.
     * 
     * @param repository
     * @return the content URL or {@code null}.
     */
    String getExposedRepositoryContentUrl( Repository repository );
}
