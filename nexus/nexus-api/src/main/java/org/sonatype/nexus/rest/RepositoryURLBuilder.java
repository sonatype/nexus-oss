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
