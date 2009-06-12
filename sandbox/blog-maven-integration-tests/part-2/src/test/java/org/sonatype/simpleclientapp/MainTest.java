package org.sonatype.simpleclientapp;

import junit.framework.TestCase;

public class MainTest
    extends TestCase
{

    public void testExecute()
    {
        assertEquals( 0, Main.execute( null ) );
        assertEquals( 0, Main.execute( new String[] {} ) );
        assertEquals( 1, Main.execute( new String[] { "one" } ) );
        assertEquals( 6, Main.execute( new String[] { "one", "two", "three", "four", "five", "six" } ) );
    }

    public void testNew()
    {
        new Main();
    }

}
