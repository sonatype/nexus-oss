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
