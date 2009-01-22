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

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A log chute for TemplateRepresentations that are using Velocity under the hud. This logChute simply redirects
 * Velocity logging to the SLF4J logging engine and is preventing the commons-logging fallback of Velocity.
 * 
 * @author cstamas
 */
public class Slf4jLogChute
    implements LogChute
{
    private final Logger logger;

    public Slf4jLogChute()
    {
        this.logger = LoggerFactory.getLogger( Velocity.class );
    }

    public void init( RuntimeServices srv )
        throws Exception
    {
        // nothing
    }

    public boolean isLevelEnabled( int level )
    {
        switch ( level )
        {
            case TRACE_ID:
                return logger.isTraceEnabled();
            case DEBUG_ID:
                return logger.isDebugEnabled();
            case INFO_ID:
                return logger.isInfoEnabled();
            case WARN_ID:
                return logger.isWarnEnabled();
            case ERROR_ID:
                return logger.isErrorEnabled();
            default:
                // huh?
                return level > INFO_ID;
        }
    }

    public void log( int level, String msg )
    {
        switch ( level )
        {
            case TRACE_ID:
                logger.trace( msg );
            case DEBUG_ID:
                logger.debug( msg );
            case INFO_ID:
                logger.info( msg );
            case WARN_ID:
                logger.warn( msg );
            case ERROR_ID:
                logger.error( msg );
            default:
                // huh?
                logger.info( msg );
        }
    }

    public void log( int level, String msg, Throwable t )
    {
        switch ( level )
        {
            case TRACE_ID:
                logger.trace( msg, t );
            case DEBUG_ID:
                logger.debug( msg, t );
            case INFO_ID:
                logger.info( msg, t );
            case WARN_ID:
                logger.warn( msg, t );
            case ERROR_ID:
                logger.error( msg, t );
            default:
                // huh?
                logger.info( msg, t );
        }
    }
}
