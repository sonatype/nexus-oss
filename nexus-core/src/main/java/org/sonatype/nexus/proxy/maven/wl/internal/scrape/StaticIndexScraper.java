package org.sonatype.nexus.proxy.maven.wl.internal.scrape;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * Scraper for remote HTTP servers.
 * 
 * @author cstamas
 */
@Named( StaticIndexScraper.ID )
@Singleton
public class StaticIndexScraper
    extends AbstractGeneratedIndexPageScraper
{
    protected static final String ID = "static-index";

    /**
     * Default constructor.
     */
    public StaticIndexScraper()
    {
        super( 1000, ID );
    }

    @Override
    protected String getTargetedServer()
    {
        return "Static Index Page";
    }

    @Override
    protected Element getParentDirectoryElement()
    {
        return Jsoup.parse( "<a href=\"../\">Parent Directory</a>" );
    }
}
