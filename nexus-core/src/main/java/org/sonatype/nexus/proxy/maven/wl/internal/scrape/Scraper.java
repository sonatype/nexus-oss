package org.sonatype.nexus.proxy.maven.wl.internal.scrape;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.jsoup.nodes.Document;
import org.sonatype.nexus.proxy.maven.wl.discovery.Prioritized;

/**
 * Scraper component, implementations should "specialise" for some remote target, like Nexus or HTTPd.
 * 
 * @author cstamas
 */
public interface Scraper
    extends Prioritized
{
    /**
     * Returns the unique ID of the scraper, never {@code null}.
     * 
     * @return the ID of the scraper.
     */
    String getId();

    /**
     * Tries to scrape. Scraper should flag the {@link ScrapeContext} if it wants to stop the processing.
     * 
     * @param context
     * @param rootResponse
     * @param rootDocument
     * @throws IOException
     */
    void scrape( ScrapeContext context, HttpResponse rootResponse, Document rootDocument )
        throws IOException;
}
