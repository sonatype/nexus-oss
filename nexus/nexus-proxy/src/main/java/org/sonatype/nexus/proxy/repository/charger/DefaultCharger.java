package org.sonatype.nexus.proxy.repository.charger;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.threads.NexusThreadFactory;

import com.google.common.base.Preconditions;

@Component( role = Charger.class )
public class DefaultCharger
    implements Charger
{
    private ExecutorService executorService;

    public DefaultCharger()
    {
        this.executorService = Executors.newCachedThreadPool( new NexusThreadFactory( "charger", "Charger" ) );
    }

    public <E> ChargeFuture<E> submit( final List<Callable<E>> callables, final ChargeStrategy<E> strategy )
    {
        Preconditions.checkNotNull( callables );

        Charge<E> charge = getChargeInstance( strategy );

        for ( Callable<? extends E> callable : callables )
        {
            charge.addAmmo( callable, ( callable instanceof ExceptionHandler ) ? (ExceptionHandler) callable
                : NopExceptionHandler.NOOP );
        }

        return submit( charge );
    }

    public <E> ChargeFuture<E> submit( final List<Callable<E>> callables, final ExceptionHandler exceptionHandler,
                                       final ChargeStrategy<E> strategy )
    {
        Preconditions.checkNotNull( callables );

        Charge<E> charge = getChargeInstance( strategy );

        for ( Callable<? extends E> callable : callables )
        {
            charge.addAmmo( callable, exceptionHandler );
        }

        return submit( charge );
    }

    public <E> ChargeFuture<E> submit( Charge<E> charge )
    {
        Preconditions.checkNotNull( charge );

        charge.exec( executorService );

        return new ChargeFuture<E>( charge );
    }

    public void shutdown()
    {
        executorService.shutdownNow();
    }

    // ==

    protected <E> Charge<E> getChargeInstance( final ChargeStrategy<E> strategy )
    {
        return new Charge<E>( strategy );
    }
}
