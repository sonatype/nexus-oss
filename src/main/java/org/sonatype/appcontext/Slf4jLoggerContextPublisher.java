package org.sonatype.appcontext;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A publisher that publishes Application Context to SLF4J Log.
 * 
 * @author cstamas
 */
public class Slf4jLoggerContextPublisher
    implements ContextPublisher
{
    private Logger logger = LoggerFactory.getLogger( AppContext.class );

    public void publishContext( AppContextRequest request, AppContext context )
    {
        for ( Entry<Object, Object> entry : context.entrySet() )
        {
            logger.info( "Property \"{}\"=\"{}\" inserted into AppContext.", String.valueOf( entry.getKey() ), String
                .valueOf( entry.getValue() ) );
        }
    }
}
