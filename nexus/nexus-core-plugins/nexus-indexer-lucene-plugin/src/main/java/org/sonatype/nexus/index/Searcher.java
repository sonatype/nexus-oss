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
package org.sonatype.nexus.index;

import java.util.List;
import java.util.Map;

import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.SearchType;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * A searcher is able to perform artifact info searches based on key/value search terms. Note that this is an
 * intermediate step towards future Nexus pluggable indexing and should not be considered public api.
 * 
 * @author Alin Dreghiciu
 */
public interface Searcher
{

    /**
     * Answers the question: can this searcher be used to search for the available terms?
     * 
     * @param terms available terms
     * @return true if searcher can be used to search for the available terms, false oterwise
     */
    boolean canHandle( Map<String, String> terms );

    /**
     * Returns the default "search type", that this Searcher wants. Naturally, this is overridable, see
     * flatIteratorSearch() method.
     * 
     * @return
     */
    SearchType getDefaultSearchType();

    /**
     * Searches for artifacts based on available terms.
     * 
     * @param terms search terms
     * @param repositoryId repository id of the repository to be searched ir null if the search should be performed on
     *            all repositories that suports indexing
     * @param from offset of first search result
     * @param count number of search results to be retrieved
     * @return search results
     * @throws NoSuchRepositoryException - If there is no repository with specified repository id
     * @deprecated use flatIteratorSearch() instead.
     */
    FlatSearchResponse flatSearch( Map<String, String> terms, String repositoryId, Integer from, Integer count,
                                   Integer hitLimit )
        throws NoSuchRepositoryException;

    /**
     * Searches for artifacts based on available terms.
     * 
     * @param terms search terms
     * @param repositoryId repository id of the repository to be searched ir null if the search should be performed on
     *            all repositories that suports indexing
     * @param from offset of first search result
     * @param count number of search results to be retrieved
     * @return search results
     * @throws NoSuchRepositoryException - If there is no repository with specified repository id
     */
    IteratorSearchResponse flatIteratorSearch( Map<String, String> terms, String repositoryId, Integer from,
                                               Integer count, Integer hitLimit, boolean uniqueRGA, SearchType searchType, 
                                               List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;
}
