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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.RESTLightClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST client to access the functions of the nexus-staging-plugin, available in Nexus Professional.
 */
public class StageClient
extends AbstractRESTLightClient
{

    public static final String PROFILES_PATH = SVC_BASE + "/staging/profiles";

    public static final String PROFILE_REPOS_PATH_PREFIX = SVC_BASE + "/staging/profile_repositories/";

    public static final String STAGE_REPO_FINISH_ACTION = "/finish";

    public static final String STAGE_REPO_DROP_ACTION = "/drop";

    public static final String STAGE_REPO_PROMOTE_ACTION = "/promote";

    private static final String STAGE_REPO_ID_PARAM = "stagedRepositoryId";

    private static final String PROFILE_ID_ELEMENT = "id";

    private static final String REPO_ID_ELEMENT = "repositoryId";

    private static final String REPO_URI_ELEMENT = "repositoryURI";

    private static final String USER_ID_ELEMENT = "userId";

    private static final String OPEN_STAGE_REPOS_XPATH = "//stagingRepositoryIds/string/text()";

    private static final String CLOSED_STAGE_REPOS_XPATH = "//stagedRepositoryIds/string/text()";

    private static final String STAGE_REPO_LIST_XPATH = "//stagingProfile";

    private static final String STAGE_REPO_XPATH = "//stagingProfile";

    private static final String STAGE_REPO_DETAIL_XPATH = "//stagingProfileRepository";

    public StageClient( final String baseUrl, final String user, final String password )
    throws RESTLightClientException
    {
        super( baseUrl, user, password, "stage/" );
    }

    /**
     * Retrieve the list of all open staging repositories (not finished) in all available profiles that are opened for
     * the current user (the one specified in this client's constructor).
     * 
     * @return details about each open repository
     */
    public List<StageRepository> getOpenStageRepositoriesForUser()
    throws RESTLightClientException
    {
        Document doc = get( PROFILES_PATH );

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, true );
    }

    /**
     * Retrieve the details for the open staging repository which would be used for an artifact with the specified
     * groupId, artifactId, and version if the current user deployed it. In the event Nexus returns multiple open
     * staging repositories for the given user and GAV, this call will return details for the FIRST repository in that
     * list.
     */
    public StageRepository getOpenStageRepositoryForUser( final String groupId, final String artifactId, final String version )
    throws RESTLightClientException
    {
        Map<String, String> params = new HashMap<String, String>();
        mapCoord( groupId, artifactId, version, params );

        Document doc = get( PROFILES_PATH, params );

        List<StageRepository> ids = parseStageRepositories( doc, STAGE_REPO_XPATH, true );
        if ( ids == null || ids.isEmpty() )
        {
            return null;
        }
        else
        {
            return ids.get( 0 );
        }
    }

    /**
     * Retrieve the list of all closed (finished) staging repositories in all available profiles that are opened for the
     * current user (the one specified in this client's constructor).
     * 
     * @return details about each closed repository
     */
    public List<StageRepository> getClosedStageRepositoriesForUser()
    throws RESTLightClientException
    {
        Document doc = get( PROFILES_PATH );

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, false );
    }

    /**
     * Retrieve the list of all closed (finished) staging repositories that may house artifacts with the specified
     * groupId, artifactId, and version for the current user.
     * 
     * @return details about each closed repository
     */
    public List<StageRepository> getClosedStageRepositoriesForUser( final String groupId, final String artifactId, final String version )
    throws RESTLightClientException
    {
        Map<String, String> params = new HashMap<String, String>();
        mapCoord( groupId, artifactId, version, params );

        Document doc = get( PROFILES_PATH, params );

        return parseStageRepositories( doc, STAGE_REPO_XPATH, false );
    }

    /**
     * Find the details for the open staging repository for the given groupId, artifactId, version, and the current
     * user, using the same algorithm as {@link StageClient#getOpenStageRepositoryForUser(String, String, String)}. Once
     * we have the details for this repository, submit those details to Nexus to convert the open repository to closed
     * (finished) status. This will make the artifacts in the repository available for use in Maven, etc.
     */
    public void finishRepositoryForUser( final String groupId, final String artifactId, final String version )
    throws RESTLightClientException
    {
        StageRepository repo = getOpenStageRepositoryForUser( groupId, artifactId, version );

        finishRepository( repo );
    }

    /**
     * Assuming the user has already queried Nexus for a valid {@link StageRepository} instance (details for an open
     * staging repository), submit those details to Nexus to convert the open repository to closed (finished) status.
     * This will make the artifacts in the repository available for use in Maven, etc.
     */
    public void finishRepository( final StageRepository repo )
    throws RESTLightClientException
    {
        performStagingAction( repo, STAGE_REPO_FINISH_ACTION, null );
    }

    /**
     * Assuming the user has already queried Nexus for a valid {@link StageRepository} instance (details for a staging
     * repository), submit those details to Nexus to drop the repository.
     */
    public void dropRepository( final StageRepository repo )
    throws RESTLightClientException
    {
        performStagingAction( repo, STAGE_REPO_DROP_ACTION, null );
    }

    /**
     * Assuming the user has already queried Nexus for a valid {@link StageRepository} instance (details for a staging
     * repository), submit those details to Nexus to promote the repository into the permanent repository with the
     * specified targetRepositoryId.
     */
    public void promoteRepository( final StageRepository repo, final String targetRepositoryId )
    throws RESTLightClientException
    {
        Element target = new Element( "targetRepositoryId" ).setText( targetRepositoryId );

        performStagingAction( repo, STAGE_REPO_PROMOTE_ACTION, Collections.singletonList( target ) );
    }

    @SuppressWarnings( "unchecked" )
    private List<StageRepository> parseStageRepositories( final Document doc, final String profileXpath, final Boolean findOpen )
    throws RESTLightClientException
    {
        // System.out.println( new XMLOutputter().outputString( doc ) );

        XPath profileXp = newXPath( profileXpath );

        List<Element> profiles;
        try
        {
            profiles = profileXp.selectNodes( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "XPath selection failed: '" + profileXpath + "' (Root node: "
                                                 + doc.getRootElement().getName() + ").", e );
        }

        List<StageRepository> result = new ArrayList<StageRepository>();
        if ( profiles != null )
        {

            XPath openRepoIdXPath = newXPath( OPEN_STAGE_REPOS_XPATH );
            XPath closedRepoIdXPath = newXPath( CLOSED_STAGE_REPOS_XPATH );

            for ( Element profile : profiles )
            {
                // System.out.println( new XMLOutputter().outputString( profile ) );

                String profileId = profile.getChild( PROFILE_ID_ELEMENT ).getText();
                Map<String, StageRepository> matchingRepoStubs = new LinkedHashMap<String, StageRepository>();

                if ( !Boolean.FALSE.equals( findOpen ) )
                {
                    try
                    {
                        List<Text> repoIds = openRepoIdXPath.selectNodes( profile );
                        if ( repoIds != null && !repoIds.isEmpty() )
                        {
                            for ( Text txt : repoIds )
                            {
                                matchingRepoStubs.put( profileId + "/" + txt.getText(),
                                                       new StageRepository( profileId, txt.getText(), findOpen ) );
                            }
                        }
                    }
                    catch ( JDOMException e )
                    {
                        throw new RESTLightClientException( "XPath selection failed: '" + OPEN_STAGE_REPOS_XPATH
                                                             + "' (Node: " + profile.getName() + ").", e );
                    }
                }

                if ( !Boolean.TRUE.equals( findOpen ) )
                {
                    try
                    {
                        List<Text> repoIds = closedRepoIdXPath.selectNodes( profile );
                        if ( repoIds != null && !repoIds.isEmpty() )
                        {
                            for ( Text txt : repoIds )
                            {
                                matchingRepoStubs.put( profileId + "/" + txt.getText(),
                                                       new StageRepository( profileId, txt.getText(), findOpen ) );
                            }
                        }
                    }
                    catch ( JDOMException e )
                    {
                        throw new RESTLightClientException( "XPath selection failed: '" + CLOSED_STAGE_REPOS_XPATH
                                                             + "' (Node: " + profile.getName() + ").", e );
                    }
                }

                if ( !matchingRepoStubs.isEmpty() )
                {
                    parseStageRepositoryDetails( profileId, matchingRepoStubs );

                    result.addAll( matchingRepoStubs.values() );
                }
            }
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    private void parseStageRepositoryDetails( final String profileId, final Map<String, StageRepository> repoStubs )
    throws RESTLightClientException
    {
        // System.out.println( repoStubs );

        Document doc = get( PROFILE_REPOS_PATH_PREFIX + profileId );

        // System.out.println( new XMLOutputter().outputString( doc ) );

        XPath repoXPath = newXPath( STAGE_REPO_DETAIL_XPATH );

        List<Element> repoDetails;
        try
        {
            repoDetails = repoXPath.selectNodes( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "Failed to select detail sections for staging-profile repositories.",
                                                 e );
        }

        if ( repoDetails != null && !repoDetails.isEmpty() )
        {
            for ( Element detail : repoDetails )
            {
                String repoId = detail.getChild( REPO_ID_ELEMENT ).getText();
                String key = profileId + "/" + repoId;

                StageRepository repo = repoStubs.get( key );

                if ( repo == null )
                {
                    continue;
                }

                Element uid = detail.getChild( USER_ID_ELEMENT );
                if ( uid != null && getUser() != null && getUser().equals( uid.getText().trim() ) )
                {
                    repo.setUser( uid.getText().trim() );
                }
                else
                {
                    repoStubs.remove( key );
                }

                Element url = detail.getChild( REPO_URI_ELEMENT );
                if ( url != null )
                {
                    repo.setUrl( url.getText() );
                }
            }
        }
    }

    private XPath newXPath( final String xpath )
    throws RESTLightClientException
    {
        try
        {
            return XPath.newInstance( xpath );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "Failed to build xpath: '" + xpath + "'.", e );
        }
    }

    private void performStagingAction( final StageRepository repo, final String actionSubpath, final List<Element> extraData )
    throws RESTLightClientException
    {
        if ( repo == null )
        {
            throw new RESTLightClientException(
                                                 "No staging-repository details specified. Please provide a valid StageRepository instance." );
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put( STAGE_REPO_ID_PARAM, repo.getRepositoryId() );

        String rootElement = getVocabulary().getProperty( VocabularyKeys.PROMOTE_STAGE_REPO_ROOT_ELEMENT );
        Document body = new Document().setRootElement( new Element( rootElement ) );

        Element data = new Element( "data" );
        body.getRootElement().addContent( data );

        data.addContent( new Element( "stagedRepositoryId" ).setText( repo.getRepositoryId() ) );

        if ( extraData != null && !extraData.isEmpty() )
        {
            for ( Element extra : extraData )
            {
                data.addContent( extra );
            }
        }

        post( PROFILES_PATH + "/" + repo.getProfileId() + actionSubpath, null, body );
    }

}
