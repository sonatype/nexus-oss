package org.sonatype.nexus.test.utils;

import java.util.ResourceBundle;

public class TestProperties
{

    private static ResourceBundle bundle;

    // i hate doing this, but its really easy, and this is for tests. this class can be replaced easy, if we need to...
    static
    {
        bundle = ResourceBundle.getBundle( "baseTest" );
    }


    public static String getString( String key )
    {
        return bundle.getString( key );
    }


    public static Integer getInteger( String key)
    {
        String value = bundle.getString( key );
        return new Integer(value);
    }

}
