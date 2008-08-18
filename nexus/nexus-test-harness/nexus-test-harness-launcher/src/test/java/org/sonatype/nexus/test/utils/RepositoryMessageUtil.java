package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RepositoryMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    public RepositoryMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RepositoryResource createRepository( RepositoryResource repo )
        throws IOException
    {

        Response response = this.sendMessage( Method.POST, repo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create Repository: " + response.getStatus() + ":\n" + responseText );
        }

        // // get the Resource object
        // RepositoryResource responseResource = this.getResourceFromResponse( response );

        // currently create doesn't return anything, it should see NEXUS-540
        // the work around is to call get at this point
        RepositoryResource responseResource = this.getRepository( repo.getId() ); // GET always uses XML, due to a
        // problem in the RESTlet client

        this.validateResourceResponse( repo, responseResource );

        return responseResource;
    }

    public void validateResourceResponse( RepositoryResource repo, RepositoryResource responseResource )
        throws IOException
    {
        Assert.assertEquals( repo.getId(), responseResource.getId() );
        Assert.assertEquals( repo.getChecksumPolicy(), responseResource.getChecksumPolicy() );
        Assert.assertEquals( repo.getName(), responseResource.getName() );
        // Assert.assertEquals( repo.getDefaultLocalStorageUrl(), responseResource.getDefaultLocalStorageUrl() ); //
        // TODO: add check for this

        // TODO: sometimes the storage dir ends with a '/' SEE: NEXUS-542
        if ( responseResource.getDefaultLocalStorageUrl().endsWith( "/" ) )
        {
            Assert.assertTrue( "Unexpected defaultLocalStorage: <expected to end with> " + "runtime/work/storage/"
                + repo.getId() + "/  <actual>" + responseResource.getDefaultLocalStorageUrl(),
                               responseResource.getDefaultLocalStorageUrl().endsWith(
                                                                                      "runtime/work/storage/"
                                                                                          + repo.getId() + "/" ) );
        }
        // NOTE one of these blocks should be removed
        else
        {
            Assert.assertTrue( "Unexpected defaultLocalStorage: <expected to end with> " + "runtime/work/storage/"
                + repo.getId() + "  <actual>" + responseResource.getDefaultLocalStorageUrl(),
                               responseResource.getDefaultLocalStorageUrl().endsWith(
                                                                                      "runtime/work/storage/"
                                                                                          + repo.getId() ) );
        }
        Assert.assertEquals( repo.getFormat(), responseResource.getFormat() );
        Assert.assertEquals( repo.getNotFoundCacheTTL(), responseResource.getNotFoundCacheTTL() );
        Assert.assertEquals( repo.getOverrideLocalStorageUrl(), responseResource.getOverrideLocalStorageUrl() );
        Assert.assertEquals( repo.getRemoteStorage(), responseResource.getRemoteStorage() );
        Assert.assertEquals( repo.getRepoPolicy(), responseResource.getRepoPolicy() );
        Assert.assertEquals( repo.getRepoType(), responseResource.getRepoType() );

        // check nexus.xml
        this.validateRepoInNexusConfig( responseResource );
    }

    public RepositoryResource getRepository( String repoId )
        throws IOException
    {

        String responseText =
            RequestFacade.doGetRequest( "service/local/repositories/" + repoId ).getEntity().getText();
        System.out.println( "responseText: \n" + responseText );

        // this should use call to: getResourceFromResponse
        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryResourceResponse resourceResponse =
            (RepositoryResourceResponse) representation.getPayload( new RepositoryResourceResponse() );

        return (RepositoryResource) resourceResponse.getData();
    }

    public RepositoryResource updateRepo( RepositoryResource repo )
        throws IOException
    {
        Response response = this.sendMessage( Method.PUT, repo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update user: " + response.getStatus() + "\n" + responseText );
        }

        // this doesn't return any objects, it should....
        // // get the Resource object
        // RepositoryResource responseResource = this.getResourceFromResponse( response );

        // for now call GET
        RepositoryResource responseResource = this.getRepository( repo.getId() );

        this.validateResourceResponse( repo, responseResource );

        return responseResource;
    }

    public Response sendMessage( Method method, RepositoryBaseResource resource )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String userId = ( method == Method.POST ) ? "" : "/" + resource.getId();

        String serviceURI = "service/local/repositories" + userId;

        RepositoryResourceResponse repoResponseRequest = new RepositoryResourceResponse();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        System.out.println( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<RepositoryListResource> getList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( "service/local/repositories" ).getEntity().getText();
        System.out.println( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        
        RepositoryListResourceResponse resourceResponse =
            (RepositoryListResourceResponse) representation.getPayload( new RepositoryListResourceResponse() );

        return resourceResponse.getData();

    }

    public RepositoryResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        RepositoryResourceResponse resourceResponse =
            (RepositoryResourceResponse) representation.getPayload( new RepositoryResourceResponse() );

        return (RepositoryResource) resourceResponse.getData();
    }

    private void validateRepoInNexusConfig( RepositoryResource repo )
        throws IOException
    {
        CRepository cRepo = NexusConfigUtil.getRepo( repo.getId() );

        Assert.assertEquals( repo.getId(), cRepo.getId() );
        Assert.assertEquals( repo.getChecksumPolicy(), cRepo.getChecksumPolicy() );
        Assert.assertEquals( repo.getName(), cRepo.getName() );
        Assert.assertEquals( repo.getFormat(), cRepo.getType() );
        Assert.assertEquals( repo.getNotFoundCacheTTL(), cRepo.getNotFoundCacheTTL() );
        Assert.assertEquals( repo.getOverrideLocalStorageUrl(), cRepo.getLocalStorage() );
        Assert.assertEquals( repo.getRemoteStorage(), cRepo.getRemoteStorage() );
        Assert.assertEquals( repo.getRepoPolicy(), cRepo.getRepositoryPolicy() );

        // TODO: allow for shadow repos
        // Assert.assertEquals( repo.getRepoType(), cRepo.getType() );

    }

}
