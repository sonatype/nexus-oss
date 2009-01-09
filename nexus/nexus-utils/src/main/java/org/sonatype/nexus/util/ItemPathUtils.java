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
    public static final String PATH_SEPARATOR = "/";

    public static final int PATH_SEPARATOR_LENGTH = PATH_SEPARATOR.length();

    public static String concatPaths( String... p )
    {
        StringBuffer result = new StringBuffer();

        for ( String path : p )
        {
            if ( !StringUtils.isEmpty( path ) )
            {
                if ( !path.startsWith( PATH_SEPARATOR ) )
                {
                    result.append( PATH_SEPARATOR );
                }

                result.append( path.endsWith( PATH_SEPARATOR ) ? path.substring( 0, path.length()
                    - PATH_SEPARATOR_LENGTH ) : path );
            }
        }

        return result.toString();
    }
}
