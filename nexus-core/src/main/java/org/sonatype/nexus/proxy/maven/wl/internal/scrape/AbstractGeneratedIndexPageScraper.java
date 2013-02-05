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
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.ParentOMatic.Payload;
import org.sonatype.nexus.util.Node;

/**
 * Scraper for remote Nexus instances that will scrape only if remote is for sure recognized as Nexus instance, and URL
 * points to a hosted repository.
 * 
 * @author cstamas
 */
public abstract class AbstractGeneratedIndexPageScraper
    extends AbstractScraper
{
    protected AbstractGeneratedIndexPageScraper( final int priority, final String id )
    {
        super( priority, id );
    }

    @Override
    protected RemoteDetectionResult detectRemoteRepository( final ScrapeContext context, final Page page )
    {
        // cheap checks first, to quickly eliminate target without doing any remote requests
        if ( page.getHttpResponse().getStatusLine().getStatusCode() == 200 )
        {
            final Elements elements = page.getDocument().getElementsByTag( "a" );
            if ( !elements.isEmpty() )
            {
                // get "template" parent link
                final Element templateParentLink = getParentDirectoryElement( page );
                // get the page parent link (note: usually it's 1st elem, but HTTPD for example has extra links for
                // column
                // sorting
                for ( Element element : elements )
                {
                    // if text is same and abs URLs points to same place, we got it
                    if ( templateParentLink.text().equals( element.text() )
                        && templateParentLink.absUrl( "href" ).equals( element.absUrl( "href" ) ) )
                    {
                        return RemoteDetectionResult.RECOGNIZED_SHOULD_BE_SCRAPED;
                    }
                }
            }
        }

        // um, we were not totally positive, this might be some web server with index page similar to Nexus one
        return RemoteDetectionResult.UNRECOGNIZED;
    }

    @Override
    protected List<String> diveIn( final ScrapeContext context, final Page page )
        throws IOException
    {
        // we use the great and all-mighty ParentOMatic
        final ParentOMatic parentOMatic = new ParentOMatic();
        diveIn( context, page, 0, parentOMatic, parentOMatic.getRoot() );
        // TODO: cases like central, that would allow to be scraped with 0 results
        final List<String> entries = parentOMatic.getAllLeafPaths();
        return entries;
    }

    protected void diveIn( final ScrapeContext context, final Page page, final int currentDepth,
                           final ParentOMatic parentOMatic, final Node<Payload> currentNode )
        throws IOException
    {
        // entry protection
        if ( currentDepth >= context.getScrapeDepth() )
        {
            return;
        }
        final Elements elements = page.getDocument().getElementsByTag( "a" );
        final List<String> pathElements = currentNode.getPathElements();
        final String currentPath = currentNode.getPath();
        for ( Element element : elements )
        {
            if ( isDeeperRepoLink( context, pathElements, element ) )
            {
                if ( element.text().startsWith( "." ) )
                {
                    // skip hidden paths
                    continue;
                }
                final Node<Payload> newSibling = parentOMatic.addPath( currentPath + "/" + element.text() );
                if ( element.absUrl( "href" ).endsWith( "/" ) )
                {
                    // "cut" recursion preemptively
                    final int siblingDepth = currentDepth + 1;
                    if ( siblingDepth < context.getScrapeDepth() )
                    {
                        final String newSiblingEncodedUrl =
                            getRemoteUrlForRepositoryPath( context, newSibling.getPathElements() ) + "/";
                        final Page siblingPage = Page.getPageFor( context, newSiblingEncodedUrl );
                        if ( siblingPage.getHttpResponse().getStatusLine().getStatusCode() == 200 )
                        {
                            diveIn( context, siblingPage, siblingDepth, parentOMatic, newSibling );
                        }
                        else
                        {
                            throw new IOException( "Unexpected response from remote repository URL " + page.getUrl()
                                + " : " + page.getHttpResponse().getStatusLine().toString() );
                        }
                    }
                }
            }
        }
    }

    protected boolean isDeeperRepoLink( final ScrapeContext context, final List<String> pathElements, final Element aTag )
    {
        // HTTPD and some others have anchors for sorting, their rel URL start with "?"
        if ( aTag.attr( "href" ).startsWith( "?" ) )
        {
            return false;
        }
        final String linkAbsoluteUrl = aTag.absUrl( "href" );
        final String currentUrl = getRemoteUrlForRepositoryPath( context, pathElements );
        return linkAbsoluteUrl.startsWith( currentUrl );
    }

    protected abstract Element getParentDirectoryElement( final Page page );
}
