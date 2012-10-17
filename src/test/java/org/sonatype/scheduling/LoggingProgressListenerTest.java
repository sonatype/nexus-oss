package org.sonatype.scheduling;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.inject.BeanScanning;

public class LoggingProgressListenerTest
    extends PlexusTestCase
{
    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration containerConfiguration )
    {
        containerConfiguration.setAutoWiring( true );
        containerConfiguration.setClassPathScanning( BeanScanning.INDEX.name() );
    }

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
