/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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
