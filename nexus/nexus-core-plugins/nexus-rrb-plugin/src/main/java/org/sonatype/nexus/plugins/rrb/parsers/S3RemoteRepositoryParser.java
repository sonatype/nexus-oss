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

public class S3RemoteRepositoryParser
    implements RemoteRepositoryParser
{

    private final Logger logger = LoggerFactory.getLogger( S3RemoteRepositoryParser.class );

    private static final String[] EXCLUDES = { "VolumeIcon", "Parent Directory", "?", "..", "index", "robots" };

    private String localUrl;

    private String remotePath;
    
    ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();

    public S3RemoteRepositoryParser( String remotePath, String localUrl, String id, String basePrefix )
    {
        this.remotePath = remotePath;
        this.localUrl = localUrl;
        
        //strip the remotePath from the localUrl
        if( localUrl.endsWith( remotePath ) )
        {
            this.localUrl = localUrl.substring( 0, localUrl.lastIndexOf( remotePath ) );
        }
        
        if( !this.localUrl.endsWith( "/" ))
        {
            this.localUrl += "/";
        }
        
        if( !this.remotePath.endsWith( "/" ))
        {
            this.remotePath += "/";
        }
    }

    void extractContent( StringBuilder indata, String prefix )
    {
        int start = 0;
        int end = 0;
        do
        {
            RepositoryDirectory rp = new RepositoryDirectory();
            StringBuilder temp = new StringBuilder();
            start = indata.indexOf( "<Key", start );
            if ( start < 0 )
            {
                break;
            }
            end = indata.indexOf( "/Key>", start ) + 5;
            temp.append( indata.subSequence( start, end ) );
            if ( !exclude( temp, prefix ) )
            {
                String relativePath = removePrefix( getKeyName( temp ), prefix ).replace( "//", "/" );
                if( relativePath.startsWith( "/" ));
                {
                    relativePath = relativePath.replaceFirst( "/", "" );
                }
                
                rp.setLeaf( true );
                rp.setText( getText( relativePath ) );
                rp.setResourceURI( localUrl + relativePath );
                rp.setRelativePath( "/" + relativePath );
                
                if ( !remotePath.endsWith( rp.getRelativePath().substring( 1 ) ) )
                {
                    logger.debug( "addning {} to result", rp.toString() );
                    result.add( rp );
                }
            }
            start = end + 1;
        }
        while ( start > 0 );

    }

    void extractCommonPrefix( StringBuilder indata, String prefix )
    {   
        int start = 0;
        int end = 0;
        do
        {
            RepositoryDirectory rp = new RepositoryDirectory();
            StringBuilder temp = new StringBuilder();
            start = indata.indexOf( "<CommonP", start );
            if ( start < 0 )
            {
                break;
            }
            end = indata.indexOf( "/CommonP", start ) + 8;
            temp.append( indata.subSequence( start, end ) );
            if ( !exclude( temp, prefix ) )
            {
                String relativePath = getRelitivePath( temp, prefix );
                
                rp.setLeaf( false );
                rp.setText( getText( relativePath ) );
                
                rp.setResourceURI( localUrl + relativePath );
                rp.setRelativePath( "/" + relativePath );

                result.add( rp );
            }
            start = end + 1;
        }
        while ( start > 0 );
    }

    private String removePrefix( String localUrl, String prefix )
    {
        if( prefix == null )
        {
            return localUrl;
        }
        
        return localUrl.replaceFirst( prefix, "" );
    }

    private String getText( String keyName )
    {
        String returnValue = "";
        if ( keyName.indexOf( '/' ) != -1 )
        {
            String[] keys = keyName.split( "/" );
            returnValue = keys[keys.length - 1];
        }
        else
        {
            returnValue = keyName;
        }
        return returnValue;
    }

    /**
     * Excludes links that are not relevant for the listing.
     */
    private boolean exclude( StringBuilder value, String prefix )
    {
        for ( String s : EXCLUDES )
        {
            if ( value.indexOf( s ) > 0 )
            {
                logger.debug( "{} is in EXCLUDES array", value );
                return true;
            }
        }
        
        if( xmlContainsString( value, prefix ) || xmlContainsString( value, this.remotePath ) || xmlContainsString( value,  prefix + this.remotePath ) )
        {
            return true;
        }

        return false;
    }
    
    private static boolean xmlContainsString( StringBuilder xmlSnippet, String checkFor )
    {
        if( StringUtils.isNotEmpty( checkFor ) && ( xmlSnippet.indexOf( ">"+ checkFor +"<" ) > 0 || xmlSnippet.indexOf( ">"+ checkFor +"/<" ) > 0 ) )
        {
            return true;
        }
        return false;
    }

    /**
     * Extracts the key name.
     */
    private String getKeyName( StringBuilder temp )
    {
        int start = temp.indexOf( ">" ) + 1;
        int end = temp.indexOf( "</" );
        return temp.substring( start, end );
    }

    /**
     * Extracts the prefix.
     */
    private String getPrefix( StringBuilder temp )
    {
       if( temp.indexOf( "<Prefix>" ) < 0)
        {
            return "";
        }
        
        int start = temp.indexOf( "<Prefix>" ) + 8;
        int end = temp.indexOf( "</Prefix" );
        return temp.substring( start, end );
    }
    
    private String getRelitivePath( StringBuilder temp, String prefix )
    {
        int start = temp.indexOf( "<Prefix>" ) + 8;
        int end = temp.indexOf( "</Prefix" );
        String relativePath = (this.remotePath + this.removePrefix( temp.substring( start, end ), prefix )).replaceAll( "//", "/" );
        
        // strip the leading /
        if( relativePath.startsWith( "/" ) )
        {
            relativePath = relativePath.substring( 1 );
        }
        return relativePath;
    }

    public ArrayList<RepositoryDirectory> extractLinks( StringBuilder indata )
    {
        String prefix = getPrefix( indata );
        
        extractContent( indata, prefix );
        extractCommonPrefix( indata, prefix );

        return result;
    }
}
