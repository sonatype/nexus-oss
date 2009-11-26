package org.sonatype.appcontext;

import java.util.Map.Entry;

/**
 * A publisher that publishes Application Context to terminal by printing it's content out.
 * 
 * @author cstamas
 */
public class TerminalContextPublisher
    implements ContextPublisher
{
    public void publishContext( AppContextFactory factory, AppContextRequest request, AppContext context )
    {
        for ( Entry<Object, Object> entry : context.entrySet() )
        {
            // dump it to System.out
            System.out.println( "Property \"" + String.valueOf( entry.getKey() ) + "\"=\""
                + String.valueOf( entry.getValue() ) + "\" inserted into AppContext." );
        }
    }
}
