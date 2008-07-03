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
