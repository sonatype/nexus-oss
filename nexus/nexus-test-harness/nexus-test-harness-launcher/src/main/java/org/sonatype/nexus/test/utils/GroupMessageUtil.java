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
package org.sonatype.nexus.test.utils;

import static org.hamcrest.MatcherAssert.*;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class GroupMessageUtil
    extends ITUtil
{
    public static final String SERVICE_PART = "service/local/repo_groups";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = LoggerFactory.getLogger( GroupMessageUtil.class );

    private final RepositoryGroupNexusRestClient groupNRC;

    public GroupMessageUtil( AbstractNexusIntegrationTest test, XStream xstream, MediaType mediaType )
    {
        super( test );
        groupNRC = new RepositoryGroupNexusRestClient(
            RequestFacade.getNexusRestClient(),
            new NexusTasksRestClient( RequestFacade.getNexusRestClient() ),
            test.getEventInspectorsUtil(),
            xstream,
            mediaType
        );
    }

    public RepositoryGroupResource createGroup( RepositoryGroupResource group )
        throws IOException
    {
        RepositoryGroupResource responseResource = groupNRC.createGroup( group );

        validateResourceResponse( group, responseResource );

        return responseResource;
    }

    public void validateResourceResponse( RepositoryGroupResource expected, RepositoryGroupResource actual )
        throws IOException
    {
        Assert.assertEquals( expected.getId(), actual.getId() );
        Assert.assertEquals( expected.getName(), actual.getName() );
        Assert.assertEquals( expected.getFormat(), actual.getFormat() );

        LOG.debug( "group repos: " + expected.getRepositories() );
        LOG.debug( "other repos: " + actual.getRepositories() );

        validateRepoLists( expected.getRepositories(), actual.getRepositories() );

        // check nexus.xml
        this.validateRepoInNexusConfig( actual );
    }

    /**
     * @param expected
     * @param actual a list of RepositoryGroupMemberRepository, or a list of repo Ids.
     */
    public void validateRepoLists( List<RepositoryGroupMemberRepository> expected, List<?> actual )
    {

        Assert.assertEquals( actual.size(), expected.size(), "Size of groups repository list, \nexpected: " + this.repoListToStringList( expected )
                + "\nactual: " + this.repoListToStringList( actual ) + "\n" );

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

            Assert.assertEquals( actualRepoId, expectedRepo.getId(), "Repo Id:" );
        }
    }

    private List<String> repoListToStringList( List<?> repos )
    {
        // convert actual list to strings( if not already )
        List<String> repoIdList = new ArrayList<String>();
        for ( Object tmpObj : repos )
        {
            if ( tmpObj instanceof RepositoryGroupMemberRepository )
            {
                RepositoryGroupMemberRepository actualRepo = (RepositoryGroupMemberRepository) tmpObj;
                repoIdList.add( actualRepo.getId() );
            }
            else
            {
                // expected string.
                repoIdList.add( tmpObj.toString() );
            }
        }
        return repoIdList;
    }

    public RepositoryGroupResource getGroup( String groupId )
        throws IOException
    {

        String responseText = RequestFacade.doGetForText( SERVICE_PART + "/" + groupId );
        LOG.debug( "responseText: \n" + responseText );

        // this should use call to: getResourceFromResponse
        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryGroupResourceResponse resourceResponse =
            (RepositoryGroupResourceResponse) representation.getPayload( new RepositoryGroupResourceResponse() );

        return resourceResponse.getData();
    }

    public RepositoryGroupResource updateGroup( RepositoryGroupResource group )
        throws IOException
    {
        RepositoryGroupResource responseResource = groupNRC.updateGroup( group );

        this.validateResourceResponse( group, responseResource );

        return responseResource;
    }

    /**
     * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
     */
    public Response sendMessage( Method method, RepositoryGroupResource resource )
        throws IOException
    {
        return this.sendMessage( method, resource, resource.getId() );
    }

    /**
     * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
     */
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
    public List<RepositoryGroupListResource> getList()
        throws IOException
    {

        String responseText = RequestFacade.doGetForText( SERVICE_PART);
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

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

        return resourceResponse.getData();
    }

    private void validateRepoInNexusConfig( RepositoryGroupResource group )
        throws IOException
    {
        CRepository cGroup = getTest().getNexusConfigUtil().getRepo( group.getId() );

        Assert.assertEquals( group.getId(), cGroup.getId() );
        Assert.assertEquals( group.getName(), cGroup.getName() );

        List<RepositoryGroupMemberRepository> expectedRepos = group.getRepositories();
        List<String> actualRepos = getTest().getNexusConfigUtil().getGroup( group.getId() ).getMemberRepositoryIds();

        this.validateRepoLists( expectedRepos, actualRepos );
    }

}
