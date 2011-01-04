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
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class RepositoryMessageUtil
    extends ITUtil
{
    public static final String ALL_SERVICE_PART = RequestFacade.SERVICE_LOCAL + "all_repositories";

    public static final String SERVICE_PART = RequestFacade.SERVICE_LOCAL + "repositories";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( RepositoryMessageUtil.class );

    public RepositoryMessageUtil( AbstractNexusIntegrationTest test, XStream xstream, MediaType mediaType )
    {
        super( test );
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RepositoryBaseResource createRepository( RepositoryBaseResource repo )
        throws IOException
    {
        return createRepository( repo, true );
    }

    public RepositoryBaseResource createRepository( RepositoryBaseResource repo, boolean validate )
        throws IOException
    {
        Response response = this.sendMessage( Method.POST, repo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create Repository: " + response.getStatus() + ":\n" + responseText );
        }

        RepositoryBaseResource responseResource = this.getRepositoryBaseResourceFromResponse( response );

        if ( validate )
        {
            this.validateResourceResponse( repo, responseResource );
        }

        return responseResource;
    }

    public void validateResourceResponse( RepositoryBaseResource repo, RepositoryBaseResource responseResource )
        throws IOException
    {
        Assert.assertEquals( repo.getId(), responseResource.getId() );
        Assert.assertEquals( repo.getName(), responseResource.getName() );
        // Assert.assertEquals( repo.getDefaultLocalStorageUrl(), responseResource.getDefaultLocalStorageUrl() ); //
        // TODO: add check for this

        // format is not used anymore, removing the check
        // Assert.assertEquals( repo.getFormat(), responseResource.getFormat() );
        Assert.assertEquals( repo.getRepoType(), responseResource.getRepoType() );

        if ( repo.getRepoType().equals( "virtual" ) )
        {
            // check mirror
            RepositoryShadowResource expected = (RepositoryShadowResource) repo;
            RepositoryShadowResource actual = (RepositoryShadowResource) responseResource;

            Assert.assertEquals( expected.getShadowOf(), actual.getShadowOf() );
        }
        else
        {
            RepositoryResource expected = (RepositoryResource) repo;
            RepositoryResource actual = (RepositoryResource) responseResource;

            // Assert.assertEquals( expected.getChecksumPolicy(), actual.getChecksumPolicy() );

            // TODO: sometimes the storage dir ends with a '/' SEE: NEXUS-542
            if ( actual.getDefaultLocalStorageUrl().endsWith( "/" ) )
            {
                Assert.assertTrue( actual.getDefaultLocalStorageUrl().endsWith( "/storage/" + repo.getId() + "/" ),
                    "Unexpected defaultLocalStorage: <expected to end with> " + "/storage/" + repo.getId()
                        + "/  <actual>" + actual.getDefaultLocalStorageUrl() );
            }
            // NOTE one of these blocks should be removed
            else
            {
                Assert.assertTrue( actual.getDefaultLocalStorageUrl().endsWith( "/storage/" + repo.getId() ),
                    "Unexpected defaultLocalStorage: <expected to end with> " + "/storage/" + repo.getId()
                        + "  <actual>" + actual.getDefaultLocalStorageUrl() );
            }

            Assert.assertEquals( expected.getNotFoundCacheTTL(), actual.getNotFoundCacheTTL() );
            // Assert.assertEquals( expected.getOverrideLocalStorageUrl(), actual.getOverrideLocalStorageUrl() );

            if ( expected.getRemoteStorage() == null )
            {
                Assert.assertNull( actual.getRemoteStorage() );
            }
            else
            {
                Assert.assertEquals( expected.getRemoteStorage().getRemoteStorageUrl(),
                    actual.getRemoteStorage().getRemoteStorageUrl() );
            }

            Assert.assertEquals( expected.getRepoPolicy(), actual.getRepoPolicy() );
        }

        // check nexus.xml
        this.validateRepoInNexusConfig( responseResource );
    }

    public RepositoryBaseResource getRepository( String repoId )
        throws IOException
    {

        Response response = RequestFacade.doGetRequest( SERVICE_PART + "/" + repoId );
        String responseText = response.getEntity().getText();
        if ( response.getStatus().isError() )
        {
            Assert.fail( "Error on request: " + response.getStatus() + "\n" + responseText );
        }

        LOG.debug( "responseText: \n" + responseText );

        // this should use call to: getResourceFromResponse
        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryResourceResponse resourceResponse =
            (RepositoryResourceResponse) representation.getPayload( new RepositoryResourceResponse() );

        return resourceResponse.getData();
    }

    public RepositoryBaseResource updateRepo( RepositoryBaseResource repo )
        throws IOException
    {
        return updateRepo( repo, true );
    }

    public RepositoryBaseResource updateRepo( RepositoryBaseResource repo, boolean validate )
        throws IOException
    {
        Response response = this.sendMessage( Method.PUT, repo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update user: " + response.getStatus() + "\n" + responseText );
        }

        RepositoryBaseResource responseResource = this.getRepositoryBaseResourceFromResponse( response );

        if ( validate )
        {
            this.validateResourceResponse( repo, responseResource );
        }

        return responseResource;
    }

    public Response sendMessage( Method method, RepositoryBaseResource resource, String id )
        throws IOException
    {
        if ( resource != null && resource.getProviderRole() == null )
        {
            if ( "virtual".equals( resource.getRepoType() ) )
            {
                resource.setProviderRole( ShadowRepository.class.getName() );
            }
            else
            {
                resource.setProviderRole( Repository.class.getName() );
            }
        }

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String idPart = ( method == Method.POST ) ? "" : "/" + id;

        String serviceURI = SERVICE_PART + idPart;

        RepositoryResourceResponse repoResponseRequest = new RepositoryResourceResponse();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        LOG.debug( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    public Response sendMessage( Method method, RepositoryBaseResource resource )
        throws IOException
    {
        return this.sendMessage( method, resource, resource.getId() );
    }

    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     * 
     * @return
     * @throws IOException
     */
    public List<RepositoryListResource> getList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( SERVICE_PART ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryListResourceResponse resourceResponse =
            (RepositoryListResourceResponse) representation.getPayload( new RepositoryListResourceResponse() );

        return resourceResponse.getData();

    }

    public List<RepositoryListResource> getAllList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( ALL_SERVICE_PART ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryListResourceResponse resourceResponse =
            (RepositoryListResourceResponse) representation.getPayload( new RepositoryListResourceResponse() );

        return resourceResponse.getData();
    }

    public RepositoryBaseResource getRepositoryBaseResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getRepositoryBaseResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        RepositoryResourceResponse resourceResponse =
            (RepositoryResourceResponse) representation.getPayload( new RepositoryResourceResponse() );

        return resourceResponse.getData();
    }

    public RepositoryResource getResourceFromResponse( Response response )
        throws IOException
    {
        return (RepositoryResource) getRepositoryBaseResourceFromResponse( response );
    }

    private void validateRepoInNexusConfig( RepositoryBaseResource repo )
        throws IOException
    {

        if ( repo.getRepoType().equals( "virtual" ) )
        {
            // check mirror
            RepositoryShadowResource expected = (RepositoryShadowResource) repo;
            CRepository cRepo = getTest().getNexusConfigUtil().getRepo( repo.getId() );
            M2LayoutedM1ShadowRepositoryConfiguration cShadowRepo =
                getTest().getNexusConfigUtil().getRepoShadow( repo.getId() );

            Assert.assertEquals( expected.getShadowOf(), cShadowRepo.getMasterRepositoryId() );
            Assert.assertEquals( expected.getId(), cRepo.getId() );
            Assert.assertEquals( expected.getName(), cRepo.getName() );

            // cstamas: This is nonsense, this starts in-process (HERE) of nexus internals while IT runs a nexus too,
            // and they start/try to use same FS resources!
            // ContentClass expectedCc =
            // repositoryTypeRegistry.getRepositoryContentClass( cRepo.getProviderRole(), cRepo.getProviderHint() );
            // Assert.assertNotNull( expectedCc,
            // "Unknown shadow repo type='" + cRepo.getProviderRole() + cRepo.getProviderHint()
            // + "'!" );
            // Assert.assertEquals( expected.getFormat(), expectedCc.getId() );
        }
        else
        {
            RepositoryResource expected = (RepositoryResource) repo;
            CRepository cRepo = getTest().getNexusConfigUtil().getRepo( repo.getId() );

            Assert.assertEquals( cRepo.getId(), expected.getId() );

            Assert.assertEquals( cRepo.getName(), expected.getName() );

            // cstamas: This is nonsense, this starts in-process (HERE) of nexus internals while IT runs a nexus too,
            // and they start/try to use same FS resources!
            // ContentClass expectedCc =
            // repositoryTypeRegistry.getRepositoryContentClass( cRepo.getProviderRole(), cRepo.getProviderHint() );
            // Assert.assertNotNull( expectedCc, "Unknown repo type='" + cRepo.getProviderRole() +
            // cRepo.getProviderHint()
            // + "'!" );
            // Assert.assertEquals( expected.getFormat(), expectedCc.getId() );

            Assert.assertEquals( expected.getNotFoundCacheTTL(), cRepo.getNotFoundCacheTTL() );

            if ( expected.getOverrideLocalStorageUrl() == null )
            {
                Assert.assertNull( cRepo.getLocalStorage().getUrl(),
                    "Expected CRepo localstorage url not be set, because it is the default." );
            }
            else
            {
                String actualLocalStorage =
                    cRepo.getLocalStorage().getUrl().endsWith( "/" ) ? cRepo.getLocalStorage().getUrl()
                        : cRepo.getLocalStorage().getUrl() + "/";
                String overridLocalStorage =
                    expected.getOverrideLocalStorageUrl().endsWith( "/" ) ? expected.getOverrideLocalStorageUrl()
                        : expected.getOverrideLocalStorageUrl() + "/";
                Assert.assertEquals( overridLocalStorage, actualLocalStorage );
            }

            if ( expected.getRemoteStorage() == null )
            {
                Assert.assertNull( cRepo.getRemoteStorage() );
            }
            else
            {
                Assert.assertEquals( expected.getRemoteStorage().getRemoteStorageUrl(),
                    cRepo.getRemoteStorage().getUrl() );
            }

            // check maven repo props (for not just check everything that is a Repository
            if ( expected.getProvider().matches( "maven[12]" ) )
            {
                M2RepositoryConfiguration cM2Repo = getTest().getNexusConfigUtil().getM2Repo( repo.getId() );

                if ( expected.getChecksumPolicy() != null )
                {
                    Assert.assertEquals( expected.getChecksumPolicy(), cM2Repo.getChecksumPolicy().name() );
                }

                Assert.assertEquals( expected.getRepoPolicy(), cM2Repo.getRepositoryPolicy().name() );
            }
        }

    }

    public static void updateIndexes( String... repositories )
        throws Exception
    {
        reindex( repositories, false );
    }

    private static void reindex( String[] repositories, boolean incremental )
        throws IOException, Exception
    {
        for ( String repo : repositories )
        {
            String serviceURI;
            if ( incremental )
            {
                serviceURI = "service/local/data_incremental_index/repositories/" + repo + "/content";
            }
            else
            {
                serviceURI = "service/local/data_index/repositories/" + repo + "/content";
            }

            Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
            Status status = response.getStatus();
            Assert.assertTrue( status.isSuccess(), "Fail to update " + repo + " repository index " + status );
        }

        // let s w8 a few time for indexes
        TaskScheduleUtil.waitForAllTasksToStop();
    }

    public static void updateIncrementalIndexes( String... repositories )
        throws Exception
    {
        reindex( repositories, true );
    }

    public RepositoryStatusResource getStatus( String repoId )
        throws IOException
    {
        return getStatus( repoId, false );
    }

    public RepositoryStatusResource getStatus( String repoId, boolean force )
        throws IOException
    {

        String uri = SERVICE_PART + "/" + repoId + "/status";

        if ( force )
        {
            uri = uri + "?forceCheck=true";
        }

        Response response = RequestFacade.sendMessage( uri, Method.GET );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), "Fail to getStatus for '" + repoId + "' repository" + status );

        XStreamRepresentation representation =
            new XStreamRepresentation( this.xstream, response.getEntity().getText(), MediaType.APPLICATION_XML );

        RepositoryStatusResourceResponse resourceResponse =
            (RepositoryStatusResourceResponse) representation.getPayload( new RepositoryStatusResourceResponse() );

        return resourceResponse.getData();
    }

    public void updateStatus( RepositoryStatusResource repoStatus )
        throws IOException
    {
        String uriPart = SERVICE_PART + "/" + repoStatus.getId() + "/status";

        XStreamRepresentation representation = new XStreamRepresentation( this.xstream, "", MediaType.APPLICATION_XML );
        RepositoryStatusResourceResponse resourceResponse = new RepositoryStatusResourceResponse();
        resourceResponse.setData( repoStatus );
        representation.setPayload( resourceResponse );

        Response response = RequestFacade.sendMessage( uriPart, Method.PUT, representation );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(),
            "Fail to update '" + repoStatus.getId() + "' repository status " + status + "\nResponse:\n"
                + response.getEntity().getText() + "\nrepresentation:\n" + representation.getText() );

    }

    /**
     * @param repoId
     * @return
     * @throws IOException
     * @deprecated This is half baked stuff
     */
    public static ContentListResourceResponse downloadRepoIndexContent( String repoId )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repoId + "/index_content/";

        Response response = RequestFacade.doGetRequest( serviceURI );
        String responseText = response.getEntity().getText();
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), responseText + status );

        XStreamRepresentation re =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );
        ContentListResourceResponse resourceResponse =
            (ContentListResourceResponse) re.getPayload( new ContentListResourceResponse() );

        return resourceResponse;
    }
}
