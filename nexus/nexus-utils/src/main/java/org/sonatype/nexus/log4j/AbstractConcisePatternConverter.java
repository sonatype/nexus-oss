/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.log4j;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author juven
 */
public abstract class AbstractConcisePatternConverter
    extends PatternConverter
{
    private int precision;

    public AbstractConcisePatternConverter( FormattingInfo formattingInfo, int precision )
    {
        super( formattingInfo );

        this.precision = precision;
    }

    abstract protected String getConciseName( LoggingEvent event );

    @Override
    /*
     * Simply copy from log4j's NamedPatternConverter
     */
    protected String convert( LoggingEvent event )
    {
        String n = getConciseName( event );

        if ( precision <= 0 )
        {
            return n;
        }
        else
        {
            int len = n.length();

            // We substract 1 from 'len' when assigning to 'end' to avoid out of
            // bounds exception in return r.substring(end+1, len). This can happen if
            // precision is 1 and the category name ends with a dot.
            int end = len - 1;
            for ( int i = precision; i > 0; i-- )
            {
                end = n.lastIndexOf( '.', end - 1 );
                if ( end == -1 )
                    return n;
            }
            return n.substring( end + 1, len );
        }

    }

    /**
     * @See Able Source <a href='http://code.google.com/p/able/source/browse/trunk/able-lib/src/main/java/able/util/StandardFormatter.java'>StandardFormatte
     *      r < / a >
     */
    public static String simplify( String className, int maxLength )
    {
        StringBuffer result = new StringBuffer();

        int classNameLength = className.length();
        int before = result.length();

        if ( classNameLength > maxLength )
        {
            int index = -1;
            while ( true )
            {
                result.append( className.charAt( index + 1 ) );

                int oldIndex = index;
                index = className.indexOf( ".", index + 1 );

                if ( index == -1 )
                {
                    String str = className.substring( oldIndex + 2 );
                    int rem = maxLength - ( result.length() - before );

                    if ( str.length() > rem )
                    {
                        str = str.substring( 0, rem - 1 ) + '~';
                    }

                    result.append( str );

                    break;
                }
                else
                {
                    result.append( '.' );
                }
            }
        }
        else
        {
            result.append( className );
        }

        return result.toString();
    }
}
