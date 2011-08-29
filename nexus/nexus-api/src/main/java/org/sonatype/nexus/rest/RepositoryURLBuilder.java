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
     * Builds the content URL of a repository. If the repository parameter is not exposed (
     * {@link Repository#isExposed()} returns {@code false}), or if it is impossible to build the URL for any other
     * reason (like this call does not happen in a HTTP Request context and baseUrl is not set), this method returns
     * {@code null}.
     * 
     * @param repository
     * @return the content URL or {@code null}.
     */
    String getRepositoryContentUrl( Repository repository );
}
