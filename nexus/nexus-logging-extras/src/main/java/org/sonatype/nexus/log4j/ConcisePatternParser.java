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
package org.sonatype.nexus.log4j;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;

/**
 * This Parser is used to integrate a compact logging into log4j </br> See: <a
 * href='http://lightbody.net/blog/2008/12/tip-compact-logging-in-java.html'>Tip: Compact Logging in Java</a>
 * 
 * @author juven
 */
public class ConcisePatternParser
    extends PatternParser
{

    public ConcisePatternParser( String pattern )
    {
        super( pattern );
    }

    protected void finalizeConverter( char c )
    {
        // use our customized concise patternConverter, instead of log4j's default
        if ( c == 'C' )
        {
            PatternConverter patternConverter = new ClassNameConcisePatternConverter(
                formattingInfo,
                extractPrecisionOption() );

            currentLiteral.setLength( 0 );

            addConverter( patternConverter );
        }
        else if ( c == 'c' )
        {
            PatternConverter patternConverter = new CategoryConcisePatternConverter(
                formattingInfo,
                extractPrecisionOption() );

            currentLiteral.setLength( 0 );

            addConverter( patternConverter );
        }
        else
        {
            super.finalizeConverter( c );
        }
    }

}
