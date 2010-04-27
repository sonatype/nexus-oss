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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

/**
 * A java.util.Properties like tool, but besides map entry, it supports comments and black lines.
 * 
 * @author juven
 */
public class EnhancedProperties
    extends LinkedHashMap<String, String>
{
    private static final long serialVersionUID = -3245270814468070815L;

    public static final String COMMENT_KEY_PREFIX = "#COMMENT";

    public static final String BLANK_LINE_KEY_PREFIX = "#BLANK_LINE";

    public void putIfNew( String key, String value )
    {
        if ( get( key ) == null )
        {
            put( key, value );
        }
    }

    public void load( InputStream inStream, String... filters )
        throws IOException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( inStream ) );

        String line;

        int commentCount = 0;
        int blankLineCount = 0;

        while ( ( line = reader.readLine() ) != null )
        {
            boolean skip = false;

            if ( filters != null && filters.length > 0 )
            {
                for ( String filter : filters )
                {
                    if ( filter.equals( line ) )
                    {
                        skip = true;
                    }
                }
            }

            if ( skip )
            {
                continue;
            }

            if ( line.length() == 0 )
            {
                put( BLANK_LINE_KEY_PREFIX + blankLineCount, "" );

                blankLineCount++;

                continue;
            }

            if ( line.startsWith( "#" ) )
            {
                put( COMMENT_KEY_PREFIX + commentCount, line );

                commentCount++;

                continue;
            }

            int delimiterIndex = line.indexOf( "=" );

            if ( delimiterIndex != -1 )
            {
                String key = line.substring( 0, delimiterIndex );

                String value = line.substring( delimiterIndex + 1 );

                put( key, value );
            }
        }
    }

    public void store( OutputStream outStream )
        throws IOException
    {
        store( outStream, null );
    }

    public void store( OutputStream outStream, String comment )
        throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( outStream, "8859_1" ) );

        if ( StringUtils.isNotBlank( comment ) )
        {
            writer.write( "# " );

            writer.write( comment );

            writer.newLine();
        }

        for ( Map.Entry<String, String> entry : entrySet() )
        {
            if ( entry.getKey().startsWith( COMMENT_KEY_PREFIX ) )
            {
                writer.write( entry.getValue() );
            }
            else if ( entry.getKey().startsWith( BLANK_LINE_KEY_PREFIX ) )
            {
                writer.write( entry.getValue() );
            }
            else
            {
                writer.write( entry.getKey() + "=" + entry.getValue() );
            }
            writer.newLine();
        }

        writer.flush();
    }

}
