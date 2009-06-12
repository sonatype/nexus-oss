package org.sonatype.simpleclientapp;

public class Main
{
    public static void main( String[] args )
    {
        int exitCode = execute( args );
        if ( exitCode != 0 )
        {
            System.exit( exitCode );
        }
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
