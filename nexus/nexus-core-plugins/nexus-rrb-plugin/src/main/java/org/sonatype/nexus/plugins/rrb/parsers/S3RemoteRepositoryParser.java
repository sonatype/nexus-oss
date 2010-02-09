package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class S3RemoteRepositoryParser
    implements RemoteRepositoryParser
{

    private final Logger logger = LoggerFactory.getLogger( S3RemoteRepositoryParser.class );

    private static final String[] EXCLUDES = { "VolumeIcon", "Parent Directory", "?", "..", "index", "robots" };

    private String localUrl;

    private String basePrefix;

    private String remoteUrl;

    ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();

    private String id;

    public S3RemoteRepositoryParser( String remoteUrl, String localUrl, String id, String basePrefix )
    {
        this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
        this.id = id;
        this.basePrefix = basePrefix;
    }

    void extractContent( StringBuilder indata )
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
            if ( !exclude( temp ) )
            {
                rp.setLeaf( true );
                rp.setText( getText( getKeyName( temp ) ) );
                rp.setResourceURI( localUrl + "/" + getKeyName( temp ) );
                rp.setRelativePath( ( getKeyName( temp ).replace( basePrefix, "" ) ).replace( "//", "/" ) );
                if ( !rp.getRelativePath().startsWith( "/" ) )
                {
                    rp.setRelativePath( "/" + rp.getRelativePath() );
                }
                if ( !remoteUrl.endsWith( rp.getRelativePath().substring( 1 ) ) )
                {
                    logger.debug( "addning {} to result", rp.toString() );
                    result.add( rp );
                }
            }
            start = end + 1;
        }
        while ( start > 0 );

    }

    void extractCommonPrefix( StringBuilder indata )
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
            if ( !exclude( temp ) )
            {
                rp.setLeaf( false );
                rp.setText( getText( getPrefix( temp ) ) );
                if ( remoteUrl.indexOf( '?' ) != -1 )
                {
                    rp.setResourceURI( removePrefix( localUrl, getPrefix( temp ) ) + "?prefix=" + getPrefix( temp ) );
                }
                else
                {
                    rp.setResourceURI( removePrefix( localUrl, getPrefix( temp ) ) + "?prefix=" + getPrefix( temp ) );
                }

                rp.setRelativePath( "/?prefix=" + getPrefix( temp ) );

                result.add( rp );
            }
            start = end + 1;
        }
        while ( start > 0 );
    }

    private String removePrefix( String localUrl, String prefix )
    {
        int end = localUrl.indexOf( prefix.substring( 0, prefix.indexOf( '/' ) ) );
        if ( end > 0 )
        {
            localUrl = localUrl.substring( 0, end );
        }
        return localUrl;
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
    private boolean exclude( StringBuilder value )
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
        int start = temp.indexOf( "<Prefix>" ) + 8;
        int end = temp.indexOf( "</Prefix" );
        return temp.substring( start, end );
    }

    public ArrayList<RepositoryDirectory> extractLinks( StringBuilder indata )
    {
        extractContent( indata );
        extractCommonPrefix( indata );

        return result;
    }
}
