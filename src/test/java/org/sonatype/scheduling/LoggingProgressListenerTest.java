package org.sonatype.scheduling;

import org.junit.Test;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link LoggingProgressListener}.
 */
public class LoggingProgressListenerTest
    extends TestSupport
{
    @Test
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

    @Test
    public void testSimpleUnknown()
    {
        LoggingProgressListener pl = new LoggingProgressListener( "foo" );

        pl.beginTask( "Task1" );

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
