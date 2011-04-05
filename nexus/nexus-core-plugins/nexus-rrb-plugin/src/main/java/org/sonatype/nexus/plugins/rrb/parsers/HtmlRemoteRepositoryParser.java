/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class HtmlRemoteRepositoryParser
    implements RemoteRepositoryParser
{

    private static final String[] EXCLUDES = { ">Skip to content<", ">Log in<", ">Products<", "Parent Directory", "?",
        ">../", ">..<", ">._.<", "-logo.png", ">Community<", ">Support<", ">Resources<", ">About us<", ">Downloads<",
        ">Documentation<", ">Resources<", ">About This Site<", ">Contact Us<", ">Legal Terms and Privacy Policy<",
        ">Log out<", ">IONA Technologies<", ">Site Index<", ">Skip to content<", ">Log In<" };

    private final Logger logger = LoggerFactory.getLogger( HtmlRemoteRepositoryParser.class );

    protected String localUrl;

    protected String remotePath;

    protected String linkStart = "<a ";

    protected String linkEnd = "/a>";

    protected String href = "href=\"";

    protected String id;

    protected String baseUrl;

    public HtmlRemoteRepositoryParser( String remotePath, String localUrl, String id, String baseUrl )
    {
        this.remotePath = remotePath;
        this.localUrl = localUrl;
        this.id = id;
        this.baseUrl = baseUrl;
    }

    /**
     * Extracts the links and sets the data in the RepositoryDirectory object.
     * 
     * @param indata
     * @return a list of RepositoryDirectory objects
     */
    public ArrayList<RepositoryDirectory> extractLinks( StringBuilder indata )
    {
        ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();

        if ( indata.indexOf( linkStart.toUpperCase() ) != -1 )
        {
            linkStart = linkStart.toUpperCase();
            linkEnd = linkEnd.toUpperCase();
            href = href.toUpperCase();
        }
        int start = 0;
        int end = 0;

        if ( !remotePath.endsWith( "/" ) )
        {
            remotePath += "/";
        }
        if ( remotePath.equals( "/" ) )
        {
            remotePath = "";
        }
        if ( !localUrl.endsWith( "/" ) )
        {
            localUrl += "/";
        }

        do
        {
            RepositoryDirectory rp = new RepositoryDirectory();
            StringBuilder temp = new StringBuilder();
            start = indata.indexOf( linkStart, start );
            if ( start < 0 )
            {
                break;
            }

            end = indata.indexOf( linkEnd, start ) + linkEnd.length();
            temp.append( indata.subSequence( start, end ) );
            if ( !exclude( temp ) )
            {
                if ( !getLinkName( temp ).trim().endsWith( "/" ) )
                {
                    rp.setLeaf( true );
                }
                rp.setText( getLinkName( temp ).replace( "/", "" ).trim() );
                String uri = getLinkUrl( temp ).replace( baseUrl, localUrl );
                uri = uri.startsWith( localUrl ) ? uri : localUrl + remotePath + uri;
                rp.setResourceURI( uri );
                rp.setRelativePath( uri.replace( localUrl, "" ) );
                if ( !rp.getRelativePath().startsWith( "/" ) )
                {
                    rp.setRelativePath( "/" + rp.getRelativePath() );
                }

                if ( StringUtils.isNotEmpty( rp.getText() ) )
                {
                    result.add( rp );
                }
                logger.debug( "adding {} to result", rp.toString() );
            }
            start = end + 1;
        }
        while ( start > 0 );

        return result;
    }

    /**
     * Extracts the link name.
     */
    protected String getLinkName( StringBuilder temp )
    {
        int start = temp.indexOf( ">" ) + 1;
        int end = temp.indexOf( "</" );
        return cleanup( temp.substring( start, end ) );
    }

    protected String cleanup( String value )
    {
        int start = value.indexOf( '<' );
        int end = value.indexOf( '>' );
        if ( start != -1 && start < end )
        {
            CharSequence seq = value.substring( start, end + 1 );
            value = value.replace( seq, "" );
            cleanup( value );
        }
        return value.trim();
    }

    /**
     * Extracts the link url.
     */
    protected String getLinkUrl( StringBuilder temp )
    {
        int start = temp.indexOf( href ) + href.length();
        int end = temp.indexOf( "\"", start + 1 );
        return temp.substring( start, end );
    }

    /**
     * Excludes links that are not relevant for the listing.
     */
    boolean exclude( StringBuilder value )
    {
        for ( String s : EXCLUDES )
        {
            if ( value.indexOf( s ) > 0 )
            {
                logger.debug( "{} is in EXCLUDES array", value );
                return true;
            }
        }
        return false;
    }

}
