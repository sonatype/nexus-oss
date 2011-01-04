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
package org.sonatype.nexus.util;

import java.util.Collection;

import org.codehaus.plexus.util.StringUtils;

/**
 * This is NOT FS Path utils! This util uses 'URL'-like paths, hence the separator is always '/' and is not system
 * dependant!
 * 
 * @author cstamas
 * @author juven
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

    /**
     * Calculates the least common parent path
     * 
     * @return null if paths is empty, else the least common parent path
     */
    public static String getLCPPath( final Collection<String> paths )
    {
        String lcp = null;

        for ( String path : paths )
        {
            if ( lcp == null )
            {
                lcp = path;
            }
            else
            {
                lcp = getLCPPath( lcp, path );
            }
        }

        return lcp;
    }

    /**
     * Calculates the least common parent path
     * 
     * @return null if any path is empty, else the least common parent path
     */
    public static String getLCPPath( final String pathA, final String pathB )
    {
        if ( StringUtils.isEmpty( pathA ) || StringUtils.isEmpty( pathB ) )
        {
            return null;
        }

        if ( pathA.equals( pathB ) )
        {
            return pathA;
        }

        if ( pathA.startsWith( pathB ) )
        {
            return pathB;
        }

        if ( pathB.startsWith( pathA ) )
        {
            return pathA;
        }

        StringBuffer lcp = new StringBuffer();

        StringBuffer token = new StringBuffer();

        int index = 0;

        while ( pathA.charAt( index ) == pathB.charAt( index ) && index < pathA.length() && index < pathB.length() )
        {
            token.append( pathA.charAt( index ) );

            if ( pathA.charAt( index ) == PATH_SEPARATOR.charAt( 0 ) )
            {
                lcp.append( token );

                token.delete( 0, token.length() );
            }

            index++;
        }

        return lcp.toString();
    }

}
