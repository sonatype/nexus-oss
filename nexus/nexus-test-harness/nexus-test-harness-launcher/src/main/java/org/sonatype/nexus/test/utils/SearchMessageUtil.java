/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class SearchMessageUtil
    extends ITUtil
{
    private static Logger log = LoggerFactory.getLogger( SearchMessageUtil.class );

    private XStream xstream;

    public SearchMessageUtil( AbstractNexusIntegrationTest test )
    {
        super( test );

        this.xstream = XStreamFactory.getXmlXStream();
    }

    /**
     * Main entry point used by other exposed methods. Do NOT expose this method, never ever.
     * 
     * @param queryArgs
     * @param repositoryId
     * @param asKeywords
     * @return
     * @throws Exception
     */
    private Response doSearchForR( Map<String, String> queryArgs, String repositoryId, SearchType searchType )
        throws IOException
    {
        StringBuffer serviceURI = null;

        if ( repositoryId == null )
        {
            serviceURI = new StringBuffer( "service/local/data_index?" );
        }
        else
        {
            serviceURI = new StringBuffer( "service/local/data_index/repositories/" + repositoryId + "?" );
        }

        for ( Entry<String, String> entry : queryArgs.entrySet() )
        {
            serviceURI.append( entry.getKey() ).append( "=" ).append( entry.getValue() ).append( "&" );
        }

        if ( searchType != null )
        {
            // we have an override in place
            // currently, REST API lacks search type (it is able to only ovveride isKeyword search happening, or not, of
            // if
            // not specified, rely on server side defaults)
            if ( SearchType.EXACT.equals( searchType ) )
            {
                serviceURI.append( "asKeywords=true&" );
            }
            else if ( SearchType.SCORED.equals( searchType ) )
            {
                serviceURI.append( "asKeywords=false&" );
            }
        }

        log.info( "Search serviceURI " + serviceURI );

        return RequestFacade.doGetRequest( serviceURI.toString() );
    }

    /**
     * Uses XStream to unmarshall the DTOs.
     * 
     * @param queryArgs
     * @param repositoryId
     * @param asKeywords
     * @return
     * @throws IOException
     */
    private List<NexusArtifact> doSearchFor( Map<String, String> queryArgs, String repositoryId, SearchType searchType )
        throws IOException
    {
        Response response = doSearchForR( queryArgs, repositoryId, searchType );

        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Search failure:\n" + responseText, response.getStatus().isSuccess() );

        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, responseText, MediaType.APPLICATION_XML );

        SearchResponse searchResponde = (SearchResponse) representation.getPayload( new SearchResponse() );

        return searchResponde.getData();
    }

    // LOW LEVEL METHODS

    public List<NexusArtifact> searchFor( Map<String, String> queryArgs )
        throws IOException
    {
        return searchFor( queryArgs, null, null );
    }

    public List<NexusArtifact> searchFor( Map<String, String> queryArgs, String repositoryId )
        throws IOException
    {
        return searchFor( queryArgs, repositoryId, null );
    }

    public List<NexusArtifact> searchFor( Map<String, String> queryArgs, String repositoryId, SearchType searchType )
        throws IOException
    {
        return doSearchFor( queryArgs, repositoryId, searchType );
    }

    // QUICK ("simple" query)

    /**
     * Returns "low" Restlet response to access response HTTP Code.
     */
    public Response searchFor_response( String query )
        throws IOException
    {
        HashMap<String, String> queryArgs = new HashMap<String, String>();

        queryArgs.put( "q", query );

        return doSearchForR( queryArgs, null, null );
    }

    public List<NexusArtifact> searchFor( String query )
        throws IOException
    {
        return searchFor( query, null );
    }

    public List<NexusArtifact> searchFor( String query, SearchType type )
        throws IOException
    {
        HashMap<String, String> queryArgs = new HashMap<String, String>();

        queryArgs.put( "q", query );

        return searchFor( queryArgs, null, type );
    }

    public List<NexusArtifact> searchFor( String query, String repositoryId, SearchType type )
        throws IOException
    {
        HashMap<String, String> queryArgs = new HashMap<String, String>();

        queryArgs.put( "q", query );

        return searchFor( queryArgs, repositoryId, type );
    }

    // GAV

    public List<NexusArtifact> searchForGav( String groupId, String artifactId, String version )
        throws IOException
    {
        return searchForGav( groupId, artifactId, version, null );
    }

    public List<NexusArtifact> searchForGav( String groupId, String artifactId, String version, String repositoryId )
        throws IOException
    {
        Map<String, String> args = new HashMap<String, String>();

        if ( StringUtils.isNotBlank( groupId ) )
        {
            args.put( "g", groupId );
        }
        if ( StringUtils.isNotBlank( artifactId ) )
        {
            args.put( "a", artifactId );
        }
        if ( StringUtils.isNotBlank( version ) )
        {
            args.put( "v", version );
        }

        return doSearchFor( args, repositoryId, null );
    }

    public List<NexusArtifact> searchForGav( Gav gav, String repositoryId )
        throws IOException
    {
        return searchForGav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), repositoryId );
    }

    // CLASSNAME

    public List<NexusArtifact> searchForClassname( String classname )
        throws IOException
    {
        Map<String, String> args = new HashMap<String, String>();

        args.put( "cn", classname );

        return doSearchFor( args, null, null );
    }

    // IDENTIFY/SHA1

    public NexusArtifact identify( String sha1 )
        throws IOException
    {
        // GET /identify/sha1/8b1b85d04eea979c33109ea42808b7d3f6d355ab (is log4j:log4j:1.2.13)

        Response response = RequestFacade.doGetRequest( "service/local/identify/sha1/" + sha1 );

        if ( response.getStatus().isSuccess() )
        {
            XStreamRepresentation representation =
                new XStreamRepresentation( xstream, response.getEntity().getText(), MediaType.APPLICATION_XML );

            return (NexusArtifact) representation.getPayload( new NexusArtifact() );
        }
        else
        {
            return null;
        }
    }

    // SWITCHES ALLOW*

    public void allowBrowsing( String repositoryName, boolean allowBrowsing )
        throws IOException
    {
        RepositoryResource repository = getRepository( repositoryName );

        repository.setBrowseable( allowBrowsing );

        saveRepository( repository, repositoryName );
    }

    public void allowSearch( String repositoryName, boolean allowSearch )
        throws IOException
    {
        RepositoryResource repository = getRepository( repositoryName );

        repository.setIndexable( allowSearch );

        saveRepository( repository, repositoryName );
    }

    public void allowDeploying( String repositoryName, boolean allowDeploying )
        throws IOException
    {
        RepositoryResource repository = getRepository( repositoryName );

        if ( allowDeploying )
        {
            repository.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        }
        else
        {
            repository.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        }

        saveRepository( repository, repositoryName );
    }

    // PRIVATE BELOW

    private RepositoryResource getRepository( String repositoryName )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repositoryName;
        final Response response = RequestFacade.doGetRequest( serviceURI );

        if ( response.getStatus().isError() )
        {
            Assert.assertFalse( "Unable do retrieve repository: " + repositoryName + "\n" + response.getStatus(),
                response.getStatus().isError() );
        }
        String responseText = response.getEntity().getText();

        RepositoryResourceResponse repository = (RepositoryResourceResponse) xstream.fromXML( responseText );
        return (RepositoryResource) repository.getData();
    }

    private void saveRepository( RepositoryResource repository, String repositoryName )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repositoryName;

        RepositoryResourceResponse repositoryResponse = new RepositoryResourceResponse();
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        repositoryResponse.setData( repository );
        representation.setPayload( repositoryResponse );

        Status status = RequestFacade.sendMessage( serviceURI, Method.PUT, representation ).getStatus();
        Assert.assertEquals( Status.SUCCESS_OK.getCode(), status.getCode() );

    }

}
