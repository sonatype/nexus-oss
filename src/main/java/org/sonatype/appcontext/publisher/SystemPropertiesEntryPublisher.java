package org.sonatype.appcontext.publisher;

import java.util.Map.Entry;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A publisher that publishes Application Context back to to System properties, probably prefixed with keyPrefix, to
 * make it available for other system components like loggers, caches, etc.
 * 
 * @author cstamas
 */
public class SystemPropertiesEntryPublisher
    implements EntryPublisher
{
    /**
     * The prefix to be used to prefix keys ("prefix.XXX"), if set.
     */
    private final String keyPrefix;

    /**
     * Flag to force publishing. Otherwise, the system property will be set only if does not exists.
     */
    private final boolean override;

    /**
     * Constructs a publisher without prefix, will publish {@code key=values} with keys as is in context.
     * 
     * @param override
     */
    public SystemPropertiesEntryPublisher( final boolean override )
    {
        this.keyPrefix = null;
        this.override = override;
    }

    /**
     * Constructs a publisher with prefix, will publish context with {@code prefix.key=value}.
     * 
     * @param keyPrefix
     * @param override
     * @throws NullPointerException if {@code keyPrefix} is null
     */
    public SystemPropertiesEntryPublisher( final String keyPrefix, final boolean override )
    {
        this.keyPrefix = Preconditions.checkNotNull( keyPrefix );
        this.override = override;
    }

    public void publishEntries( final AppContext context )
    {
        for ( Entry<String, Object> entry : context.entrySet() )
        {
            String key = entry.getKey();
            String value = String.valueOf( entry.getValue() );

            // adjust the key name and put it back to System properties
            String sysPropKey = keyPrefix == null ? key : keyPrefix + key;

            if ( override || System.getProperty( sysPropKey ) == null )
            {
                System.setProperty( sysPropKey, (String) value );
            }
        }
    }
}
