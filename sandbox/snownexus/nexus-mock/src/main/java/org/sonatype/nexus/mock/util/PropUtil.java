package org.sonatype.nexus.mock.util;

public class PropUtil
{
    public static String get( String name, String def )
    {
        String val = System.getProperty( name, def );
        if ( val != null && ( val.startsWith( "${" ) && val.endsWith( "}" ) ) )
        {
            val = def;
        }

        return val;
    }

    public static int get( String name, int def )
    {
        return Integer.parseInt( get( name, String.valueOf( def ) ) );
    }
}
