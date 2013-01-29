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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jsoup.nodes.Document;

/**
 * Scraper for remote AmazonS3 hosted repositories.
 * 
 * @author cstamas
 */
@Named( AmazonS3IndexScraper.ID )
@Singleton
public class AmazonS3IndexScraper
    extends AbstractScraper
{
    protected static final String ID = "amazons3-index";

    /**
     * Default constructor.
     */
    public AmazonS3IndexScraper()
    {
        super( 4000, ID ); // 4th by popularity
    }

    @Override
    protected String getTargetedServer()
    {
        return "Amazon S3";
    }

    @Override
    protected RemoteDetectionResult detectRemoteRepository( final ScrapeContext context,
                                                            final HttpResponse rootResponse, final Document rootDocument )
    {
        final boolean hasAmzRequestIdHeader = rootResponse.getFirstHeader( "x-amz-request-id" ) != null;
        final Header serverHeader = rootResponse.getFirstHeader( "Server" );
        if ( hasAmzRequestIdHeader && serverHeader != null && serverHeader.getValue() != null
            && serverHeader.getValue().startsWith( "AmazonS3" ) )
        {
            return RemoteDetectionResult.RECOGNIZED_SHOULD_BE_SCRAPED;
        }
        return RemoteDetectionResult.UNRECOGNIZED;
    }

    @Override
    protected void diveIn( ScrapeContext context, HttpResponse rootResponse, Document rootDocument )
        throws IOException
    {
        // TODO:
        context.stop( "Remote recognized as AmazonS3, but scraper is not yet implemented!" );
    }
}
