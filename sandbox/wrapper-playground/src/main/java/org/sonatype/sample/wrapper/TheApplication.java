package org.sonatype.sample.wrapper;

import java.io.IOException;

public class TheApplication
{
    public static void main( String[] args )
        throws IOException
    {
        System.out.println( "This is The Main Application, that will now install ApplicationA." );

        ApplicationA.installApplication();

        System.out.println( "This is The Main Application, that will now going DOWN!" );

        System.exit( 0 );
    }
}
