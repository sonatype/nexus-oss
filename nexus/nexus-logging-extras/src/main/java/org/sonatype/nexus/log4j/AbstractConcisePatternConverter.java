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
     * @See <a href='http://code.google.com/p/able/source/browse/trunk/able-lib/src/main/java/able/util/StandardFormatter.java'>StandardFormatter</a
     *      >
     */
    public static String simplify( String className, int maxLength )
    {
        if ( className.length() <= maxLength )
        {
            return className;
        }

        StringBuffer result = new StringBuffer();

        int index = -1;
        
        while ( true )
        {
            if ( className.indexOf( ".", index + 1 ) == -1 )
            {
                String remainingStr = className.substring( index + 1 );
                
                int availableLength = maxLength - result.length();

                if ( remainingStr.length() > availableLength )
                {
                    remainingStr = remainingStr.substring( 0, availableLength - 1 ) + '~';
                }

                result.append( remainingStr );

                break;
            }
            else
            {
                result.append( className.charAt( index + 1 ) );
                
                if ( result.length() + 1 == maxLength )
                {
                    result.append( '~' );
                    
                    break;
                }

                result.append( '.' );
                
                index = className.indexOf( ".", index + 1 );
            }
        }

        return result.toString();
    }
}
