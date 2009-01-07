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
package org.sonatype.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.restlet.Context;

/**
 * A log chute for TemplateRepresentations that are using Velocity under the hud. This logChute simply redirects
 * Velocity logging to the same logging engine that is Restlet using and is preventing the commons-logging fallback of
 * Velocity.
 * 
 * @author cstamas
 */
public class RestletLogChute
    implements LogChute
{
    private final Logger logger;

    public RestletLogChute( Context context )
    {
        super();

        this.logger = context.getLogger();
    }

    public void init( RuntimeServices srv )
        throws Exception
    {
        // nothing
    }

    public boolean isLevelEnabled( int level )
    {
        return logger.isLoggable( convertLevel( level ) );
    }

    public void log( int level, String msg )
    {
        logger.log( convertLevel( level ), msg );
    }

    public void log( int level, String msg, Throwable t )
    {
        logger.log( convertLevel( level ), msg, t );
    }

    protected Level convertLevel( int lvl )
    {
        switch ( lvl )
        {
            case TRACE_ID:
                return Level.FINER;
            case LogChute.DEBUG_ID:
                return Level.FINE;
            case INFO_ID:
                return Level.INFO;
            case WARN_ID:
                return Level.WARNING;
            case ERROR_ID:
                return Level.SEVERE;
            default:
                return Level.INFO;
        }
    }
}
