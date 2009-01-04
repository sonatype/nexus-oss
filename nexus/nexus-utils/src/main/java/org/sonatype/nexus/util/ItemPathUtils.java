package org.sonatype.nexus.util;

import org.codehaus.plexus.util.StringUtils;

/**
 * This is NOT FS Path utils! This util uses 'URL'-like paths, hence the separator is always '/' and is not system
 * dependant!
 * 
 * @author cstamas
 */
public class ItemPathUtils
{
    public static String concatPaths( String... p )
    {
        StringBuffer result = new StringBuffer();

        for ( String path : p )
        {
            if ( !StringUtils.isEmpty( path ) )
            {
                if ( !path.startsWith( "/" ) )
                {
                    result.append( "/" );
                }

                result.append( path.endsWith( "/" ) ? path.substring( 0, path.length() - 1 ) : path );
            }
        }

        return result.toString();
    }
}
