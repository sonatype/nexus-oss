package org.sonatype.nexus.proxy.maven.wl.internal.scrape;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * The page fetched from remote and preprocessed by JSoup.
 * 
 * @author cstamas
 * @since 2.4
 */
public class Page
{
    private final String url;

    private final HttpResponse httpResponse;

    private final Document document;

    /**
     * Constructor.
     * 
     * @param url the URL from where this page was fetched.
     * @param httpResponse the HTTP response for this page (with consumed body!).
     * @param document the JSoup document for this page.
     */
    public Page( final String url, final HttpResponse httpResponse, final Document document )
    {
        this.url = checkNotNull( url );
        this.httpResponse = checkNotNull( httpResponse );
        this.document = checkNotNull( document );
    }

    /**
     * The URL from where this page was fetched.
     * 
     * @return the URL from where this page was fetched.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * The HTTP response for this page (response body is consumed!). To check stuff like headers.
     * 
     * @return the HTTP response of page.
     */
    public HttpResponse getHttpResponse()
    {
        return httpResponse;
    }

    /**
     * The body of the page, parsed by JSoup.
     * 
     * @return the page body document.
     */
    public Document getDocument()
    {
        return document;
    }

    // ==

    /**
     * Checks if header with given name is present.
     * 
     * @param headerName
     * @return {@code true} if header with given name is present.
     */
    protected boolean hasHeader( final String headerName )
    {
        return getHttpResponse().getFirstHeader( headerName ) != null;
    }

    /**
     * Checks if header with given name is present and start with given value.
     * 
     * @param headerName
     * @param value
     * @return {@code true} if header with given name is present and starts with given value.
     */
    protected boolean hasHeaderAndStartsWith( final String headerName, final String value )
    {
        final Header header = getHttpResponse().getFirstHeader( headerName );
        return header != null && header.getValue() != null && header.getValue().startsWith( value );
    }

    /**
     * Checks if header with given name is present and equals with given value.
     * 
     * @param headerName
     * @param value
     * @return {@code true} if header with given name is present and equals with given value.
     */
    protected boolean hasHeaderAndEqualsWith( final String headerName, final String value )
    {
        final Header header = getHttpResponse().getFirstHeader( headerName );
        return header != null && header.getValue() != null && header.getValue().equals( value );
    }

    // ==

    /**
     * Returns a page for given URL.
     * 
     * @param context
     * @param url
     * @return the Page for given URL.
     * @throws IOException
     */
    public Page getPageFor( final ScrapeContext context, final String url )
        throws IOException
    {
        if ( getUrl().equals( url ) )
        {
            return this;
        }
        // TODO: detect redirects
        final HttpGet get = new HttpGet( url );
        HttpResponse response = context.getHttpClient().execute( get );
        try
        {
            if ( response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 499 )
            {
                if ( response.getEntity() != null )
                {
                    return new Page( url, response, Jsoup.parse( response.getEntity().getContent(), null, url ) );
                }
                else
                {
                    // no body
                    return new Page( url, response, Jsoup.parseBodyFragment( "<html></html>", url ) );
                }
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
}
