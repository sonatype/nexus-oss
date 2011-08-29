package org.sonatype.nexus.rest;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;

public interface RepositoryURLBuilder
{
    /**
     * Builds the content URL of a repository identified by Id.
     * 
     * @param repositoryId
     * @return the content URL.
     */
    String getRepositoryContentUrl( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Builds the content URL of a repository.
     * 
     * @param repository
     * @return the content URL.
     */
    String getRepositoryContentUrl( Repository repository );
}
