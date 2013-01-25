package org.sonatype.nexus.proxy.maven.wl.internal.scrape;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.internal.ArrayListEntrySource;
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
    protected RemoteDetectionResult detectRemoteRepository( final ScrapeContext context,
                                                            final HttpResponse rootResponse, final Document rootDocument )
    {
        // cheap checks first, to quickly eliminate target without doing any remote requests
        final Elements elements = rootDocument.getElementsByTag( "a" );
        if ( !elements.isEmpty() && getParentDirectoryElement().text().equals( elements.get( 0 ).text() ) )
        {
            return RemoteDetectionResult.RECOGNIZED_SHOULD_BE_SCRAPED;
        }

        // um, we were not totally positive, this might be some web server with index page similar to Nexus one
        return RemoteDetectionResult.UNRECOGNIZED;
    }

    @Override
    protected void diveIn( final ScrapeContext context, final HttpResponse rootResponse, final Document rootDocument )
        throws IOException
    {
        // we use the great and all-mighty ParentOMatic
        final ParentOMatic parentOMatic = new ParentOMatic();
        diveIn( context, rootDocument, 0, parentOMatic, parentOMatic.getRoot() );
        if ( !context.isStopped() )
        {
            // TODO: cases like central, that would allow to be scraped with 0 results
            final EntrySource entrySource = new ArrayListEntrySource( parentOMatic.getAllLeafPaths() );
            context.stop( entrySource, "Remote recognized as " + getTargetedServer() + "." );
        }
    }

    protected void diveIn( final ScrapeContext context, final Document document, final int currentDepth,
                           final ParentOMatic parentOMatic, final Node<Payload> currentNode )
        throws IOException
    {
        // entry protection
        if ( currentDepth >= context.getScrapeDepth() )
        {
            return;
        }
        final Elements elements = document.getElementsByTag( "a" );
        final String currentPath = currentNode.getPath();
        for ( Element element : elements )
        {
            if ( isDeeperRepoLink( context, currentPath, element ) )
            {
                final Node<Payload> newSibling = parentOMatic.addPath( currentPath + "/" + element.text() );
                if ( isDeeperRepoCollectionLink( context, currentPath, element ) )
                {
                    // "cut" recursion preemptively
                    final int siblingDepth = currentDepth + 1;
                    if ( siblingDepth < context.getScrapeDepth() )
                    {
                        final Document siblingDocument = getDocumentFor( context, newSibling.getPath() + "/" );
                        diveIn( context, siblingDocument, siblingDepth, parentOMatic, newSibling );
                    }
                }
            }
        }
    }

    protected boolean isDeeperRepoLink( final ScrapeContext context, final String currentRepoPath, final Element aTag )
    {
        final String linkAbsoluteUrl = aTag.absUrl( "href" );
        final String currentUrl = getRemoteUrlForRepositoryPath( context, currentRepoPath );
        return linkAbsoluteUrl.startsWith( currentUrl );
    }

    protected boolean isDeeperRepoCollectionLink( final ScrapeContext context, final String currentRepoPath,
                                                  final Element aTag )
    {
        return aTag.attr( "href" ).endsWith( "/" );
    }

    protected abstract Element getParentDirectoryElement();
}
