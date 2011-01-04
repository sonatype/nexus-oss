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
package org.sonatype.nexus.client;

import java.util.List;

import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;

public interface NexusClient
{
    public static final String ROLE = NexusClient.class.getName();

    public void connect( String baseUrl, String username, String password ) throws NexusClientException, NexusConnectionException;
    
    public void disconnect() throws NexusClientException, NexusConnectionException;
    
    public RepositoryBaseResource createRepository( RepositoryBaseResource repo ) throws NexusClientException, NexusConnectionException;
    
    public RepositoryBaseResource updateRepository( RepositoryBaseResource repo ) throws NexusClientException, NexusConnectionException;
    
    public RepositoryBaseResource getRepository( String id ) throws NexusClientException, NexusConnectionException;
    
    public boolean isValidRepository( String id ) throws NexusClientException, NexusConnectionException;
    
    public void deleteRepository( String id ) throws NexusClientException, NexusConnectionException;
    
    public List<RepositoryListResource> getRespositories() throws NexusClientException, NexusConnectionException;
    
    
    public NexusArtifact searchBySHA1(String sha1) throws NexusClientException, NexusConnectionException;
    
    public List<NexusArtifact> searchByGAV(NexusArtifact gav) throws NexusClientException, NexusConnectionException;
    
    public boolean isNexusStarted( boolean blocking) throws NexusClientException, NexusConnectionException;
    
    public void stopNexus() throws NexusClientException, NexusConnectionException;
    
    public void startNexus() throws NexusClientException, NexusConnectionException;
    
    public void restartNexus() throws NexusClientException, NexusConnectionException;
}
