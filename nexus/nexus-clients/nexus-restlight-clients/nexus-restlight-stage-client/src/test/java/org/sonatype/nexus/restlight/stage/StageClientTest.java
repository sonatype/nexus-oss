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
package org.sonatype.nexus.restlight.stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.jdom.JDOMException;
import org.junit.Test;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

public class StageClientTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

    @Test
    public void queryAllOpenRepositories()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupOpenReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repositories = client.getOpenStageRepositories();

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( repositories );

        assertEquals( 3, repositories.size() );

        assertEquals( "tp1-001", repositories.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-001", repositories.get( 0 ).getUrl() );

        assertEquals( "tp1-002", repositories.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repositories.get( 1 ).getUrl() );

        assertEquals( "tp1-003", repositories.get( 2 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repositories.get( 2 ).getUrl() );
    }

    @Test
    public void queryAllOpenRepositoriesForUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupOpenReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repositories = client.getOpenStageRepositoriesForUser();

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( repositories );

        assertEquals( 2, repositories.size() );

        assertEquals( "tp1-002", repositories.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repositories.get( 0 ).getUrl() );

        assertEquals( "tp1-003", repositories.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repositories.get( 1 ).getUrl() );
    }

    @Test
    public void queryOpenRepositoryForGAVAndUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupOpenReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        StageRepository repo = client.getOpenStageRepositoryForUser( "group", "artifact", "version" );

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( repo );

        assertEquals( "tp1-002", repo.getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repo.getUrl() );
    }

    @Test
    public void finishRepository()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupOpenReposConversation();

        POSTFixture finishPost = new POSTFixture( getExpectedUser(), getExpectedPassword() );

        finishPost.setExactURI( StageClient.PROFILES_PATH + "/112cc490b91265a1" + StageClient.STAGE_REPO_FINISH_ACTION );

        if ( "1.3.1".equals( getTestNexusAPIVersion() ) )
        {
            finishPost.setRequestDocument( readTestDocumentResource( "finish-repo-old.xml" ) );
        }
        else
        {
            finishPost.setRequestDocument( readTestDocumentResource( "finish-repo-new.xml" ) );
        }

        finishPost.setResponseStatus( 201 );

        fixture.getConversation().add( finishPost );

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        StageRepository repo = client.getOpenStageRepositoryForUser( "group", "artifact", "version" );

        assertNotNull( repo );

        assertEquals( "tp1-002", repo.getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repo.getUrl() );

        client.finishRepository( repo, "this is a description" );

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }
    }

    @Test
    public void promoteMultipleItemsToGroup()
        throws JDOMException, IOException, RESTLightClientException
    {

        try
        {
            System.setProperty( TEST_NX_API_VERSION_SYSPROP, "1.7.2" );

            initConversation();

            POSTFixture finishPost = new POSTFixture( getExpectedUser(), getExpectedPassword() );

            finishPost.setExactURI( StageClient.STAGE_REPO_BULK_PROMOTE );
            finishPost.setRequestDocument( readTestDocumentResource( "promote-to-group-request.xml" ) );
            finishPost.setResponseStatus( 201 );

            fixture.getConversation().add( finishPost );

            StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

            List<String> repoIds = new ArrayList<String>();
            repoIds.add( "repoId1" );
            repoIds.add( "repoId2" );
            client.promoteRepositories( "groupProfile", "The description", repoIds );

            List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
            if ( unused != null && !unused.isEmpty() )
            {
                System.out.println( unused );
                fail( "Conversation was not finished. Didn't traverse:\n" + unused );
            }
        }
        finally
        {
            // reset
            System.setProperty( TEST_NX_API_VERSION_SYSPROP, "1.3.2" );
        }
    }

    @Test
    public void queryBuildPromotionProfiles()
        throws JDOMException, IOException, RESTLightClientException
    {
        initConversation();

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "profile-list-bp.xml" ) );
        fixture.getConversation().add( repoListGet );

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );
        List<StageProfile> profiles = client.getBuildPromotionProfiles();

        Assert.assertEquals( 2, profiles.size() );
        Assert.assertEquals( "profile3", profiles.get( 0 ).getProfileId() );
        Assert.assertEquals( "test-profile3", profiles.get( 0 ).getName() );

        Assert.assertEquals( "profile4", profiles.get( 1 ).getProfileId() );
        Assert.assertEquals( "test-profile4", profiles.get( 1 ).getName() );

    }

    @Test
    public void queryClosedRepositoryForGAVAndUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupClosedReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repos = client.getClosedStageRepositoriesForUser( "group", "artifact", "version" );

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( repos );
        assertEquals( 2, repos.size() );

        assertEquals( "tp1-002", repos.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repos.get( 0 ).getUrl() );

        assertEquals( "tp1-003", repos.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repos.get( 1 ).getUrl() );
    }

    @Test
    public void queryAllClosedRepository()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupClosedReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repos = client.getClosedStageRepositories();

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( repos );
        assertEquals( 3, repos.size() );

        assertEquals( "tp1-001", repos.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-001", repos.get( 0 ).getUrl() );

        assertEquals( "tp1-002", repos.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repos.get( 1 ).getUrl() );

        assertEquals( "tp1-003", repos.get( 2 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repos.get( 2 ).getUrl() );
    }

    @Test
    public void queryClosedRepositoryForUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupClosedReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repos = client.getClosedStageRepositoriesForUser();

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( repos );
        assertEquals( 2, repos.size() );

        assertEquals( "tp1-002", repos.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repos.get( 0 ).getUrl() );

        assertEquals( "tp1-003", repos.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repos.get( 1 ).getUrl() );
    }

    private void initConversation()
        throws JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        fixture.setConversation( conversation );
    }

    private void setupOpenReposConversation()
        throws JDOMException, IOException
    {
        initConversation();

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "profile-list.xml" ) );

        fixture.getConversation().add( repoListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "profile-repo-list.xml" ) );

        fixture.getConversation().add( reposGet );
    }

    private void setupClosedReposConversation()
        throws JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "profile-repo-list-closed.xml" ) );

        conversation.add( reposGet );

        fixture.setConversation( conversation );
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

}
