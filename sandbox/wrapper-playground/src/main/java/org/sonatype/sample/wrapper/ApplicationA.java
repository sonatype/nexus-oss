package org.sonatype.sample.wrapper;

import java.io.IOException;

public class ApplicationA
{
    public static void main( String[] args )
        throws IOException
    {
        System.out.println( "This is application A installing application B!" );

        ApplicationB.installApplication();

        System.exit( 0 );
    }

    public static void installApplication()
        throws IOException
    {
        WrapperHelper.backupWrapperConf();

        WrapperHelper.swapInWrapperConf( WrapperHelper.getConfFile( "wrapper-a.conf" ) );
    }
}
