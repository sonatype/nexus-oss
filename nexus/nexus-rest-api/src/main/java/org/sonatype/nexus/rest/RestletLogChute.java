package org.sonatype.nexus.rest;

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
