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
package org.sonatype.nexus.proxy.maven.wl.internal.scrape;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sonatype.nexus.proxy.maven.wl.internal.AbstractPrioritized;
import org.sonatype.nexus.util.PathUtils;

import com.google.common.base.Function;

/**
 * Abstract class for {@link Scraper} implementations.
 * 
 * @author cstamas
 */
public abstract class AbstractScraper
    extends AbstractPrioritized
    implements Scraper
{
    /**
     * Detection results.
     */
    public static enum RemoteDetectionResult
    {
        /**
         * Remote not recognized, this scraper cannot do anything with it.
         */
        UNRECOGNIZED,

        /**
         * Recognized and we are sure it can and should be scraped.
         */
        RECOGNIZED_SHOULD_BE_SCRAPED,

        /**
         * Recognized and we are sure it should not be scraped.
         */
        RECOGNIZED_SHOULD_NOT_BE_SCRAPED;
    }

    private final String id;

    protected AbstractScraper( final int priority, final String id )
    {
        super( priority );
        this.id = checkNotNull( id );
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void scrape( final ScrapeContext context, final HttpResponse rootResponse, final Document rootDocument )
        throws IOException
    {
        final RemoteDetectionResult detectionResult = detectRemoteRepository( context, rootResponse, rootDocument );
        switch ( detectionResult )
        {
            case RECOGNIZED_SHOULD_BE_SCRAPED:
                getLogger().debug( "Remote repository on URL={} recognized as {}, scraping it...",
                    context.getRemoteRepositoryRootUrl(), getTargetedServer() );
                diveIn( context, rootResponse, rootDocument );
                break;

            case RECOGNIZED_SHOULD_NOT_BE_SCRAPED:
                getLogger().debug( "Remote repository on URL={} recognized as {}, but must not be scraped, stopping.",
                    context.getRemoteRepositoryRootUrl(), getTargetedServer() );
                context.stop( "Remote recognized as " + getTargetedServer() + ", but is not a hosted repository." );
                break;

            default:
                // not recognized, just continue with next Scraper
                getLogger().debug( "Remote repository on URL={} not recognized as {}, skipping it.",
                    context.getRemoteRepositoryRootUrl(), getTargetedServer() );
                break;
        }
    }

    // ==

    protected abstract String getTargetedServer();

    protected abstract RemoteDetectionResult detectRemoteRepository( final ScrapeContext context,
                                                                     final HttpResponse rootResponse,
                                                                     final Document rootDocument );

    protected abstract void diveIn( final ScrapeContext context, final HttpResponse rootResponse,
                                    final Document rootDocument )
        throws IOException;

    // ==

    protected HttpResponse getHttpResponseFor( final ScrapeContext context, final String url )
        throws IOException
    {
        // TODO: detect redirects
        final HttpGet get = new HttpGet( url );
        return context.getHttpClient().execute( get );
    }

    protected Document getDocumentFor( final ScrapeContext context, final String url )
        throws IOException
    {
        getLogger().debug( "Scraping URL {}", url );
        // TODO: detect redirects
        final HttpGet get = new HttpGet( url );
        HttpResponse response = context.getHttpClient().execute( get );
        try
        {
            if ( response.getStatusLine().getStatusCode() == 200 )
            {
                return Jsoup.parse( response.getEntity().getContent(), null, url );
            }
            else
            {
                throw new IOException( "Unexpected response from remote repository URL " + url + " : "
                    + response.getStatusLine().toString() );
            }
        }
        finally
        {
            EntityUtils.consumeQuietly( response.getEntity() );
        }
    }

    protected String getRemoteUrlForRepositoryPath( final ScrapeContext context, final List<String> pathElements )
    {
        // explanation: Nexus "repository paths" are always absolute, using "/" as separators and starting with "/"
        // but, the repo remote URL comes from Nexus config, and Nexus always "normalizes" the URL and it always ends
        // with "/"
        String sp = PathUtils.pathFrom( pathElements, URLENCODE );
        while ( sp.startsWith( "/" ) )
        {
            sp = sp.substring( 1 );
        }
        return context.getRemoteRepositoryRootUrl() + sp;
    }

    // ==

    private static final UrlEncode URLENCODE = new UrlEncode();

    private static final class UrlEncode
        implements Function<String, String>
    {
        @Override
        public String apply( @Nullable String input )
        {
            try
            {
                // See
                // http://en.wikipedia.org/wiki/Percent-encoding
                return URLEncoder.encode( input, "UTF-8" ).replace( "+", "%20" );
            }
            catch ( UnsupportedEncodingException e )
            {
                // Platform not supporting UTF-8? Unlikely.
                throw new IllegalStateException( "WAT?", e );
            }
        }
    }
}
