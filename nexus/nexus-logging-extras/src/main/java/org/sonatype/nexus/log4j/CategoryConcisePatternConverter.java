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

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author juven
 */
public class CategoryConcisePatternConverter
    extends AbstractConcisePatternConverter
{
    private static final int ROLE_LENGTH = 20;

    private static final int HINT_LENGTH = 8;

    public CategoryConcisePatternConverter( FormattingInfo formattingInfo, int precision )
    {
        super( formattingInfo, precision );
    }

    @Override
    protected String getConciseName( LoggingEvent event )
    {

        StringBuffer result = new StringBuffer();

        appendConciseCategory( result, event.getLoggerName() );

        appendLeftSpaces( result );

        return result.toString();
    }

    private void appendConciseCategory( StringBuffer sb, String loggerName )
    {
        int colonIndex = loggerName.indexOf( ':' );

        if ( colonIndex == -1 )
        {
            sb.append( simplify( loggerName, ROLE_LENGTH ) );

            return;
        }

        String roleName = loggerName.substring( 0, colonIndex );

        String hintName = loggerName.substring( colonIndex + 1 );

        sb.append( simplify( roleName, ROLE_LENGTH ) ).append( ":" ).append( simplify( hintName, HINT_LENGTH ) );

    }

    private void appendLeftSpaces( StringBuffer sb )
    {
        while ( sb.length() < ROLE_LENGTH + 1 + HINT_LENGTH )
        {
            sb.append( " " );
        }
    }
}
