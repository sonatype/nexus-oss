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
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.index.SearchType;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.tasks.descriptors.RepairIndexTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

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
            serviceURI.append( entry.getKey() ).append( "=" ).append( Reference.encode( entry.getValue() ) ).append(
                "&" );
        }

        if ( searchType != null )
        {
            // we have an override in place
            // currently, REST API lacks search type (it is able to only ovveride isKeyword search happening, or not, of
            // if
            // not specified, rely on server side defaults)
            if ( SearchType.EXACT.equals( searchType ) )
            {
                serviceURI.append( "exact=true&" );
            }
            else if ( SearchType.SCORED.equals( searchType ) )
            {
                serviceURI.append( "exact=false&" );
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

        Assert.assertTrue( response.getStatus().isSuccess(), "Search failure:\n" + responseText );

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
        return searchForGav( groupId, artifactId, version, null, repositoryId );
    }

    public List<NexusArtifact> searchForGav( String groupId, String artifactId, String version, String packaging,
                                             String repositoryId )
        throws IOException
    {
        return searchForGav( groupId, artifactId, version, packaging, null, repositoryId );
    }

    public List<NexusArtifact> searchForGav( String groupId, String artifactId, String version, String packaging,
                                             String classifier, String repositoryId )
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
        if ( StringUtils.isNotBlank( packaging ) )
        {
            args.put( "p", packaging );
        }
        if ( StringUtils.isNotBlank( classifier ) )
        {
            args.put( "c", classifier );
        }

        return doSearchFor( args, repositoryId, null );
    }

    public List<NexusArtifact> searchForGav( Gav gav, String repositoryId )
        throws IOException
    {
        return searchForGav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
            gav.getClassifier(), repositoryId );
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
            Assert.assertFalse( response.getStatus().isError(), "Unable do retrieve repository: " + repositoryName
                + "\n" + response.getStatus() );
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

    public void reindexRepository( String taskName, String repoId, boolean force )
        throws Exception
    {
        doReindex( taskName, repoId, force, false );
    }

    public void reindexGroup( String taskName, String groupId, boolean force )
        throws Exception
    {
        doReindex( taskName, groupId, force, true );
    }

    private void doReindex( String taskName, String repoId, boolean force, boolean group )
        throws Exception
    {
        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( taskName );
        if ( force )
        {
            scheduledTask.setTypeId( RepairIndexTaskDescriptor.ID );
        }
        else
        {
            scheduledTask.setTypeId( UpdateIndexTaskDescriptor.ID );
        }
        scheduledTask.setSchedule( "manual" );

        if ( repoId != null )
        {
            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setKey( UpdateIndexTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
                prop.setValue(  repoId );
            scheduledTask.addProperty( prop );
        }

        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertTrue( status.isSuccess() );

        status = TaskScheduleUtil.run( TaskScheduleUtil.getTask( taskName ).getId() );
        Assert.assertTrue( status.isSuccess() );
    }

    public ArtifactInfoResource getInfo( String repositoryId, String itemPath )
        throws IOException
    {
        Response res =
            RequestFacade.sendMessage( "content/repositories/" + repositoryId + "/" + itemPath + "?describe=info",
                Method.GET, new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML ) );

        String responseText = res.getEntity().getText();
        if ( !res.getStatus().isSuccess() )
        {
            Assert.fail( res.getStatus() + "\n" + responseText );
        }

        XStreamRepresentation rep =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );
        ArtifactInfoResourceResponse info =
            (ArtifactInfoResourceResponse) rep.getPayload( new ArtifactInfoResourceResponse() );

        return info.getData();
    }

    // ================================
    // Search NG

    /**
     * Main entry point used by other exposed methods. Do NOT expose this method, never ever.
     * 
     * @param queryArgs
     * @param repositoryId
     * @param asKeywords
     * @return
     * @throws Exception
     */
    private Response doNGSearchForR( Map<String, String> queryArgs, String repositoryId, SearchType searchType )
        throws IOException
    {
        StringBuffer serviceURI = new StringBuffer( "service/local/lucene/search?" );

        if ( StringUtils.isNotBlank( repositoryId ) )
        {
            serviceURI.append( "repositoryId=" ).append( repositoryId ).append( "&" );
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
                serviceURI.append( "exact=true&" );
            }
            else if ( SearchType.SCORED.equals( searchType ) )
            {
                serviceURI.append( "exact=false&" );
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
    private SearchNGResponse doNGSearchFor( Map<String, String> queryArgs, String repositoryId, SearchType searchType )
        throws IOException
    {
        Response response = doNGSearchForR( queryArgs, repositoryId, searchType );

        String responseText = response.getEntity().getText();

        Assert.assertTrue( response.getStatus().isSuccess(), "Search failure:\n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, responseText, MediaType.APPLICATION_XML );

        SearchNGResponse searchResponse = (SearchNGResponse) representation.getPayload( new SearchNGResponse() );

        return searchResponse;
    }

    // NG Keyword search

    public SearchNGResponse searchNGFor( String query )
        throws IOException
    {
        return searchNGFor( query, null, null );
    }

    public SearchNGResponse searchNGFor( String query, String repositoryId, SearchType type )
        throws IOException
    {
        HashMap<String, String> queryArgs = new HashMap<String, String>();

        queryArgs.put( "q", query );

        return doNGSearchFor( queryArgs, repositoryId, type );
    }

    public SearchNGResponse searchNGForGav( String groupId, String artifactId, String version, String classifier,
                                            String packaging )
        throws IOException
    {
        return searchNGForGav( groupId, artifactId, version, classifier, packaging, null, null );
    }

    public SearchNGResponse searchNGForGav( String groupId, String artifactId, String version, String classifier,
                                            String packaging, String repositoryId, SearchType type )
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
        if ( StringUtils.isNotBlank( classifier ) )
        {
            args.put( "c", classifier );
        }
        if ( StringUtils.isNotBlank( packaging ) )
        {
            args.put( "p", packaging );
        }

        return doNGSearchFor( args, repositoryId, type );
    }

    public SearchNGResponse searchNGForGav( Gav gav )
        throws IOException
    {
        return searchNGForGav( gav, null, null );
    }

    public SearchNGResponse searchNGForGav( Gav gav, String repositoryId, SearchType type )
        throws IOException
    {
        return searchNGForGav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getClassifier(),
            gav.getExtension(), repositoryId, type );
    }

    public SearchNGResponse searchSha1NGFor( String sha1 )
        throws IOException
    {
        return searchSha1NGFor( sha1, null, null );
    }

    public SearchNGResponse searchSha1NGFor( String sha1, String repositoryId, SearchType type )
        throws IOException
    {
        HashMap<String, String> queryArgs = new HashMap<String, String>();

        queryArgs.put( "sha1", sha1 );

        return doNGSearchFor( queryArgs, repositoryId, type );
    }

    // =================
    // TreeView

    public IndexBrowserTreeViewResponseDTO indexBrowserTreeView( String repoId, String path )
        throws IOException
    {
        return indexBrowserTreeView( repoId, path, null, null, null );
    }

    public IndexBrowserTreeViewResponseDTO indexBrowserTreeView( String repoId, String groupIdHint,
                                                                 String artifactIdHint )
        throws IOException
    {
        return indexBrowserTreeView( repoId, null, groupIdHint, artifactIdHint, null );
    }

    public IndexBrowserTreeViewResponseDTO indexBrowserTreeView( String repoId, String path, String groupIdHint,
                                                                 String artifactIdHint, String versionIdHint )
        throws IOException
    {
        assert repoId != null : "Repository ID must not be null!";

        String serviceURI = "service/local/repositories/" + repoId + "/index_content/";

        if ( path != null )
        {
            // trim off leading "/" if any
            while ( path.length() > 0 && path.startsWith( "/" ) )
            {
                path = path.substring( 1 );
            }

            serviceURI = serviceURI + path;

            if ( !serviceURI.endsWith( "/" ) )
            {
                serviceURI = serviceURI + "/";
            }
        }

        serviceURI = serviceURI + "?";

        if ( StringUtils.isNotBlank( groupIdHint ) )
        {
            serviceURI = serviceURI + "groupIdHint=" + groupIdHint + "&";
        }
        if ( StringUtils.isNotBlank( artifactIdHint ) )
        {
            serviceURI = serviceURI + "artifactIdHint=" + artifactIdHint + "&";
        }
        if ( StringUtils.isNotBlank( versionIdHint ) )
        {
            serviceURI = serviceURI + "versionHint=" + versionIdHint + "&";
        }

        Response response = RequestFacade.doGetRequest( serviceURI );

        String responseText = response.getEntity().getText();

        Status status = response.getStatus();

        Assert.assertTrue( status.isSuccess(), responseText + status );

        XStreamRepresentation re =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        IndexBrowserTreeViewResponseDTO resourceResponse =
            (IndexBrowserTreeViewResponseDTO) re.getPayload( new IndexBrowserTreeViewResponseDTO() );

        return resourceResponse;
    }

}
