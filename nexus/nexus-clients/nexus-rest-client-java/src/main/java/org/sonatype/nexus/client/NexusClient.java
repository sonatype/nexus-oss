/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.client;

import java.util.List;

import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;

public interface NexusClient
{
    public static final String ROLE = NexusClient.class.getName();

    /**
     * Connects to nexus server
     */
    public void connect( String baseUrl, String username, String password )
        throws NexusClientException, NexusConnectionException;

    /**
     * Ends the connection with nexus server
     */
    public void disconnect()
        throws NexusClientException, NexusConnectionException;

    /**
     * Creates a new repository on nexus
     */
    public RepositoryBaseResource createRepository( RepositoryBaseResource repo )
        throws NexusClientException, NexusConnectionException;

    /**
     * Updates a existing repository on nexus
     */
    public RepositoryBaseResource updateRepository( RepositoryBaseResource repo )
        throws NexusClientException, NexusConnectionException;

    /**
     * Retrieve a repository from nexus using ID
     */
    public RepositoryBaseResource getRepository( String id )
        throws NexusClientException, NexusConnectionException;

    /**
     * Checks if a repository ID exists on nexus
     */
    public boolean isValidRepository( String id )
        throws NexusClientException, NexusConnectionException;

    /**
     * Delete a repository from nexus using ID
     */
    public void deleteRepository( String id )
        throws NexusClientException, NexusConnectionException;

    /**
     * Retrieve all repositories from nexus
     */
    public List<RepositoryListResource> getRepositories()
        throws NexusClientException, NexusConnectionException;

    /**
     * Search for an artifact using SHA1 hash
     */
    public NexusArtifact searchBySHA1( String sha1 )
        throws NexusClientException, NexusConnectionException;

    /**
     * Search for an artifact using Maven GAV coordinates (groupId, artifactId, version, classifier, extension)
     */
    public List<NexusArtifact> searchByGAV( NexusArtifact gav )
        throws NexusClientException, NexusConnectionException;

    /**
     * Check is nexus server is started
     * 
     * @param blocking when true waits nexus to start
     */
    public boolean isNexusStarted( boolean blocking )
        throws NexusClientException, NexusConnectionException;

    /**
     * Stop nexus server
     */
    public void stopNexus()
        throws NexusClientException, NexusConnectionException;

    /**
     * Start nexus server
     */
    public void startNexus()
        throws NexusClientException, NexusConnectionException;

    /**
     * Restart nexus server
     */
    public void restartNexus()
        throws NexusClientException, NexusConnectionException;
}
