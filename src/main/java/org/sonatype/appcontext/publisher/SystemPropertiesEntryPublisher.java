package org.sonatype.appcontext.publisher;

import java.util.Map.Entry;

import org.sonatype.appcontext.AppContext;

/**
 * A publisher that publishes Application Context back to to System properties, probably prefixed with keyPrefix, to
 * make it available for other system components like loggers, caches, etc.
 * 
 * @author cstamas
 */
public class SystemPropertiesEntryPublisher
    implements EntryPublisher
{
    private final String keyPrefix;

    public SystemPropertiesEntryPublisher()
    {
        this( null );
    }

    public SystemPropertiesEntryPublisher( final String keyPrefix )
    {
        this.keyPrefix = keyPrefix;
    }

    public void publishEntries( AppContext context )
    {
        for ( Entry<String, Object> entry : context.entrySet() )
        {
            String key = entry.getKey();

            String value = String.valueOf( entry.getValue() );

            // adjust the key name and put it back to System properties
            String sysPropKey = keyPrefix == null ? key : keyPrefix + key;

            if ( System.getProperty( sysPropKey ) == null )
            {
                System.setProperty( sysPropKey, (String) value );
            }
        }
    }
}
