package org.sonatype.simpleclientapp;

public class Main
{
    public static void main( String[] args )
    {
        System.exit( execute( args ) );
    }

    public static int execute( String[] args )
    {
        if ( args == null )
        {
            return 0;
        }

        return args.length;
    }
}
