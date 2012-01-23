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
