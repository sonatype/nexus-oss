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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.sisu.litmus.testsupport.inject.InjectedTestSupport;

public class AmazonS3IndexScraperTest
    extends InjectedTestSupport
{
    private AmazonS3IndexScraper s3scraper;

    @Before
    public void prepare()
    {
        s3scraper = (AmazonS3IndexScraper) lookup( Scraper.class, AmazonS3IndexScraper.ID );
    }

    @Test
    @Ignore
    public void smoke()
        throws IOException
    {
        final HttpClient httpClient = new DefaultHttpClient();
        final String remoteUrl = "http://spring-roo-repository.springsource.org/release/";
        final HttpGet get = new HttpGet( remoteUrl );
        final HttpResponse response = httpClient.execute( get );
        final Document document = Jsoup.parse( response.getEntity().getContent(), null, remoteUrl );
        final ScrapeContext context = new ScrapeContext( httpClient, remoteUrl, 2 );
        final Page page = new Page( remoteUrl, response, document );
        s3scraper.scrape( context, page );

        if ( context.isSuccessful() )
        {
            System.out.println( context.getEntrySource().readEntries() );
        }
        else
        {
            if ( context.isStopped() )
            {
                System.out.println( context.getMessage() );
            }
            else
            {
                System.out.println( "Huh?" );
            }
        }
    }
}
