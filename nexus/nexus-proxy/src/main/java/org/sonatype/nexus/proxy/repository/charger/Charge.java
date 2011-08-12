package org.sonatype.nexus.proxy.repository.charger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Amount of parallel workload.
 * 
 * @author cstamas
 * @param <E>
 */
public class Charge<E>
{
    private final List<ChargeWrapper<E>> ammunition;

    private final List<ChargeWrapperFuture<E>> ammunitionFutures;

    private final ChargeStrategy<E> strategy;

    private volatile boolean done;

    public Charge( final ChargeStrategy<E> strategy )
    {
        this.strategy = strategy;

        this.ammunition = new ArrayList<ChargeWrapper<E>>();

        this.ammunitionFutures = new ArrayList<ChargeWrapperFuture<E>>();
    }

    public void addAmmo( final Callable<? extends E> callable, final ExceptionHandler exceptionHandler )
    {
        ammunition.add( new ChargeWrapper<E>( this, callable, exceptionHandler ) );
    }

    public List<ChargeWrapperFuture<E>> getAmmoFutures()
    {
        return ammunitionFutures;
    }

    public synchronized void exec( final ExecutorService service )
    {
        for ( ChargeWrapper<E> ammo : ammunition )
        {
            ammunitionFutures.add( new ChargeWrapperFuture<E>( ammo, service.submit( ammo ) ) );
        }
    }

    public boolean cancel()
    {
        if ( isDone() )
        {
            return false;
        }
        else
        {
            for ( ChargeWrapperFuture<E> future : ammunitionFutures )
            {
                future.cancel( false );
            }

            return true;
        }
    }

    public boolean isDone()
    {
        return done;
    }

    public List<E> getResult()
        throws Exception
    {
        return strategy.getResult( this );
    }

    public synchronized void checkIsDone()
    {
        if ( strategy.isDone( this ) )
        {
            done = true;
        }
    }
}
