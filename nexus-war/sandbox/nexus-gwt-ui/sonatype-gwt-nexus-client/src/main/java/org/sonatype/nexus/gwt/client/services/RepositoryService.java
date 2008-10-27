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
