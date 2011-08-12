package org.sonatype.nexus.proxy.repository.charger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleExceptionHandler
    implements ExceptionHandler
{
    private final List<Class<? extends Exception>> exceptionClassesToKickIn;

    private boolean kickedIn = false;

    public SimpleExceptionHandler( final Class<? extends Exception>... exceptionClassesToKickIn )
    {
        this( Arrays.asList( exceptionClassesToKickIn ) );
    }

    public SimpleExceptionHandler( final List<Class<? extends Exception>> exceptionClassesToKickIn )
    {
        this.exceptionClassesToKickIn = new ArrayList<Class<? extends Exception>>();

        if ( exceptionClassesToKickIn != null )
        {
            this.exceptionClassesToKickIn.addAll( exceptionClassesToKickIn );
        }
    }

    public boolean isKickedIn()
    {
        return kickedIn;
    }

    @Override
    public boolean handle( Exception ex )
    {
        if ( exceptionClassesToKickIn.contains( ex.getClass() ) )
        {
            kickedIn = true;

            System.out.println( "Ignoring exception " + ex.toString() );

            return true;
        }

        return false;
    }

}
