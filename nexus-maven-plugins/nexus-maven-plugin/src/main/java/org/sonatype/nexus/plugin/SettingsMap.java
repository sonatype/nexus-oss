package org.sonatype.nexus.plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class SettingsMap
    extends LinkedHashMap<String, String>
    implements Map<String, String>
{

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public SettingsMap( Properties p )
    {
        super( (Map) p );
    }

    private static final long serialVersionUID = 3965158074707752467L;

    @Override
    public String get( Object k )
    {
        String[] keys = ( (String) k ).split( "\\|" );
        String key;
        String defaultValue = null;

        switch ( keys.length )
        {
            default:
                throw new IllegalArgumentException( "Invalid key " + k );
            case 3:
                // description, so?
            case 2:
                defaultValue = keys[1];
            case 1:
                key = keys[0];
        }

        String value = super.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
