/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.metadata;

import java.io.IOException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.osgi.service.obr.Resource;
import org.sonatype.nexus.proxy.InvalidItemContentException;

import com.sonatype.nexus.obr.util.ObrUtils;

/**
 * Default {@link ObrResourceReader} that can handle OBR referrals.
 */
public class DefaultObrResourceReader
    implements ObrResourceReader
{
    private boolean relative;

    private Map<String, ObrParser> visited;

    private List<URL> breadcrumbs;

    private ObrParser parser;

    /**
     * Creates a new {@link ObrResourceReader} for the given OBR site.
     * 
     * @param site the OBR site
     * @param relative use relative URIs?
     * @throws IOException
     */
    public DefaultObrResourceReader( ObrSite site, boolean relative )
        throws IOException
    {
        this.relative = relative;

        visited = new HashMap<String, ObrParser>();
        breadcrumbs = new ArrayList<URL>();

        visit( site, Integer.MAX_VALUE );
    }

    /**
     * Visit an OBR site and continue parsing.
     * 
     * @param site the OBR site
     * @param depth the maximum depth
     * @throws IOException
     */
    private void visit( ObrSite site, int depth )
        throws IOException
    {
        URL nextMetadataUrl = site.getMetadataUrl();

        if ( isNewSite( nextMetadataUrl ) )
        {
            try
            {
                pushReferral( nextMetadataUrl, new DefaultObrParser( site, depth, relative ) );
            }
            catch ( XmlPullParserException e )
            {
                throw new InvalidItemContentException( "Error parsing OBR header", e );
            }
        }
    }

    /**
     * True if we have not yet visited this URL, otherwise false.
     * 
     * @param url the OBR URL
     * @return true if we have not yet visited this URL, otherwise false
     */
    private boolean isNewSite( URL url )
    {
        return !visited.containsKey( url.toExternalForm() );
    }

    /**
     * Retrieves the parser that's currently parsing the given OBR.
     * 
     * @param url the OBR URL
     * @return the parser for the given OBR
     */
    private ObrParser getParser( URL url )
    {
        return visited.get( url.toExternalForm() );
    }

    /**
     * Records that we are now parsing the referred OBR.
     * 
     * @param url the OBR URL
     * @param nextParser the parser for the given OBR
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void pushReferral( URL url, ObrParser nextParser )
        throws XmlPullParserException, IOException
    {
        visited.put( url.toExternalForm(), nextParser );

        // move onto the resource section of the OBR
        nextParser.require( XmlPullParser.START_DOCUMENT, null, null );
        nextParser.nextTag();
        nextParser.require( XmlPullParser.START_TAG, null, "repository" );

        // safe to proceed
        if ( parser != null )
        {
            breadcrumbs.add( parser.getMetadataUrl() );
        }

        parser = nextParser;
    }

    /**
     * Stop parsing the referred OBR and return to the referring OBR site.
     * 
     * @return true if there are more sites to parse, otherwise false
     */
    private boolean popReferral()
    {
        if ( breadcrumbs.isEmpty() )
        {
            return false;
        }

        parser = getParser( breadcrumbs.remove( breadcrumbs.size() - 1 ) );

        return true;
    }

    public Resource readResource()
        throws IOException
    {
        try
        {
            while ( true )
            {
                parser.nextTag();

                if ( "referral".equals( parser.getName() ) )
                {
                    parseReferral(); // points to another (external) OBR site
                }
                else if ( "repository".equals( parser.getName() ) )
                {
                    // should be end tag as we parsed the start tag in "visit"
                    parser.require( XmlPullParser.END_TAG, null, "repository" );
                    if ( false == popReferral() )
                    {
                        return null;
                    }
                }
                else
                {
                    break; // assume the only other top-level tag is resource
                }
            }

            return parser.parseResource();
        }
        catch ( XmlPullParserException e )
        {
            throw new InvalidItemContentException( "Error parsing OBR resource", e );
        }
    }

    /**
     * Parses a referred OBR site, obeying the maximum allowed depth of referrals.
     * 
     * @throws IOException
     */
    private void parseReferral()
        throws IOException
    {
        try
        {
            parser.require( XmlPullParser.START_TAG, null, "referral" );

            String url = parser.getAttributeValue( null, "url" );
            String depth = parser.getAttributeValue( null, "depth" );

            parser.nextTag();
            parser.require( XmlPullParser.END_TAG, null, "referral" );

            if ( breadcrumbs.size() < parser.getMaxDepth() )
            {
                visit( new ReferencedObrSite( new URL( parser.getMetadataUrl(), url ) ), calculateMaxDepth( depth ) );
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new InvalidItemContentException( "Error parsing OBR referral", e );
        }
    }

    /**
     * Chooses the smallest limit: either the current global maximum depth or the new local maximum.
     * 
     * @param depth the requested depth
     * @return the new maximum depth
     */
    private int calculateMaxDepth( String depth )
    {
        try
        {
            return Math.min( parser.getMaxDepth(), breadcrumbs.size() + Integer.parseInt( depth ) );
        }
        catch ( Exception e )
        {
            return Integer.MAX_VALUE;
        }
    }

    public int read( CharBuffer cb )
        throws IOException
    {
        // just here to complete the Reader API, it's not actually used

        try
        {
            parser.nextToken();
        }
        catch ( XmlPullParserException e )
        {
            throw new InvalidItemContentException( "Error parsing XML token", e );
        }

        int n = cb.length();
        cb.append( parser.getText() );
        return cb.length() - n;
    }

    public void close()
    {
        for ( ObrParser p : visited.values() )
        {
            ObrUtils.close( p );
        }
    }
}
