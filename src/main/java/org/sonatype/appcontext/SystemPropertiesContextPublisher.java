package org.sonatype.appcontext;

import java.util.Map.Entry;

/**
 * A publisher that publishes Application Context back to to System properties prefixed with keyPrefix, to make it
 * available for other system components like loggers, caches, etc.
 * 
 * @author cstamas
 */
public class SystemPropertiesContextPublisher
    implements ContextPublisher
{
    /**
     * Keyprefix to publish application context.
     */
    private String keyPrefix = null;

    public void publishContext( AppContextFactory factory, AppContextRequest request, AppContextResponse context )
    {
        for ( Entry<Object, Object> entry : context.getContext().entrySet() )
        {
            String key = (String) entry.getKey();

            String value = (String) entry.getValue();

            // adjust the key name and put it back to System properties
            String sysPropKey = produceKeyPrefix( request ) + key;

            if ( System.getProperty( sysPropKey ) == null )
            {
                System.setProperty( sysPropKey, (String) value );
            }
        }
    }

    protected String produceKeyPrefix( AppContextRequest request )
    {
        if ( getKeyPrefix() == null )
        {
            return request.getName().toLowerCase() + ".";
        }
        else
        {
            return getKeyPrefix();
        }
    }

    public String getKeyPrefix()
    {
        return keyPrefix;
    }

    public void setKeyPrefix( String keyPrefix )
    {
        this.keyPrefix = keyPrefix;
    }
}
