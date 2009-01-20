/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
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

    /**
     * Simple concat method. It only watches that there is only one PATH_SEPARATOR betwen parts passed in. It DOES NOT
     * checks that parts are fine or not.
     * 
     * @param p
     * @return
     */
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

    /**
     * Simple path cleanup.
     * 
     * @param path
     * @return
     */
    public static String cleanUpTrailingSlash( String path )
    {
        if ( StringUtils.isEmpty( path ) )
        {
            path = PATH_SEPARATOR;
        }

        if ( path.length() > 1 && path.endsWith( PATH_SEPARATOR ) )
        {
            path = path.substring( 0, path.length() - PATH_SEPARATOR.length() );
        }

        return path;
    }

    /**
     * Calculates the parent path for a path.
     * 
     * @param path
     * @return
     */
    public static String getParentPath( String path )
    {
        if ( PATH_SEPARATOR.equals( path ) )
        {
            return path;
        }

        int lastSepratorPos = path.lastIndexOf( PATH_SEPARATOR );

        if ( lastSepratorPos == 0 )
        {
            return PATH_SEPARATOR;
        }
        else
        {
            return path.substring( 0, lastSepratorPos );
        }
    }

}
