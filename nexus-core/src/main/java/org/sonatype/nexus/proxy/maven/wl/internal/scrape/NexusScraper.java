package org.sonatype.nexus.proxy.maven.wl.internal.scrape;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

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
@Named( NexusScraper.ID )
@Singleton
public class NexusScraper
    extends AbstractScraper
{
    protected static final String ID = "nexus";

    /**
     * Default constructor.
     */
    public NexusScraper()
    {
        super( 100, ID );
    }

    @Override
    protected String getTargetedServer()
    {
        return "Sonatype Nexus";
    }

    @Override
    protected RemoteDetectionResult detectRemoteRepository( final ScrapeContext context )
    {
        // cheap checks first, to quickly eliminate target without doing any remote requests
        final Elements elements = context.getRemoteRepositoryRootDocument().getElementsByTag( "a" );
        if ( elements.isEmpty() )
        {
            // not even close to nexus index page format, as it always have at least one element
            return RemoteDetectionResult.UNRECOGNIZED;
        }
        final Element firstLinkElement = elements.get( 0 );
        if ( "Parent Directory".equals( firstLinkElement.text() ) && "../".equals( firstLinkElement.attr( "href" ) ) )
        {
            try
            {
                // so index page looks like Nexus index page, let's see about repo metadata
                // this is not cheap, as we are doing extra HTTP requests to get it
                final Document response = getDocumentFor( context, "/.meta/repository-metadata.xml" );
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
            }
        }

        // um, we were not totally positive, this might be some web server with index page similar to Nexus one
        return RemoteDetectionResult.UNRECOGNIZED;
    }

    @Override
    protected EntrySource diveIn( final ScrapeContext context )
        throws IOException
    {
        // we use the great and all-mighty ParentOMatic
        final ParentOMatic parentOMatic = new ParentOMatic();
        diveIn( context, context.getRemoteRepositoryRootDocument(), 0, parentOMatic, parentOMatic.getRoot() );
        return new ArrayListEntrySource( parentOMatic.getAllLeafPaths() );
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
        for ( Element element : elements )
        {
            if ( "Parent Directory".equals( element.text() ) && "../".equals( element.attr( "href" ) ) )
            {
                continue; // skip it, it's always the 1st link on all Nexus index pages
            }
            final Node<Payload> newSibling = parentOMatic.addPath( currentNode.getPath() + "/" + element.text() );
            if ( element.attr( "href" ).endsWith( "/" ) )
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
