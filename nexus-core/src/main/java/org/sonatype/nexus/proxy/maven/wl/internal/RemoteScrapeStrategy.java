/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteStrategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyFailedException;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyResult;
import org.sonatype.nexus.proxy.maven.wl.internal.scrape.Page;
import org.sonatype.nexus.proxy.maven.wl.internal.scrape.ScrapeContext;
import org.sonatype.nexus.proxy.maven.wl.internal.scrape.Scraper;
import org.sonatype.nexus.proxy.storage.remote.httpclient.HttpClientManager;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

/**
 * Remote scrape strategy.
 * 
 * @author cstamas
 */
@Named( RemoteScrapeStrategy.ID )
@Singleton
public class RemoteScrapeStrategy
    extends AbstractRemoteStrategy
    implements RemoteStrategy
{
    protected static final String ID = "scrape";

    private final WLConfig config;

    private final HttpClientManager httpClientManager;

    private final List<Scraper> scrapers;

    /**
     * Constructor.
     * 
     * @param config
     * @param httpClientManager
     * @param scrapers
     */
    @Inject
    public RemoteScrapeStrategy( final WLConfig config, final HttpClientManager httpClientManager,
                                 final List<Scraper> scrapers )
    {
        // "last resort"
        super( Integer.MAX_VALUE, ID );
        this.config = checkNotNull( config );
        this.httpClientManager = checkNotNull( httpClientManager );
        this.scrapers = checkNotNull( scrapers );
    }

    @Override
    public StrategyResult discover( final MavenProxyRepository mavenProxyRepository )
        throws StrategyFailedException
    {
        getLogger().debug( "Remote scrape on {} tried",
            RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
        // get client configured in same way as proxy is using it
        final HttpClient httpClient =
            httpClientManager.create( mavenProxyRepository, mavenProxyRepository.getRemoteStorageContext() );
        final String remoteRepositoryRootUrl = mavenProxyRepository.getRemoteUrl();
        if ( isMarkedForNoScrape( httpClient, remoteRepositoryRootUrl ) )
        {
            getLogger().debug( "Remote {} marked as no-scrape, giving up.",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
            throw new StrategyFailedException( "Remote forbids scraping, is flagged as \"no-scrape\"." );
        }
        try
        {
            final ScrapeContext context =
                new ScrapeContext( httpClient, remoteRepositoryRootUrl, config.getRemoteScrapeDepth() );
            final Page rootPage = Page.getPageFor( context, remoteRepositoryRootUrl );
            final ArrayList<Scraper> appliedScrapers = new ArrayList<Scraper>( scrapers );
            Collections.sort( appliedScrapers, new PriorityOrderingComparator<Scraper>() );
            for ( Scraper scraper : appliedScrapers )
            {
                getLogger().debug( "Remote scraping {} with Scraper {}",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), scraper.getId() );
                scraper.scrape( context, rootPage );
                if ( context.isStopped() )
                {
                    if ( context.isSuccessful() )
                    {
                        getLogger().debug( "Remote scraping {} with Scraper {} succeeded.",
                            RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), scraper.getId() );
                        return new StrategyResult( context.getMessage(), context.getEntrySource() );
                    }
                    else
                    {
                        getLogger().debug( "Remote scraping {} with Scraper {} stopped execution.",
                            RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), scraper.getId() );
                        throw new StrategyFailedException( context.getMessage() );
                    }
                }
                getLogger().debug( "Remote scraping {} with Scraper {} skipped.",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), scraper.getId() );
            }

            getLogger().debug( "Not possible remote scrape of {}, no scraper succeeded.",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
            throw new StrategyFailedException( "No scraper was able to scrape remote (or remote prevents scraping)." );
        }
        catch ( IOException e )
        {
            throw new StrategyFailedException( e.getMessage(), e );
        }
    }

    // ==

    protected boolean isMarkedForNoScrape( final HttpClient httpClient, final String remoteRepositoryRootUrl )
    {
        final List<String> noscrapeFlags = config.getRemoteNoScrapeFlagPaths();
        for ( String noscrapeFlag : noscrapeFlags )
        {
            while ( noscrapeFlag.startsWith( "/" ) )
            {
                noscrapeFlag = noscrapeFlag.substring( 1 );
            }
            final String flagRemoteUrl = remoteRepositoryRootUrl + noscrapeFlag;
            HttpResponse response = null;
            try
            {
                final HttpHead head = new HttpHead( flagRemoteUrl );
                response = httpClient.execute( head );
                if ( response.getStatusLine().getStatusCode() > 199 && response.getStatusLine().getStatusCode() < 300 )
                {
                    return true;
                }
            }
            catch ( IOException e )
            {
                // ahem, let's skip over this
                // remote should TELL not TRY TO TELL do not scrape me
            }
            finally
            {
                if ( response != null )
                {
                    EntityUtils.consumeQuietly( response.getEntity() );
                }
            }
        }
        return false;
    }
}
