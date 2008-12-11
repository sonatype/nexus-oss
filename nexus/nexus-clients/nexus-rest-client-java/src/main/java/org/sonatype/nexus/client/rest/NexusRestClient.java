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
package org.sonatype.nexus.client.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

/**
 * @plexus.component instantiation-strategy="per-lookup" role="org.sonatype.nexus.client.NexusClient"
 */
public class NexusRestClient
    implements NexusClient
{

    private static final String REPO_SERVICE = "repositories";

    private static final String STATE_SERVICE = "status";

    private RestClientHelper clientHelper = null;

    private Logger logger = Logger.getLogger( getClass() );

    public static final String WAIT_FOR_START_TIMEOUT_KEY = "nexus.client.started.timeout";

    public void connect( String baseUrl, String username, String password )
    {
        this.clientHelper = new RestClientHelper( baseUrl, username, password );
    }

    public void disconnect()
        throws NexusConnectionException, NexusClientException
    {
        this.clientHelper = null;
    }

    public RepositoryBaseResource createRepository( RepositoryBaseResource repo )
        throws NexusConnectionException, NexusClientException
    {
        RepositoryResourceResponse repoResponseRequest = new RepositoryResourceResponse();
        repoResponseRequest.setData( repo );

        Object tempObj = this.getClientHelper().create( REPO_SERVICE, null, repoResponseRequest );

        // Hack around NEXUS-540
        if ( tempObj == null )
        {
            return this.getRepository( repo.getId() );
        }

        // type check the object so we can have a meaninful error if needed
        this.checkType( tempObj, RepositoryResourceResponse.class );

        RepositoryResourceResponse repoResponse = (RepositoryResourceResponse) tempObj;
        return repoResponse.getData();

    }

    public void deleteRepository( String id )
        throws NexusConnectionException, NexusClientException
    {
        this.getClientHelper().delete( REPO_SERVICE, id );
    }

    public RepositoryBaseResource getRepository( String id )
        throws NexusConnectionException, NexusClientException
    {
        Object tempObj = this.getClientHelper().get( REPO_SERVICE, id );

        // type check the object so we can have a meaninful error if needed
        this.checkType( tempObj, RepositoryResourceResponse.class );

        RepositoryResourceResponse repoResponse = (RepositoryResourceResponse) tempObj;
        return repoResponse.getData();
    }

    @SuppressWarnings( "unchecked" )
    public List<RepositoryListResource> getRespositories()
        throws NexusConnectionException, NexusClientException
    {
        Object tempObj = this.getClientHelper().getList( REPO_SERVICE );

        // type check the object so we can have a meaninful error if needed
        this.checkType( tempObj, RepositoryListResourceResponse.class );
        RepositoryListResourceResponse repoResponse = (RepositoryListResourceResponse) tempObj;
        return repoResponse.getData();

    }

    public RepositoryBaseResource updateRepository( RepositoryBaseResource repo )
        throws NexusConnectionException, NexusClientException
    {
        RepositoryResourceResponse repoResponseRequest = new RepositoryResourceResponse();
        repoResponseRequest.setData( repo );

        Object tempObj = this.getClientHelper().update( REPO_SERVICE, repo.getId(), repoResponseRequest );

        // Hack around NEXUS-540
        if ( tempObj == null )
        {
            return this.getRepository( repo.getId() );
        }

        // type check the object so we can have a meaninful error if needed
        this.checkType( tempObj, RepositoryResourceResponse.class );

        RepositoryResourceResponse repoResponse = (RepositoryResourceResponse) tempObj;
        return repoResponse.getData();
    }

    public boolean isValidRepository( String id )
        throws NexusClientException, NexusConnectionException
    {

        List<RepositoryListResource> repoList = this.getRespositories();

        for ( RepositoryListResource repositoryListResource : repoList )
        {
            if ( repositoryListResource.getId() != null && repositoryListResource.getId().equals( id ) )
            {
                return true;
            }
        }

        return false;
    }

    public NexusArtifact searchBySHA1( String sha1 )
        throws NexusClientException, NexusConnectionException
    {
        Object tempObj = this.getClientHelper().get( "identify/sha1", sha1 );

        if ( tempObj != null )
        {
            // type check the object so we can have a meaninful error if needed
            this.checkType( tempObj, NexusArtifact.class );
            return (NexusArtifact) tempObj;
        }
        return null;

    }

    @SuppressWarnings( "unchecked" )
    public List<NexusArtifact> searchByGAV( NexusArtifact gav )
        throws NexusClientException, NexusConnectionException
    {
        Map<String, String> params = new HashMap<String, String>();
        // build the url params
        // group
        if ( StringUtils.isNotEmpty( gav.getGroupId() ) )
        {
            params.put( "g", gav.getGroupId() );
        }
        // artifact
        if ( StringUtils.isNotEmpty( gav.getArtifactId() ) )
        {
            params.put( "a", gav.getArtifactId() );
        }
        // version
        if ( StringUtils.isNotEmpty( gav.getVersion() ) )
        {
            params.put( "v", gav.getVersion() );
        }
        // classifier
        if ( StringUtils.isNotEmpty( gav.getClassifier() ) )
        {
            params.put( "c", gav.getClassifier() );
        }
        // packaging
        if ( StringUtils.isNotEmpty( gav.getPackaging() ) )
        {
            params.put( "p", gav.getPackaging() );
        }

        Object tempObj = this.getClientHelper().get( "data_index", params );

        if ( tempObj != null )
        {
            // type check the object so we can have a meaninful error if needed
            this.checkType( tempObj, SearchResponse.class );
            return ( (SearchResponse) tempObj ).getData();

        }
        return null;

    }

    public boolean isNexusStarted( boolean blocking )
        throws NexusClientException, NexusConnectionException
    {
        return blocking ? this.waitforNexusToStart() : this.isNexusStarted();
    }

    public boolean waitforNexusToStart()
    {
        long timeout = 10000;
        String timeoutProp = System.getProperty( WAIT_FOR_START_TIMEOUT_KEY, Long.toString( timeout ) );
        try
        {
            timeout = Long.valueOf( timeoutProp );
        }
        catch ( NumberFormatException e )
        {
            logger.warn( "System property '" + WAIT_FOR_START_TIMEOUT_KEY + "' is not a number. defaulting to:  "
                + timeout );
        }

        long startTime = System.currentTimeMillis();

        // poll the service every 1/2 sec
        while ( System.currentTimeMillis() < ( startTime + timeout ) )
        {
            // its possible nexus just started, and is not ready to return the status information
            // so if we run into an error just log it, its possible to hit this error 20 times (default)
            try
            {
                if ( this.isNexusStarted() )
                {
                    return true;
                }
            }
            catch ( NexusClientException e )
            {
                this.logger.debug( "Error while waiting for nexus to start: " + e.getMessage(), e );
            }
            catch ( NexusConnectionException e )
            {
                this.logger.debug( "Error while waiting for nexus to start: " + e.getMessage(), e );
            }
            try
            {
                Thread.sleep( 500 ); // sleep for 1/2 second.
            }
            catch ( InterruptedException e )
            {
            }
        }
        return false;
    }

    public boolean isNexusStarted()
        throws NexusClientException, NexusConnectionException
    {
        Object tempObj = this.getClientHelper().get( STATE_SERVICE, (String) null );

        // StatusResourceResponse.getData ->StatusResource

        // type check the object so we can have a meaninful error if needed
        this.checkType( tempObj, StatusResourceResponse.class );

        // everything should be smooth sailing from here.
        return ( (StatusResourceResponse) tempObj ).getData().getState().endsWith( "STARTED" );
    }

    public void restartNexus()
        throws NexusClientException, NexusConnectionException
    {
        this.getClientHelper().sendCommand( STATE_SERVICE, "RESTART" );
    }

    public void startNexus()
        throws NexusClientException, NexusConnectionException
    {
        this.getClientHelper().sendCommand( STATE_SERVICE, "START" );
    }

    public void stopNexus()
        throws NexusClientException, NexusConnectionException
    {
        this.getClientHelper().sendCommand( STATE_SERVICE, "STOP" );
    }

    private RestClientHelper getClientHelper()
        throws NexusClientException
    {
        if ( this.clientHelper != null )
        {
            return this.clientHelper;
        }
        else
        {
            throw new NexusClientException( "Not connected to a Nexus instance." );
        }
    }

    /**
     * Used to add meaningful exceptions.
     * 
     * @throws NexusClientException
     */
    @SuppressWarnings( "unchecked" )
    private void checkType( Object obj, Class expectedType )
        throws NexusClientException
    {

        if ( !expectedType.isInstance( obj ) )
        {
            throw new NexusClientException( "Response from server returned an unexpected object.  Expected: "
                + expectedType + ", actual: " + obj.getClass() );
        }
    }
}
