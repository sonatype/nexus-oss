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
package org.sonatype.nexus.rest.indexng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.UniqueArtifactFilterPostprocessor;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.index.KeywordSearcher;
import org.sonatype.nexus.index.MavenCoordinatesSearcher;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractIndexerNexusPlexusResource;
import org.sonatype.nexus.rest.model.NexusNGArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifactHit;
import org.sonatype.nexus.rest.model.NexusNGArtifactLink;
import org.sonatype.nexus.rest.model.NexusNGRepositoryDetail;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

@Component( role = PlexusResource.class, hint = "SearchNGIndexPlexusResource" )
@Path( SearchNGIndexPlexusResource.RESOURCE_URI )
public class SearchNGIndexPlexusResource
    extends AbstractIndexerNexusPlexusResource
{
    /**
     * Capping the number of Lucene Documents to process, to avoid potention problems and DOS-like attacks. If someone
     * needs more results, download the index instead and process it in-situ.
     */
    private static final int LUCENE_HIT_LIMIT = 5000;

    /**
     * Hard upper limit of the count of search hits delivered over REST API. In short: how many rows user sees in UI
     * max. Note: this does not correspond to ArtifactInfo count! This is GA count that may be backed by a zillion
     * ArtifactInfos! Before (old resource) this was 200 (well, the max count of ROWS user would see was 200).
     */
    private static final int DEFAULT_GA_HIT_LIMIT = 200;

    /**
     * The actual limit value, that may be overridden by users using Java System Properties, and defaults to
     * DEFAULT_GA_HIT_LIMIT.
     */
    private static final int GA_HIT_LIMIT = SystemPropertiesHelper.getInteger( "plexus.search.ga.hit.limit",
        DEFAULT_GA_HIT_LIMIT );

    /**
     * Time to spend in 1st processing loop before bail out. It defaults to 30sec (UI timeout is 60secs).
     */
    private static final long DEFAULT_FIRST_LOOP_EXECUTION_TIME_LIMIT = 30000;

    /**
     * The actual time limit to spend in search, that may be overridden by users using Java System Properties, and
     * defaults to DEFAULT_FIRST_LOOP_EXECUTION_TIME_LIMIT.
     */
    private static final long FIRST_LOOP_EXECUTION_TIME_LIMIT = SystemPropertiesHelper.getLong(
        "plexus.search.ga.firstLoopTime", DEFAULT_FIRST_LOOP_EXECUTION_TIME_LIMIT );

    /**
     * The default threshold of change size in relevance, from where we may "cut" the results.
     */
    private static final int DEFAULT_DOCUMENT_RELEVANCE_HIT_CHANGE_THRESHOLD = 500;

    /**
     * The threshold of change size in relevance, from where we may "cut" the results.
     */
    private static final float DOCUMENT_RELEVANCE_HIT_CHANGE_THRESHOLD = (float) SystemPropertiesHelper.getInteger(
        "plexus.search.ga.hit.relevanceDropThreshold", DEFAULT_DOCUMENT_RELEVANCE_HIT_CHANGE_THRESHOLD ) / 1000f;

    /**
     * The default treshold of change from the very 1st hit. from where we may "cut" the results.
     */
    private static final int DEFAULT_DOCUMENT_TOP_RELEVANCE_HIT_CHANGE_THRESHOLD = 750;

    /**
     * The treshold of change from the very 1st hit. from where we may "cut" the results.
     */
    private static final float DOCUMENT_TOP_RELEVANCE_HIT_CHANGE_THRESHOLD = (float) SystemPropertiesHelper.getInteger(
        "plexus.search.ga.hit.topRelevanceDropThreshold", DEFAULT_DOCUMENT_TOP_RELEVANCE_HIT_CHANGE_THRESHOLD ) / 1000f;

    /**
     * The default treshold, that is used to "uncollapse" the collapsed results (if less hits than threshold).
     */
    private static final int DEFAULT_COLLAPSE_OVERRIDE_TRESHOLD = 35;

    /**
     * The treshold, that is used to "uncollapse" the collapsed results (if less hits than threshold).
     */
    private static final int COLLAPSE_OVERRIDE_TRESHOLD = SystemPropertiesHelper.getInteger(
        "plexus.search.ga.collapseOverrideThreshold", DEFAULT_COLLAPSE_OVERRIDE_TRESHOLD );

    // doing "plain search" 3 times in case of AlreadyClosedExce
    private final int RETRIES = 3;

    public static final String RESOURCE_URI = "/lucene/search";

    private Logger searchDiagnosticLogger = LoggerFactory.getLogger( "search.ng.diagnostic" );

    @Requirement( role = Searcher.class )
    private List<Searcher> searchers;

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:index]" );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    /**
     * Search against all repositories using provided parameters. Note there are a few different types of searches you
     * can perform. If you provide the 'q' query parameter, a keyword search will be performed. If you provide the 'g,
     * a, v, p or c' query parameters, a maven coordinate search will be performed. If you provide the 'cn' query
     * parameter, a classname search will be performed. If you provide the 'sha1' query parameter, a checksum search
     * will be performed.
     * 
     * @param q provide this param for a keyword search (g, a, v, p, c, cn, sha1 params will be ignored).
     * @param sha1 provide this param for a checksum search (g, a, v, p, c, cn params will be ignored).
     * @param cn provide this param for a classname search (g, a, v, p, c params will be ignored).
     * @param g group id to perform a maven search against (can be combined with a, v, p & c params as well).
     * @param a artifact id to perform a maven search against (can be combined with g, v, p & c params as well).
     * @param v version to perform a maven search against (can be combined with g, a, p & c params as well).
     * @param p packaging type to perform a maven search against (can be combined with g, a, v & c params as well).
     * @param c classifier to perform a maven search against (can be combined with g, a, v & p params as well).
     * @param from result index to start retrieving results from.
     * @param count number of results to have returned to you.
     * @param repositoryId The repositoryId to which repository search should be narrowed. Omit if search should be
     *            global.
     */
    @Override
    @GET
    @ResourceMethodSignature( queryParams = { @QueryParam( "q" ), @QueryParam( "g" ), @QueryParam( "a" ),
        @QueryParam( "v" ), @QueryParam( "p" ), @QueryParam( "c" ), @QueryParam( "cn" ), @QueryParam( "sha1" ),
        @QueryParam( "from" ), @QueryParam( "count" ), @QueryParam( "repositoryId" ) }, output = SearchResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        final Map<String, String> terms = new HashMap<String, String>();

        for ( Parameter parameter : form )
        {
            terms.put( parameter.getName(), parameter.getValue() );
        }

        Integer from = null;
        Boolean exact = null;
        String repositoryId = null;
        Boolean expandVersion = Boolean.FALSE;
        Boolean collapseResults = Boolean.FALSE;

        if ( form.getFirstValue( "from" ) != null )
        {
            try
            {
                from = Integer.valueOf( form.getFirstValue( "from" ) );
            }
            catch ( NumberFormatException e )
            {
                from = null;
            }
        }

        int count = LUCENE_HIT_LIMIT;
        if ( form.getFirstValue( "count" ) != null )
        {
            try
            {
                // capping the possible count
                count = Math.min( LUCENE_HIT_LIMIT, Integer.valueOf( form.getFirstValue( "count" ) ) );
            }
            catch ( NumberFormatException e )
            {
                count = LUCENE_HIT_LIMIT;
            }
        }

        if ( form.getFirstValue( "repositoryId" ) != null )
        {
            repositoryId = form.getFirstValue( "repositoryId" );
        }

        if ( form.getFirstValue( "exact" ) != null )
        {
            exact = Boolean.valueOf( form.getFirstValue( "exact" ) );
        }

        if ( form.getFirstValue( "versionexpand" ) != null )
        {
            expandVersion = Boolean.valueOf( form.getFirstValue( "versionexpand" ) );
        }
        if ( form.getFirstValue( "collapseresults" ) != null )
        {
            collapseResults = Boolean.valueOf( form.getFirstValue( "collapseresults" ) );
        }

        // A little explanation about collapseResults, that might seems little bit awkward, since currently we have only
        // one column "collapsable" (the version), but before and maybe in the future that's not the case. So, here is
        // it:
        // the "collapseResults" is just a flag "do we allow collapse at all". It is just a shorthand to turn on or off
        // collapse generally
        // Let's assume we have columns colA, colB and colC collapsable. So, instead saying
        // expandColA=true,expandColB=true,expandColC=true,
        // it is just easy to say collapseresults=false
        // BUT, if collapseresults=true is sent by client, even then an "override" will happen if there is actually NO
        // ROW to collapse!
        // So: collapseresults=false EQUALS-TO expandColA=true & expandColB=true & expandColC=true
        if ( collapseResults )
        {
            // here we would like to have ANDed all the collapsable column flags and negated the result
            // currently we
            collapseResults = !( true && expandVersion ); // && expandColA && expandColB;
        }

        IteratorSearchResponse searchResult = null;

        SearchNGResponse result = new SearchNGResponse();

        int runCount = 0;

        while ( runCount < RETRIES )
        {
            try
            {
                List<ArtifactInfoFilter> filters = new ArrayList<ArtifactInfoFilter>();

                // we need to save this reference to later
                SystemWideLatestVersionCollector systemWideCollector = new SystemWideLatestVersionCollector();
                filters.add( systemWideCollector );

                RepositoryWideLatestVersionCollector repositoryWideCollector = null;

                if ( collapseResults )
                {
                    repositoryWideCollector = new RepositoryWideLatestVersionCollector();
                    filters.add( repositoryWideCollector );
                }

                try
                {
                    searchResult =
                        searchByTerms( terms, repositoryId, from, count, exact, expandVersion, collapseResults, filters,
                                       searchers );

                    if ( searchResult == null )
                    {
                        collapseResults = false;

                        continue;
                    }
                    else
                    {
                        repackIteratorSearchResponse( request, terms, result, collapseResults, from, count,
                            searchResult, systemWideCollector, repositoryWideCollector );

                        if ( !result.isTooManyResults() )
                        {
                            // if we had collapseResults ON, and the totalHits are larger than actual (filtered)
                            // results, and the actual result count is below COLLAPSE_OVERRIDE_TRESHOLD,
                            // and full result set is smaller than HIT_LIMIT
                            // then repeat without collapse
                            if ( collapseResults && result.getData().size() < searchResult.getTotalHitsCount()
                                && result.getData().size() < COLLAPSE_OVERRIDE_TRESHOLD
                                && searchResult.getTotalHitsCount() < GA_HIT_LIMIT )
                            {
                                collapseResults = false;

                                continue;
                            }
                        }
                    }
                }
                catch ( IOException e )
                {
                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
                }

                // we came here, so we break the while-loop, we got what we need
                break;
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                    "Repository to be searched does not exists!", e );
            }
            catch ( AlreadyClosedException e )
            {
                runCount++;

                getLogger().info(
                    "NexusIndexer issue (NEXUS-3702), we got AlreadyClosedException that happens when Reindexing or other \"indexer intensive\" task is running on instance while searching! Redoing search again." );

                if ( getLogger().isDebugEnabled() )
                {
                    // just keep it silent (DEBUG)
                    getLogger().debug( "Got AlreadyClosedException exception!", e );
                }

                result.setData( null );
            }
        }

        if ( result.getData() == null )
        {
            try
            {
                repackIteratorSearchResponse( request, terms, result, collapseResults, from, count,
                    IteratorSearchResponse.empty( null ), null, null );
            }
            catch ( NoSuchRepositoryException e )
            {
                // will not happen
            }
            catch ( IOException e )
            {
                // will not happen
            }

            getLogger().info(
                "Nexus issue (NEXUS-3702): Was unable to perform search "
                    + RETRIES
                    + " times, giving up, and lying about TooManyResults. Please retry to reproduce this with DEBUG logs and report this issue!" );
        }

        return result;
    }

    protected Logger getSearchDiagnosticLogger()
    {
        return searchDiagnosticLogger;
    }

    /* UT */ IteratorSearchResponse searchByTerms( final Map<String, String> terms, final String repositoryId,
                                                  final Integer from, final int count, final Boolean exact,
                                                  final Boolean expandVersion, final Boolean collapseResults,
                                                  final List<ArtifactInfoFilter> filters, final List<Searcher> searchers )
        throws NoSuchRepositoryException, ResourceException, IOException
    {
        try
        {
            for ( Searcher searcher : searchers )
            {
                if ( searcher.canHandle( terms ) )
                {
                    SearchType searchType = searcher.getDefaultSearchType();

                    if ( exact != null )
                    {
                        if ( exact )
                        {
                            searchType = SearchType.EXACT;
                        }
                        else
                        {
                            searchType = SearchType.SCORED;
                        }
                    }

                    // copy the list, to be able to modify it but do not interleave with potential recursive calls
                    List<ArtifactInfoFilter> actualFilters = new ArrayList<ArtifactInfoFilter>( filters );

                    if ( collapseResults )
                    {
                        // filters should affect only Keyword and GAVSearch!
                        // TODO: maybe we should left this to the given Searcher implementation to handle (like kw and gav
                        // searcher is)
                        // Downside would be that REST query params would be too far away from incoming call (too spread)
                        if ( searcher instanceof KeywordSearcher || searcher instanceof MavenCoordinatesSearcher )
                        {
                            UniqueArtifactFilterPostprocessor filter = new UniqueArtifactFilterPostprocessor();

                            filter.addField( MAVEN.GROUP_ID );
                            filter.addField( MAVEN.ARTIFACT_ID );
                            filter.addField( MAVEN.PACKAGING );
                            filter.addField( MAVEN.CLASSIFIER );
                            filter.addField( MAVEN.REPOSITORY_ID );

                            if ( Boolean.TRUE.equals( expandVersion ) )
                            {
                                filter.addField( MAVEN.VERSION );
                            }

                            // add this last, to collapse results but _after_ collectors collects!
                            actualFilters.add( filter );
                        }
                    }

                    final IteratorSearchResponse searchResponse =
                        searcher.flatIteratorSearch( terms, repositoryId, from, count, null, false, searchType,
                                                     actualFilters );

                    if ( searchResponse != null )
                    {
                        if ( collapseResults && searchResponse.getTotalHitsCount() < COLLAPSE_OVERRIDE_TRESHOLD )
                        {
                            searchResponse.close();

                            // FIXME: fix this, this is ugly
                            // We are returning null, to hint that we need UNCOLLAPSED search!
                            // Needed, to be able to "signal" the fact that we are overriding collapsed switch
                            // since we have to send it back in DTOs to REST client
                            return null;

                            // old code was a recursive call:
                            // this was a "collapsed" search (probably initiated by UI), and we have less then treshold hits
                            // override collapse
                            // return searchByTerms( terms, repositoryId, from, count, exact, expandVersion, false, filters
                            // );
                        }
                        else
                        {
                            return searchResponse;
                        }
                    }
                }
            }
        }
        catch ( IllegalArgumentException e )
        {
            if ( e.getCause() instanceof ParseException )
            {
                // NEXUS-4372: illegal query -> 400 response
                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getCause(),
                                                   getNexusErrorResponse( "search", e.getCause().getMessage() ) );
            }
            else
            {
                throw e;
            }
        }

        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Requested search query is not supported" );
    }

    protected void repackIteratorSearchResponse( Request request, Map<String, String> terms, SearchNGResponse response,
                                                 boolean collapsed, Integer from, int count,
                                                 IteratorSearchResponse iterator,
                                                 SystemWideLatestVersionCollector systemWideCollector,
                                                 RepositoryWideLatestVersionCollector repositoryWideCollector )
        throws NoSuchRepositoryException, IOException
    {
        response.setCollapsed( collapsed );

        response.setTotalCount( iterator.getTotalHitsCount() );

        response.setFrom( from == null ? -1 : from.intValue() );

        response.setCount( count == LUCENE_HIT_LIMIT ? -1 : count );

        // System.out.println( "** Query is \"" + iterator.getQuery().toString() + "\"." );

        try
        {
            if ( !response.isTooManyResults() )
            {
                // 1st pass, collect results
                LinkedHashMap<String, NexusNGArtifact> hits = new LinkedHashMap<String, NexusNGArtifact>();

                NexusNGArtifact artifact;

                float firstDocumentScore = -1f;

                float lastDocumentScore = -1f;

                final long startedAtMillis = System.currentTimeMillis();

                // 1sd pass, build first two level (no links), and actually consume the iterator and collectors will be
                // set
                for ( ArtifactInfo ai : iterator )
                {
                    final String key = ai.groupId + ":" + ai.artifactId + ":" + ai.version;

                    artifact = hits.get( key );

                    // System.out.println( "* " + ai.context + " : " + ai.toString() + " -- " + ai.getLuceneScore() +
                    // " -- "
                    // + ( artifact != null ? "F" : "N" ) );

                    if ( artifact == null )
                    {
                        if ( System.currentTimeMillis() - startedAtMillis > FIRST_LOOP_EXECUTION_TIME_LIMIT )
                        {
                            getSearchDiagnosticLogger().debug(
                                "Stopping delivering search results since we spent more than "
                                    + FIRST_LOOP_EXECUTION_TIME_LIMIT + " millis in 1st loop processing results." );

                            break;
                        }

                        // we stop if we delivered "most important" hits (change of relevance from 1st document we got)
                        if ( hits.size() > 10
                            && ( firstDocumentScore - ai.getLuceneScore() ) > DOCUMENT_TOP_RELEVANCE_HIT_CHANGE_THRESHOLD )
                        {
                            getSearchDiagnosticLogger().debug(
                                "Stopping delivering search results since we span "
                                    + DOCUMENT_TOP_RELEVANCE_HIT_CHANGE_THRESHOLD + " of score change (firstDocScore="
                                    + firstDocumentScore + ", currentDocScore=" + ai.getLuceneScore() + ")." );

                            break;
                        }

                        // we stop if we detect a "big drop" in relevance in relation to previous document's score
                        if ( hits.size() > 10 && lastDocumentScore > 0 )
                        {
                            if ( ( lastDocumentScore - ai.getLuceneScore() ) > DOCUMENT_RELEVANCE_HIT_CHANGE_THRESHOLD )
                            {
                                getSearchDiagnosticLogger().debug(
                                    "Stopping delivering search results since we hit a relevance drop bigger than "
                                        + DOCUMENT_RELEVANCE_HIT_CHANGE_THRESHOLD + " (lastDocScore="
                                        + lastDocumentScore + ", currentDocScore=" + ai.getLuceneScore() + ")." );

                                // the relevance change was big, so we stepped over "trash" results that are
                                // probably not relevant at all, just stop here then
                                break;
                            }
                        }

                        // we stop if we hit the GA limit
                        if ( ( hits.size() + 1 ) > GA_HIT_LIMIT )
                        {
                            getSearchDiagnosticLogger().debug(
                                "Stopping delivering search results since we hit a GA hit limit of " + GA_HIT_LIMIT
                                    + "." );

                            // check for HIT_LIMIT: if we are stepping it over, stop here
                            break;
                        }
                        else
                        {
                            artifact = new NexusNGArtifact();

                            artifact.setGroupId( ai.groupId );

                            artifact.setArtifactId( ai.artifactId );

                            artifact.setVersion( ai.version );

                            artifact.setHighlightedFragment( getMatchHighlightHtmlSnippet( ai ) );

                            hits.put( key, artifact );
                        }
                    }

                    Repository repository = getUnprotectedRepositoryRegistry().getRepository( ai.repository );

                    addRepositoryDetails( request, response, repository );

                    NexusNGArtifactHit hit = null;

                    for ( NexusNGArtifactHit artifactHit : artifact.getArtifactHits() )
                    {
                        if ( repository.getId().equals( artifactHit.getRepositoryId() ) )
                        {
                            hit = artifactHit;

                            break;
                        }
                    }

                    if ( hit == null )
                    {
                        hit = new NexusNGArtifactHit();

                        hit.setRepositoryId( repository.getId() );

                        // if collapsed, we add links in 2nd pass, otherwise here
                        if ( !collapsed )
                        {
                            // we are adding the POM link "blindly", unless packaging is POM,
                            // since the it will be added below the "usual" way
                            if ( !"pom".equals( ai.packaging ) )
                            {
                                NexusNGArtifactLink link =
                                    createNexusNGArtifactLink( request, ai.repository, ai.groupId, ai.artifactId,
                                        ai.version, "pom", null );

                                // add the POM link
                                hit.addArtifactLink( link );
                            }
                        }

                        // we just created it, add it
                        artifact.addArtifactHit( hit );
                    }

                    if ( !collapsed )
                    {
                        boolean needsToBeAdded = true;

                        for ( NexusNGArtifactLink link : hit.getArtifactLinks() )
                        {
                            if ( StringUtils.equals( link.getClassifier(), ai.classifier )
                                && StringUtils.equals( link.getExtension(), ai.fextension ) )
                            {
                                needsToBeAdded = false;

                                break;
                            }
                        }

                        if ( needsToBeAdded )
                        {
                            NexusNGArtifactLink link =
                                createNexusNGArtifactLink( request, ai.repository, ai.groupId, ai.artifactId,
                                    ai.version, ai.fextension, ai.classifier );

                            hit.addArtifactLink( link );
                        }
                    }

                    if ( firstDocumentScore < 0 )
                    {
                        firstDocumentScore = ai.getLuceneScore();
                    }

                    lastDocumentScore = ai.getLuceneScore();
                }

                // summary:
                getSearchDiagnosticLogger().debug(
                    "Query terms \"" + terms + "\" (LQL \"" + iterator.getQuery() + "\") matched total of "
                        + iterator.getTotalHitsCount() + " records, " + iterator.getTotalProcessedArtifactInfoCount()
                        + " records were processed out of those, resulting in " + hits.size()
                        + " unique GA records. Lucene scored documents first=" + firstDocumentScore + ", last="
                        + lastDocumentScore + ". Main processing loop took "
                        + ( System.currentTimeMillis() - startedAtMillis ) + " ms." );

                // 2nd pass, set versions
                for ( NexusNGArtifact artifactNg : hits.values() )
                {
                    final String systemWideCollectorKey =
                        systemWideCollector.getKey( artifactNg.getGroupId(), artifactNg.getArtifactId() );

                    LatestVersionHolder systemWideHolder = systemWideCollector.getLVHForKey( systemWideCollectorKey );

                    if ( systemWideHolder != null )
                    {
                        if ( systemWideHolder.getLatestSnapshot() != null )
                        {
                            artifactNg.setLatestSnapshot( systemWideHolder.getLatestSnapshot().toString() );

                            artifactNg.setLatestSnapshotRepositoryId( systemWideHolder.getLatestSnapshotRepositoryId() );
                        }

                        if ( systemWideHolder.getLatestRelease() != null )
                        {
                            artifactNg.setLatestRelease( systemWideHolder.getLatestRelease().toString() );

                            artifactNg.setLatestReleaseRepositoryId( systemWideHolder.getLatestReleaseRepositoryId() );
                        }
                    }

                    // add some "touche" on 1st level
                    if ( collapsed )
                    {
                        // set the top level version to one of the latest ones
                        if ( artifactNg.getLatestRelease() != null )
                        {
                            artifactNg.setVersion( artifactNg.getLatestRelease() );
                        }
                        else
                        {
                            artifactNg.setVersion( artifactNg.getLatestSnapshot() );
                        }

                        // "create" the links now
                        for ( NexusNGArtifactHit hit : artifactNg.getArtifactHits() )
                        {
                            final String repositoryWideCollectorKey =
                                repositoryWideCollector.getKey( hit.getRepositoryId(), artifactNg.getGroupId(),
                                    artifactNg.getArtifactId() );

                            LatestECVersionHolder repositoryWideHolder =
                                repositoryWideCollector.getLVHForKey( repositoryWideCollectorKey );

                            if ( repositoryWideHolder != null )
                            {
                                String versionToSet = null;

                                // do we have a "latest release" version?
                                if ( repositoryWideHolder.getLatestRelease() != null )
                                {
                                    versionToSet = repositoryWideHolder.getLatestRelease().toString();
                                }
                                else
                                {
                                    versionToSet = repositoryWideHolder.getLatestSnapshot().toString();
                                }

                                // add POM link
                                NexusNGArtifactLink pomLink =
                                    createNexusNGArtifactLink( request, hit.getRepositoryId(), artifactNg.getGroupId(),
                                        artifactNg.getArtifactId(), versionToSet, "pom", null );

                                hit.addArtifactLink( pomLink );

                                // TODO: order!
                                // add main artifact link
                                // add everything else

                                // make the list by joining two collections
                                // rationale: in case of reposes, only one of these will be populated, other will be
                                // empty
                                // but in case of mixed policy (like group), probably both will exist
                                // TODO: this will not work like it in groups, since then the versions will mismatch!
                                ArrayList<ECHolder> ecHolders =
                                    new ArrayList<ECHolder>( repositoryWideHolder.getReleaseECHolders() );
                                ecHolders.addAll( repositoryWideHolder.getSnapshotECHolders() );

                                for ( ECHolder holder : ecHolders )
                                {
                                    // add non-poms only, since we added POMs above
                                    if ( !"pom".equals( holder.getExtension() ) )
                                    {
                                        NexusNGArtifactLink link =
                                            createNexusNGArtifactLink( request, hit.getRepositoryId(),
                                                artifactNg.getGroupId(), artifactNg.getArtifactId(), versionToSet,
                                                holder.getExtension(), holder.getClassifier() );

                                        hit.addArtifactLink( link );
                                    }
                                }
                            }
                        }
                    }
                }

                response.setData( new ArrayList<NexusNGArtifact>( hits.values() ) );
            }
        }
        finally
        {
            iterator.close();
        }

        response.setTooManyResults( iterator.getTotalHitsCount() > count );
    }

    protected void addRepositoryDetails( Request request, SearchNGResponse response, Repository repository )
    {
        boolean add = true;

        for ( NexusNGRepositoryDetail repoDetail : response.getRepoDetails() )
        {
            if ( repoDetail.getRepositoryId().equals( repository.getId() ) )
            {
                add = false;
                break;
            }
        }

        if ( add )
        {
            NexusNGRepositoryDetail repoDetail = new NexusNGRepositoryDetail();

            repoDetail.setRepositoryId( repository.getId() );

            repoDetail.setRepositoryName( repository.getName() );

            repoDetail.setRepositoryURL( createRepositoryReference( request, repository.getId() ).getTargetRef().toString() );

            repoDetail.setRepositoryContentClass( repository.getRepositoryContentClass().getId() );

            repoDetail.setRepositoryKind( extractRepositoryKind( repository ) );

            MavenRepository mavenRepo = repository.adaptToFacet( MavenRepository.class );

            if ( mavenRepo != null )
            {
                repoDetail.setRepositoryPolicy( mavenRepo.getRepositoryPolicy().name() );
            }

            response.addRepoDetail( repoDetail );
        }
    }

    protected NexusNGArtifactLink createNexusNGArtifactLink( final Request request, final String repositoryId,
                                                             final String groupId, final String artifactId,
                                                             final String version, final String extension,
                                                             final String classifier )
    {
        NexusNGArtifactLink link = new NexusNGArtifactLink();

        link.setExtension( extension );

        link.setClassifier( classifier );

        return link;
    }

    protected String extractRepositoryKind( Repository repository )
    {
        RepositoryKind kind = repository.getRepositoryKind();

        if ( kind.isFacetAvailable( HostedRepository.class ) )
        {
            return "hosted";
        }
        else if ( kind.isFacetAvailable( ProxyRepository.class ) )
        {
            return "proxy";
        }
        else if ( kind.isFacetAvailable( GroupRepository.class ) )
        {
            return "group";
        }
        else if ( kind.isFacetAvailable( ShadowRepository.class ) )
        {
            return "virtual";
        }
        else
        {
            // huh?
            return repository.getRepositoryKind().getMainFacet().getName();
        }
    }
}
