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
        super( 100, ID );
    }

    @Override
    protected String getTargetedServer()
    {
        return "Sonatype Nexus";
    }

    @Override
    protected Element getParentDirectoryElement()
    {
        return Jsoup.parse( "<a href=\"../\">Parent Directory</a>" );
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
}
