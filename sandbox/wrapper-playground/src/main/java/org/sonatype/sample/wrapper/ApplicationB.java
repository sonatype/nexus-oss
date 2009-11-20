package org.sonatype.sample.wrapper;

import java.io.IOException;

public class ApplicationB
{
    public static void main( String[] args )
        throws IOException
    {
        System.out.println( "This is application B installing application A!" );

        ApplicationA.installApplication();

        System.exit( 0 );
    }

    public static void installApplication()
        throws IOException
    {
        WrapperHelper.backupWrapperConf();

        WrapperHelper.swapInWrapperConf( WrapperHelper.getConfFile( "wrapper-b.conf" ) );
    }
}
