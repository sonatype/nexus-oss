package org.sonatype.appcontext;

import java.util.Map;
import java.util.Properties;

/**
 * A ContextFiller that uses System.getProperties() as source, filters it for property keys starting with "aPrefix.".
 * 
 * @author cstamas
 */
public class SystemPropertiesContextFiller
    implements ContextFiller
{
    private String keyPrefix = null;

    public void fillContext( AppContextFactory factory, AppContextRequest request, Map<Object, Object> context )
    {
        /*
         * Iterate through system properties, insert all items into a map (making sure to do the translation needed,
         * remove "aPrefix." )
         */
        Properties sysProps = System.getProperties();

        String systemPropPrefix = produceKeyPrefix( request );

        for ( Object obj : sysProps.keySet() )
        {
            String key = obj.toString();

            if ( key.startsWith( systemPropPrefix ) && key.length() > systemPropPrefix.length() )
            {
                String plexusKey = key.substring( systemPropPrefix.length() );

                context.put( plexusKey, sysProps.get( obj ) );
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
