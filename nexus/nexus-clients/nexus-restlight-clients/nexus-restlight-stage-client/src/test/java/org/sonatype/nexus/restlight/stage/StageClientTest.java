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
package org.sonatype.nexus.restlight.stage;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import junit.framework.Assert;

public class StageClientTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

    private long before;

    @Before
    public void before()
    {
        this.before = System.currentTimeMillis();
    }

    @Test
    public void queryAllOpenRepositories()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupOpenReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repositories = client.getOpenStageRepositories();

        failOnUnfinishedConversation();

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

        failOnUnfinishedConversation();

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
        setupEvaluateOpenReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        StageRepository repo = client.getOpenStageRepositoryForUser( "group", "artifact", "version" );

        failOnUnfinishedConversation();

        assertNotNull( repo );

        assertEquals( "tp1-002", repo.getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repo.getUrl() );
    }

    private void failOnUnfinishedConversation()
    {
        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }
    }

    @Test
    public void finishRepository()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupEvaluateOpenReposConversation();

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

        failOnUnfinishedConversation();
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

            failOnUnfinishedConversation();
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

        addGet( StageClient.PROFILES_PATH, "profile-list-bp.xml" );

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
        setupEvaluateClosedReposConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repos = client.getClosedStageRepositoriesForUser( "group", "artifact", "version" );

        failOnUnfinishedConversation();

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

        failOnUnfinishedConversation();

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

        failOnUnfinishedConversation();

        assertNotNull( repos );
        assertEquals( 2, repos.size() );

        assertEquals( "tp1-002", repos.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repos.get( 0 ).getUrl() );

        assertEquals( "tp1-003", repos.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repos.get( 1 ).getUrl() );
    }

    @Test
    public void queryAllRepositoriesWithDates()
        throws Exception
    {
        setupAllReposWithDatesConversation();

        StageClient client = new StageClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );

        List<StageRepository> repositories = client.getOpenStageRepositories();

        failOnUnfinishedConversation();

        assertNotNull( repositories );

        assertThat( repositories, hasSize(4) );

        assertEquals( "tp1-001", repositories.get( 0 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-001", repositories.get( 0 ).getUrl() );
        assertThat( repositories.get( 0 ), hasProperty( "createdDate", is("n/a") ) );
        assertThat( repositories.get( 0 ), hasProperty( "closedDate", is("n/a") ) );

        assertEquals( "tp1-002", repositories.get( 1 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-002", repositories.get( 1 ).getUrl() );
        assertThat( repositories.get( 1 ), hasProperty( "createdDate", is("n/a") ) );
        assertThat( repositories.get( 1 ), hasProperty( "closedDate", is("n/a") ) );

        assertEquals( "tp1-003", repositories.get( 2 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repositories.get( 2 ).getUrl() );
        assertThat( repositories.get( 2 ), hasProperty( "createdDate", is( "arbitraryDateString" ) ) );
        assertThat( repositories.get( 2 ), hasProperty( "closedDate", is("n/a") ) );

        assertEquals( "tp1-004", repositories.get( 3 ).getRepositoryId() );
        assertEquals( "http://localhost:8082/nexus/content/repositories/tp1-003", repositories.get( 2 ).getUrl() );
        assertThat( repositories.get( 3 ), hasProperty( "createdDate", is( "arbitraryDateString" ) ) );
        assertThat( repositories.get( 3 ), hasProperty( "closedDate", is( "arbitraryDateString" ) ) );
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

        addGet( StageClient.PROFILES_PATH, "profile-list.xml" );

        // GETFixture repoEvalListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        // repoEvalListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        // repoEvalListGet.setResponseDocument( readTestDocumentResource( "profile-list.xml" ) );
        //
        // fixture.getConversation().add( repoEvalListGet );

        addGet( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1", "profile-repo-list.xml" );
    }

    private void setupAllReposWithDatesConversation()
        throws Exception
    {
        initConversation();

        addGet( StageClient.PROFILES_PATH, "dates-profile-list.xml" );
        addGet( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1", "creation-closed-dates.xml" );
    }

    private void addGet( String servicePath, String resourcePath )
        throws JDOMException, IOException
    {
        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( servicePath );
        repoListGet.setResponseDocument( readTestDocumentResource( resourcePath ) );

        fixture.getConversation().add( repoListGet );
    }

    private void setupEvaluateOpenReposConversation()
        throws JDOMException, IOException
    {
        initConversation();

        addGet( StageClient.PROFILES_EVALUATE_PATH, "profile-list.xml" );

        // GETFixture repoEvalListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        // repoEvalListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        // repoEvalListGet.setResponseDocument( readTestDocumentResource( "profile-list.xml" ) );
        //
        // fixture.getConversation().add( repoEvalListGet );

        addGet( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1", "profile-repo-list.xml" );
    }

    private void setupEvaluateClosedReposConversation()
        throws JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        // GETFixture repoEvalListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        // repoEvalListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        // repoEvalListGet.setResponseDocument( readTestDocumentResource( "profile-list-closed.xml" ) );
        //
        // conversation.add( repoEvalListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "profile-repo-list-closed.xml" ) );

        conversation.add( reposGet );

        fixture.setConversation( conversation );
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

        // GETFixture repoEvalListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        // repoEvalListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        // repoEvalListGet.setResponseDocument( readTestDocumentResource( "profile-list-closed.xml" ) );
        //
        // conversation.add( repoEvalListGet );

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
