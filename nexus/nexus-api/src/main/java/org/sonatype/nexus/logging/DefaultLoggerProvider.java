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
package org.sonatype.nexus.logging;

import java.util.HashMap;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very-very simple Provider implementation for providing SLF4J loggers.
 * 
 * @author cstamas
 */
@Component( role = LoggerProvider.class )
public class DefaultLoggerProvider
    implements LoggerProvider
{
    private final HashMap<String, Logger> loggers = new HashMap<String, Logger>();

    public synchronized Logger get()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String loggerKey = null;

        if ( stackTrace.length >= 4 )
        {
            loggerKey = stackTrace[3].getClassName();
        }
        else
        {
            loggerKey = "ROOT";
        }

        return getLogger( loggerKey );
    }

    public Logger getLogger( String loggerKey )
    {
        if ( !loggers.containsKey( loggerKey ) )
        {
            loggers.put( loggerKey, LoggerFactory.getLogger( loggerKey ) );
        }

        return loggers.get( loggerKey );
    }
}
