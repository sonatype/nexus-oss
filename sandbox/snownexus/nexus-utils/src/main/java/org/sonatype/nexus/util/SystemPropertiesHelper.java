package org.sonatype.nexus.util;

public class SystemPropertiesHelper
{
    public static int getInteger( String key, int defaultValue )
    {
        String value = System.getProperty( key );

        if ( value == null || value.trim().length() == 0 )
        {
            return defaultValue;
        }

        try
        {
            return Integer.valueOf( value );
        }
        catch ( NumberFormatException e )
        {
            return defaultValue;
        }
    }

    public static boolean getBoolean( String key, boolean defaultValue )
    {
        String value = System.getProperty( key );

        if ( value == null || value.trim().length() == 0 )
        {
            return defaultValue;
        }

        return Boolean.valueOf( value );
    }

}
