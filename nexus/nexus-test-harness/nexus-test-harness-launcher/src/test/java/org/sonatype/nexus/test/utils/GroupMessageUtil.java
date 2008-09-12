package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class GroupMessageUtil
{
    private static final String SERVICE_PART = "service/local/repo_groups";

    private XStream xstream;

    private MediaType mediaType;
    
    private static final Logger LOG = Logger.getLogger( GroupMessageUtil.class );

    public GroupMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RepositoryGroupResource createGroup( RepositoryGroupResource group )
        throws IOException
    {

        Response response = this.sendMessage( Method.POST, group );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create Repository: " + response.getStatus() + ":\n" + responseText );
        }

        // // get the Resource object
        // RepositoryGroupResource responseResource = this.getResourceFromResponse( response );

        // currently create doesn't return anything, it should see NEXUS-540
        // the work around is to call get at this point
        RepositoryGroupResource responseResource = this.getGroup( group.getId() ); // GET always uses XML, due to a
        // problem in the RESTlet client

        this.validateResourceResponse( group, responseResource );

        return responseResource;
    }

    public void validateResourceResponse( RepositoryGroupResource group, RepositoryGroupResource responseResource )
        throws IOException
    {
        Assert.assertEquals( group.getId(), responseResource.getId() );
        Assert.assertEquals( group.getName(), responseResource.getName() );
        Assert.assertEquals( group.getFormat(), responseResource.getFormat() );

        LOG.debug( "group repos: " + group.getRepositories() );
        LOG.debug( "other repos: " + responseResource.getRepositories() );

        validateRepoLists( group.getRepositories(), responseResource.getRepositories() );

        // check nexus.xml
        this.validateRepoInNexusConfig( responseResource );
    }

    /**
     * @param expected
     * @param actual a list of RepositoryGroupMemberRepository, or a list of repo Ids.
     */
    public void validateRepoLists( List<RepositoryGroupMemberRepository> expected, List actual )
    {
        Assert.assertEquals( "Size of groups repository list:", expected.size(), actual.size() );

        for ( int ii = 0; ii < expected.size(); ii++ )
        {
            RepositoryGroupMemberRepository expectedRepo = expected.get( ii );
            String actualRepoId = null;
            Object tmpObj = actual.get( ii );
            if ( tmpObj instanceof RepositoryGroupMemberRepository )
            {
                RepositoryGroupMemberRepository actualRepo = (RepositoryGroupMemberRepository) tmpObj;
                actualRepoId = actualRepo.getId();
            }
            else
            {
                // expected string.
                actualRepoId = tmpObj.toString();
            }

            Assert.assertEquals( "Repo Id:", expectedRepo.getId(), actualRepoId );
        }
    }

    public RepositoryGroupResource getGroup( String repoId )
        throws IOException
    {

        String responseText = RequestFacade.doGetRequest( SERVICE_PART + "/" + repoId ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        // this should use call to: getResourceFromResponse
        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamInitializer.initialize( new XStream() ), responseText, MediaType.APPLICATION_XML );

        RepositoryGroupResourceResponse resourceResponse =
            (RepositoryGroupResourceResponse) representation.getPayload( new RepositoryGroupResourceResponse() );

        return (RepositoryGroupResource) resourceResponse.getData();
    }

    public RepositoryGroupResource updateGroup( RepositoryGroupResource group )
        throws IOException
    {
        Response response = this.sendMessage( Method.PUT, group );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update user: " + response.getStatus() + "\n" + responseText );
        }

        // this doesn't return any objects, it should....
        // // get the Resource object
        // RepositoryGroupResource responseResource = this.getResourceFromResponse( response );

        // for now call GET
        RepositoryGroupResource responseResource = this.getGroup( group.getId() );

        this.validateResourceResponse( group, responseResource );

        return responseResource;
    }

    public Response sendMessage( Method method, RepositoryGroupResource resource )
        throws IOException
    {
        return this.sendMessage( method, resource, resource.getId() );
    }
    
    public Response sendMessage( Method method, RepositoryGroupResource resource, String id )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String idPart = ( method == Method.POST ) ? "" : "/" + id;
        String serviceURI = SERVICE_PART + idPart;

        RepositoryGroupResourceResponse repoResponseRequest = new RepositoryGroupResourceResponse();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        LOG.debug( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<RepositoryGroupListResource> getList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( SERVICE_PART ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamInitializer.initialize( new XStream() ), responseText, MediaType.APPLICATION_XML );

        RepositoryGroupListResourceResponse resourceResponse =
            (RepositoryGroupListResourceResponse) representation.getPayload( new RepositoryGroupListResourceResponse() );

        return resourceResponse.getData();

    }

    public RepositoryGroupResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        RepositoryGroupResourceResponse resourceResponse =
            (RepositoryGroupResourceResponse) representation.getPayload( new RepositoryGroupResourceResponse() );

        return (RepositoryGroupResource) resourceResponse.getData();
    }

    @SuppressWarnings("unchecked")
    private void validateRepoInNexusConfig( RepositoryGroupResource group )
        throws IOException
    {
        CRepositoryGroup cGroup = NexusConfigUtil.getGroup( group.getId() );

        Assert.assertEquals( group.getId(), cGroup.getGroupId() );
        Assert.assertEquals( group.getName(), cGroup.getName() );

        List expectedRepos = group.getRepositories();
        List actualRepos = cGroup.getRepositories();

        this.validateRepoLists( expectedRepos, actualRepos );
    }

}
