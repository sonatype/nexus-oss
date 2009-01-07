/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.util;

import java.util.List;

/**
 * Convert artifactory style pattern to nexus style pattern (which is regular expression)
 * 
 * @author Juven Xu
 */
public class PatternConvertor
{
    public static String convert125Pattern( String path )
    {
        if ( path.equals( "ANY" ) )
        {
            return ".*";
        }
        else
        {
            if ( !path.equals( "/" ) )
            {
                path = path + "/";
            }
            return path + ".*";
        }
    }

    /**
     * Ant style (*, **, ?)
     * 
     * @param includes
     * @param excludes
     * @return
     */
    public static String convert130Pattern( List<String> includes, List<String> excludes )
    {
        // default is all
        if ( includes.isEmpty() )
        {
            includes.add( "**" );
        }

        StringBuffer regx = new StringBuffer();

        for ( int i = 0; i < includes.size(); i++ )
        {
            if ( i > 0 )
            {
                regx.append( "|" );
            }

            regx.append( "(" + convertAntStylePattern( includes.get( i ) ) + ")" );
        }

        // TODO: how to append excludes?

        return regx.toString();
    }

    /**
     * ? -> .{1} </br> ** -> .* </br> * -> [^/]*
     * 
     * @param pattern
     * @return
     */
    public static String convertAntStylePattern( String pattern )
    {
        StringBuffer regx = new StringBuffer();

        for ( int i = 0; i < pattern.length(); )
        {

            if ( pattern.charAt( i ) == '?' )
            {
                regx.append( ".{1}" );

                i++;
            }
            else if ( pattern.charAt( i ) == '*' && ( i + 1 ) != pattern.length() && pattern.charAt( i + 1 ) == '*' )
            {
                regx.append( ".*" );

                i += 2;
            }
            else if ( pattern.charAt( i ) == '*' )
            {
                regx.append( "[^/]*" );

                i++;
            }
            else if ( pattern.charAt( i ) == '.' )
            {
                regx.append( "\\." );
                
                i++;
            }
            else
            {
                regx.append( pattern.charAt( i ) );

                i++;
            }
        }

        return regx.toString();
    }
}
