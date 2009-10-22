/*
 * Nexus: RESTLight Client
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.restlight.stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.jdom.JDOMException;
import org.junit.Test;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class StageClientTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture();

    @Test
    public void queryAllOpenRepositoriesForUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupOpenReposConversation();

        StageClient client = new StageClient( getBaseUrl(), "testuser", "unused" );

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

        StageClient client = new StageClient( getBaseUrl(), "testuser", "unused" );

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
        
        POSTFixture finishPost = new POSTFixture();
        
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

        StageClient client = new StageClient( getBaseUrl(), "testuser", "unused" );

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
    public void queryClosedRepositoryForGAVAndUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupClosedReposConversation();

        StageClient client = new StageClient( getBaseUrl(), "testuser", "unused" );

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
    public void queryClosedRepositoryForUser()
        throws JDOMException, IOException, RESTLightClientException
    {
        setupClosedReposConversation();

        StageClient client = new StageClient( getBaseUrl(), "testuser", "unused" );

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

    private void setupOpenReposConversation()
        throws JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture();
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "profile-list.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture();
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "profile-repo-list.xml" ) );

        conversation.add( reposGet );

        fixture.setConversation( conversation );
    }

    private void setupClosedReposConversation()
        throws JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture();
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture();
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
