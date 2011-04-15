package org.sonatype.scheduling;

import org.codehaus.plexus.PlexusTestCase;

public class LoggingProgressListenerTest
    extends PlexusTestCase
{
    public void testSimple()
    {
        LoggingProgressListener pl = new LoggingProgressListener( "foo" );

        pl.beginTask( "Task1", 10 );

        pl.working( 3 );
        
        pl.working( "Hm, this is hard!", 3 );

        pl.beginTask( "Task2", 10 );

        pl.working( 3 );

        pl.beginTask( "Task3", 10 );

        pl.working( 3 );
        pl.working( "Hm, this is hard!", 5 );

        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
    }

    public void testSimpleUnknown()
    {
        LoggingProgressListener pl = new LoggingProgressListener( "foo" );

        pl.beginTask( "Task1", ProgressListener.UNKNOWN );

        pl.working( 3 );
        
        pl.working( "Hm, this is hard!", 3 );

        pl.beginTask( "Task2", 10 );

        pl.working( 3 );

        pl.beginTask( "Task3", 10 );

        pl.working( 3 );
        pl.working( "Hm, this is hard!", 5 );

        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
    }

}
