package org.sonatype.appcontext.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.ContextStringDumper;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A publisher that publishes Application Context to SLF4J Log on first enabled level (will try DEBUG, INFO, WARN in
 * this order). If none of those are enabled, will do nothing.
 * 
 * @author cstamas
 */
public class Slf4jLoggerEntryPublisher
    implements EntryPublisher
{
    private final Logger logger;

    public Slf4jLoggerEntryPublisher()
    {
        this( LoggerFactory.getLogger( AppContext.class ) );
    }

    public Slf4jLoggerEntryPublisher( final Logger logger )
    {
        this.logger = Preconditions.checkNotNull( logger );
    }

    public void publishEntries( AppContext context )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n===================================\n" );
        sb.append( ContextStringDumper.dumpToString( context ) );
        sb.append( "===================================\n" );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( sb.toString() );
        }
        else if ( logger.isInfoEnabled() )
        {
            logger.info( sb.toString() );
        }
        else if ( logger.isWarnEnabled() )
        {
            logger.warn( sb.toString() );
        }
    }
}
