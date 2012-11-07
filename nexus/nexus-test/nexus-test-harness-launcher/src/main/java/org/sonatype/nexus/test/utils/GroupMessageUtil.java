/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

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
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class GroupMessageUtil
    extends ITUtil
{
    public static final String SERVICE_PART = "service/local/repo_groups";

    private static final Logger LOG = LoggerFactory.getLogger( GroupMessageUtil.class );

    private final RepositoryGroupsNexusRestClient groupNRC;

    public GroupMessageUtil( AbstractNexusIntegrationTest test, XStream xstream, MediaType mediaType )
    {
        super( test );
        groupNRC = new RepositoryGroupsNexusRestClient(
            RequestFacade.getNexusRestClient(),
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

    public RepositoryGroupResource updateGroup( RepositoryGroupResource group )
        throws IOException
    {
        RepositoryGroupResource responseResource = groupNRC.updateGroup( group );

        this.validateResourceResponse( group, responseResource );

        return responseResource;
    }

    public List<RepositoryGroupListResource> getList()
        throws IOException
    {
        return groupNRC.getList();
    }

    public Response sendMessage( final Method method, final RepositoryGroupResource resource, final String id )
        throws IOException
    {
        return groupNRC.sendMessage( method, resource, id );
    }

    /**
     * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
     */
    public Response sendMessage( Method method, RepositoryGroupResource resource )
        throws IOException
    {
        return groupNRC.sendMessage( method, resource, resource.getId() );
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

    public RepositoryGroupResource getGroup( final String groupId )
        throws IOException
    {
        return groupNRC.getGroup( groupId );
    }
}
