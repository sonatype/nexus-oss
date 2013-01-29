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

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Scraper for remote Nexus instances that will scrape only if remote is for sure recognized as Nexus instance, and URL
 * points to a hosted repository.
 * 
 * @author cstamas
 */
@Named( NexusScraper.ID )
@Singleton
public class NexusScraper
    extends AbstractGeneratedIndexPageScraper
{
    protected static final String ID = "nexus";

    /**
     * Default constructor.
     */
    public NexusScraper()
    {
        super( 1000, ID ); // 1st by popularity
    }

    @Override
    protected String getTargetedServer()
    {
        return "Sonatype Nexus";
    }

    @Override
    protected Element getParentDirectoryElement( final ScrapeContext context, final Document document )
    {
        final Document doc = Jsoup.parseBodyFragment( "<a href=\"../\">Parent Directory</a>", document.baseUri() );
        return doc.getElementsByTag( "a" ).first();
    }

    @Override
    protected RemoteDetectionResult detectRemoteRepository( final ScrapeContext context,
                                                            final HttpResponse rootResponse, final Document rootDocument )
    {
        final RemoteDetectionResult result = super.detectRemoteRepository( context, rootResponse, rootDocument );
        if ( RemoteDetectionResult.RECOGNIZED_SHOULD_BE_SCRAPED == result )
        {
            try
            {
                // so index page looks like Nexus index page, let's see about repo metadata
                // this is not cheap, as we are doing extra HTTP requests to get it
                final Document response =
                    getDocumentFor( context, context.getRemoteRepositoryRootUrl() + ".meta/repository-metadata.xml" );
                final Elements url = response.getElementsByTag( "url" ); // all nexus MD has this. sanity
                final Elements localUrl = response.getElementsByTag( "localUrl" ); // only proxies
                final Elements memberRepositories = response.getElementsByTag( "memberRepositories" ); // only groups
                if ( !url.isEmpty() && localUrl.isEmpty() && memberRepositories.isEmpty() )
                {
                    // we are sure it is a nexus hosted repo
                    return RemoteDetectionResult.RECOGNIZED_SHOULD_BE_SCRAPED;
                }
                else
                {
                    // is a proxy or a group, do not scrape
                    return RemoteDetectionResult.RECOGNIZED_SHOULD_NOT_BE_SCRAPED;
                }
            }
            catch ( IOException e )
            {
                // hm, either not exists or whoknows, just ignore this as Nexus must have it and should return it
                getLogger().debug( "Problem during fetch of /.meta/repository-metadata.xml", e );
            }
        }
        // um, we were not totally positive, this might be some web server with index page similar to Nexus one
        return RemoteDetectionResult.UNRECOGNIZED;
    }
}
