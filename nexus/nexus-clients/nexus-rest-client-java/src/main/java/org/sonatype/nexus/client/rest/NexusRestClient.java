package org.sonatype.nexus.client.rest;

import java.util.List;

import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;

public class NexusRestClient
    implements NexusClient
{

    private static final String REPO_SERVICE = "repositories";

    private RestClientHelper clientHelper = null;

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

        // expecting an instance of RepositoryResourceResponse
        if ( tempObj instanceof RepositoryResourceResponse )
        {
            RepositoryResourceResponse repoResponse = (RepositoryResourceResponse) tempObj;
            return repoResponse.getData();
        }
        else
        {
            throw new NexusClientException(
                                            "Response from server returned an unexpected object.  Expected: RepositoryResourceResponse, actual: "
                                                + tempObj.getClass() );
        }
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

        // expecting an instance of RepositoryResourceResponse
        if ( tempObj instanceof RepositoryResourceResponse )
        {
            RepositoryResourceResponse repoResponse = (RepositoryResourceResponse) tempObj;
            return repoResponse.getData();
        }
        else
        {
            throw new NexusClientException(
                                            "Response from server returned an unexpected object.  Expected: RepositoryResourceResponse, actual: "
                                                + tempObj.getClass() );
        }
    }

    @SuppressWarnings( "unchecked" )
    public List<RepositoryListResource> getRespositories()
        throws NexusConnectionException, NexusClientException
    {
        Object tempObj = this.getClientHelper().getList( REPO_SERVICE );

        // expecting an instance of RepositoryResourceResponse
        if ( tempObj instanceof RepositoryListResourceResponse )
        {
            RepositoryListResourceResponse repoResponse = (RepositoryListResourceResponse) tempObj;
            return repoResponse.getData();
        }
        else
        {
            throw new NexusClientException(
                                            "Response from server returned an unexpected object.  Expected: RepositoryListResourceResponse, actual: "
                                                + tempObj.getClass() );
        }
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

        // expecting an instance of RepositoryResourceResponse
        if ( tempObj instanceof RepositoryResourceResponse )
        {
            RepositoryResourceResponse repoResponse = (RepositoryResourceResponse) tempObj;
            return repoResponse.getData();
        }
        else
        {
            throw new NexusClientException(
                                            "Response from server returned an unexpected object.  Expected: RepositoryResourceResponse, actual: "
                                                + tempObj.getClass() );
        }
    }

    public boolean isValidRepository( String id )
        throws NexusClientException, NexusConnectionException
    {

        List<RepositoryListResource> repoList = this.getRespositories();
        
        for ( RepositoryListResource repositoryListResource : repoList )
        {
            if( repositoryListResource.getId() != null && repositoryListResource.getId().equals( id ))
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

            // expecting an instance of NexusArtifact
            if ( tempObj instanceof NexusArtifact )
            {
                return (NexusArtifact) tempObj;
            }
            else
            {
                throw new NexusClientException(
                                                "Response from server returned an unexpected object.  Expected: NexusArtifact, actual: "
                                                    + tempObj.getClass() );
            }
        }
        return null;

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
}
