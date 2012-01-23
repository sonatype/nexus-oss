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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.ProxyConfig;
import org.sonatype.nexus.restlight.common.RESTLightClientException;

/**
 * REST client to access the functions of the nexus-staging-plugin, available in Nexus Professional.
 */
public class StageClient
    extends AbstractRESTLightClient
{

    public static final String PROFILES_PATH = SVC_BASE + "/staging/profiles";

    public static final String PROFILES_EVALUATE_PATH = SVC_BASE + "/staging/profile_evaluate";

    public static final String PROFILE_REPOS_PATH_PREFIX = SVC_BASE + "/staging/profile_repositories/";

    public static final String STAGE_REPO_FINISH_ACTION = "/finish";

    public static final String STAGE_REPO_DROP_ACTION = "/drop";

    public static final String STAGE_REPO_PROMOTE_ACTION = "/promote";

    public static final String STAGE_REPO_BULK_PROMOTE = SVC_BASE + "/staging/bulk/promote";

    private static final String STAGE_REPO_ID_PARAM = "stagedRepositoryId";

    private static final String PROFILE_ID_ELEMENT = "id";

    private static final String PROFILE_NAME_ELEMENT = "name";

    private static final String PROFILE_MODE_ELEMENT = "mode";

    private static final String REPO_ID_ELEMENT = "repositoryId";

    private static final String REPO_URI_ELEMENT = "repositoryURI";

    private static final String REPO_DESCRIPTION_ELEMENT = "description";

    private static final String REPO_IP_ADDRESS_ELEMENT = "ipAddress";

    private static final String REPO_USER_AGENT_ELEMENT = "userAgent";

    private static final String REPO_CREATED_DATE_ELEMENT = "createdDate";

    private static final String REPO_CLOSED_DATE_ELEMENT = "closedDate";

    private static final String USER_ID_ELEMENT = "userId";

    private static final String OPEN_STAGE_REPOS_XPATH = "stagingRepositoryIds/string/text()";

    private static final String CLOSED_STAGE_REPOS_XPATH = "stagedRepositoryIds/string/text()";

    private static final String STAGE_REPO_LIST_XPATH = "//stagingProfile";

    private static final String STAGE_REPO_XPATH = "//stagingProfile";

    private static final String DATA_XPATH = "//data";

    private static final String STAGE_REPO_DETAIL_XPATH = "//stagingProfileRepository";

    private static final String BUILD_PROMOTION_PROFILES_XPATH = "//stagingProfile[mode=\"GROUP\"]";

    private static final String STAGE_VOCAB_BASE_PATH = "stage/";
    
    public StageClient( final String baseUrl, final String user, final String password )
        throws RESTLightClientException
    {
        super( baseUrl, user, password, STAGE_VOCAB_BASE_PATH  );
    }

    public StageClient(final String baseUrl, final String user, final String password, final ProxyConfig proxyConfig) throws RESTLightClientException {
        super(baseUrl, user, password, STAGE_VOCAB_BASE_PATH , proxyConfig);
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

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, true, true );
    }

    /**
     * Retrieve the list of all closed (finished) staging repositories that may house artifacts with the specified
     * groupId, artifactId, and version for the current user.
     *
     * @return details about each closed repository
     */
    public List<StageRepository> getOpenStageRepositoriesForUser( final String groupId, final String artifactId,
                                                                  final String version )
        throws RESTLightClientException
    {
        Map<String, String> params = new HashMap<String, String>();
        mapCoord( groupId, artifactId, version, params );

        Document doc = get( PROFILES_EVALUATE_PATH, params );

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, true, true );
    }

    /**
     * Retrieve the details for the open staging repository which would be used for an artifact with the specified
     * groupId, artifactId, and version if the current user deployed it. In the event Nexus returns multiple open
     * staging repositories for the given user and GAV, this call will return details for the FIRST repository in that
     * list.
     */
    public StageRepository getOpenStageRepositoryForUser( final String groupId, final String artifactId,
                                                          final String version )
        throws RESTLightClientException
    {
        Map<String, String> params = new HashMap<String, String>();
        mapCoord( groupId, artifactId, version, params );

        Document doc = get( PROFILES_EVALUATE_PATH, params );

        List<StageRepository> ids = parseStageRepositories( doc, STAGE_REPO_XPATH, true, true );
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

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, false, true );
    }

    /**
     * Retrieve the list of all closed (finished) staging repositories that may house artifacts with the specified
     * groupId, artifactId, and version for the current user.
     *
     * @return details about each closed repository
     */
    public List<StageRepository> getClosedStageRepositoriesForUser( final String groupId, final String artifactId,
                                                                    final String version )
        throws RESTLightClientException
    {
        Map<String, String> params = new HashMap<String, String>();
        mapCoord( groupId, artifactId, version, params );

        Document doc = get( PROFILES_EVALUATE_PATH, params );

        return parseStageRepositories( doc, STAGE_REPO_XPATH, false, true );
    }

    /**
     * Find the details for the open staging repository for the given groupId, artifactId, version, and the current
     * user, using the same algorithm as {@link StageClient#getOpenStageRepositoryForUser(String, String, String)}. Once
     * we have the details for this repository, submit those details to Nexus to convert the open repository to closed
     * (finished) status. This will make the artifacts in the repository available for use in Maven, etc.
     */
    public void finishRepositoryForUser( final String groupId, final String artifactId, final String version,
                                         final String description )
        throws RESTLightClientException
    {
        StageRepository repo = getOpenStageRepositoryForUser( groupId, artifactId, version );

        finishRepository( repo, description );
    }

    /**
     * Assuming the user has already queried Nexus for a valid {@link StageRepository} instance (details for an open
     * staging repository), submit those details to Nexus to convert the open repository to closed (finished) status.
     * This will make the artifacts in the repository available for use in Maven, etc.
     */
    public void finishRepository( final StageRepository repo, final String description )
        throws RESTLightClientException
    {
        Element extras = processDescription( description );

        performStagingAction( repo, STAGE_REPO_FINISH_ACTION, Arrays.asList( extras ) );
    }

    private Element processDescription( final String description )
    {
        if ( description == null )
        {
            return null;
        }

        String descElementName = getVocabulary().getProperty( VocabularyKeys.PROMOTE_STAGE_REPO_DESCRIPTION_ELEMENT,
                                                              VocabularyKeys.SUPPRESS_ELEMENT_VALUE );

        if ( !VocabularyKeys.SUPPRESS_ELEMENT_VALUE.equals( descElementName ) )
        {
            Element desc = new Element( REPO_DESCRIPTION_ELEMENT ).setText( description );
            return desc;
        }
        else
        {
            return null;
        }
    }

    /**
     * Assuming the user has already queried Nexus for a valid {@link StageRepository} instance (details for a staging
     * repository), submit those details to Nexus to drop the repository.
     */
    public void dropRepository( final StageRepository repo, final String description )
        throws RESTLightClientException
    {
        Element extras = processDescription( description );
        performStagingAction( repo, STAGE_REPO_DROP_ACTION, Arrays.asList( extras ) );
    }

    /**
     * Assuming the user has already queried Nexus for a valid {@link StageRepository} instance (details for a staging
     * repository), submit those details to Nexus to promote the repository into the permanent repository with the
     * specified targetRepositoryId.
     *
     * @param description
     */
    public void promoteRepository( final StageRepository repo, final String targetRepositoryId, String description )
        throws RESTLightClientException
    {
        Element target = new Element( "targetRepositoryId" ).setText( targetRepositoryId );
        Element extras = processDescription( description );

        performStagingAction( repo, STAGE_REPO_PROMOTE_ACTION, Arrays.asList( extras, target ) );
    }

    /**
     * Promotes a set of repositories to a group profile.
     *
     * @param groupProfileId The group profile to promote to.
     * @param repositoryIds  A list of repositoryIds to be promoted.
     * @throws RESTLightClientException
     */
    public void promoteRepositories( String stagingProfileGroup, String description, List<String> stagedRepositoryIds )
        throws RESTLightClientException
    {
        if ( stagedRepositoryIds == null || stagedRepositoryIds.isEmpty() )
        {
            throw new RESTLightClientException(
                "No staging repositories specified. Please provide a valid staged repository ids." );
        }

        if ( StringUtils.isEmpty( stagingProfileGroup ) )
        {
            throw new RESTLightClientException(
                "No build promotion profile specified. Please provide a build promotion profile." );
        }

        String rootElement = this.getVocabulary().getProperty( VocabularyKeys.BULK_ACTION_REQUEST_ROOT_ELEMENT );
        Document body = new Document().setRootElement( new Element( rootElement ) );

        Element data = new Element( "data" );
        body.getRootElement().addContent( data );

        if ( StringUtils.isNotEmpty( description ) )
        {
            data.addContent( new Element( "description" ).setText( description ) );
        }

        data.addContent( new Element( "stagingProfileGroup" ).setText( stagingProfileGroup ) );

        Element staedRepoIds = new Element( "stagedRepositoryIds" );
        data.addContent( staedRepoIds );

        for ( String repoId : stagedRepositoryIds )
        {
            staedRepoIds.addContent( new Element( "string" ).setText( repoId ) );
        }

        post( STAGE_REPO_BULK_PROMOTE, null, body );
    }

    /**
     * Returns a list of all the build promotion profile Ids.
     *
     * @return
     * @throws RESTLightClientException
     */
    @SuppressWarnings( "unchecked" )
    public List<StageProfile> getBuildPromotionProfiles()
        throws RESTLightClientException
    {
        Document doc = get( PROFILES_PATH );

        // heavy lifting is done with xpath
        XPath profileXp = newXPath( BUILD_PROMOTION_PROFILES_XPATH );

        List<Element> profiles;
        try
        {
            profiles = profileXp.selectNodes( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException(
                "XPath selection failed: '" + BUILD_PROMOTION_PROFILES_XPATH + "' (Root node: "
                    + doc.getRootElement().getName() + ").", e );
        }

        List<StageProfile> result = new ArrayList<StageProfile>();
        if ( profiles != null )
        {
            for ( Element profile : profiles )
            {
                // just pull out the id and name.
                String profileId = profile.getChild( PROFILE_ID_ELEMENT ).getText();
                String name = profile.getChild( PROFILE_NAME_ELEMENT ).getText();

                result.add( new StageProfile( profileId, name ) );
            }
        }
        return result;
    }

    @SuppressWarnings( "unchecked" )
    private List<StageRepository> parseStageRepositories( final Document doc, final String profileXpath,
                                                          final Boolean findOpen, boolean filterUser )
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
            throw new RESTLightClientException(
                "XPath selection failed: '" + profileXpath + "' (Root node: " + doc.getRootElement().getName() + ").",
                e );
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
                String profileName = profile.getChild( PROFILE_NAME_ELEMENT ).getText();

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
                                                       new StageRepository( profileId, txt.getText(),
                                                                            findOpen ).setProfileName( profileName ) );
                            }
                        }
                    }
                    catch ( JDOMException e )
                    {
                        throw new RESTLightClientException(
                            "XPath selection failed: '" + OPEN_STAGE_REPOS_XPATH + "' (Node: " + profile.getName()
                                + ").", e );
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
                                                       new StageRepository( profileId, txt.getText(),
                                                                            findOpen ).setProfileName( profileName ) );
                            }
                        }
                    }
                    catch ( JDOMException e )
                    {
                        throw new RESTLightClientException(
                            "XPath selection failed: '" + CLOSED_STAGE_REPOS_XPATH + "' (Node: " + profile.getName()
                                + ").", e );
                    }
                }

                if ( !matchingRepoStubs.isEmpty() )
                {
                    parseStageRepositoryDetails( profileId, matchingRepoStubs, filterUser );

                    result.addAll( matchingRepoStubs.values() );
                }
            }
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    private void parseStageRepositoryDetails( final String profileId, final Map<String, StageRepository> repoStubs,
                                              boolean filterUser )
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
                    if ( filterUser )
                    {
                        repoStubs.remove( key );
                    }
                }

                Element url = detail.getChild( REPO_URI_ELEMENT );
                if ( url != null )
                {
                    repo.setUrl( url.getText() );
                }

                Element desc = detail.getChild( REPO_DESCRIPTION_ELEMENT );
                if ( desc != null )
                {
                    repo.setDescription( desc.getText() );
                }

                Element ipAddress = detail.getChild( REPO_IP_ADDRESS_ELEMENT );
                if ( ipAddress != null )
                {
                    repo.setIpAddress( ipAddress.getText() );
                }

                Element userAgent = detail.getChild( REPO_USER_AGENT_ELEMENT );
                if ( userAgent != null )
                {
                    repo.setUserAgent( userAgent.getText() );
                }

                final Element createdDate = detail.getChild( REPO_CREATED_DATE_ELEMENT );
                if ( createdDate != null )
                {
                    repo.setCreatedDate( createdDate.getText() );
                }

                final Element closedDate = detail.getChild( REPO_CLOSED_DATE_ELEMENT );
                if ( closedDate != null )
                {
                    repo.setClosedDate( closedDate.getText() );
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

    private void performStagingAction( final StageRepository repo, final String actionSubpath,
                                       final List<Element> extraData )
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

    public List<StageRepository> getOpenStageRepositories()
        throws RESTLightClientException
    {
        Document doc = get( PROFILES_PATH );

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, true, false );
    }

    public List<StageRepository> getClosedStageRepositories()
        throws RESTLightClientException
    {
        Document doc = get( PROFILES_PATH );

        return parseStageRepositories( doc, STAGE_REPO_LIST_XPATH, false, false );
    }

    /**
     * Returns a list of all the staging profile Ids.
     *
     * @return
     * @throws RESTLightClientException
     */
    @SuppressWarnings( "unchecked" )
    public List<StageProfile> getStageProfiles()
        throws RESTLightClientException
    {
        Document doc = get( PROFILES_PATH );

        // heavy lifting is done with xpath
        XPath profileXp = newXPath( STAGE_REPO_XPATH );

        List<Element> profiles;
        try
        {
            profiles = profileXp.selectNodes( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException(
                "XPath selection failed: '" + STAGE_REPO_XPATH + "' (Root node: " + doc.getRootElement().getName()
                    + ").", e );
        }

        List<StageProfile> result = new ArrayList<StageProfile>();
        if ( profiles != null )
        {
            for ( Element profile : profiles )
            {
                // just pull out the id and name.
                String profileId = profile.getChild( PROFILE_ID_ELEMENT ).getText();
                String name = profile.getChild( PROFILE_NAME_ELEMENT ).getText();
                String mode = profile.getChild( PROFILE_MODE_ELEMENT ).getText();

                result.add( new StageProfile( profileId, name, mode ) );
            }
        }
        return result;
    }
}
