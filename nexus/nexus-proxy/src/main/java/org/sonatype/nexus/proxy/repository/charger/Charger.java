package org.sonatype.nexus.proxy.repository.charger;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Simple component to submit Charge instances for execution.
 * 
 * @author cstamas
 */
public interface Charger
{
    /**
     * Handy method to quickly assemble and execute a charge of work, with passed in Callables using passed in Strategy.
     * 
     * @param callables
     * @param strategy
     * @return
     */
    <E> ChargeFuture<E> submit( List<Callable<E>> callables, ChargeStrategy<E> strategy );

    /**
     * Handy method to quickly assemble and execute a charge of work, sharing one instance (!) of ExceptionHandler, with
     * passed in Callables using passed in Strategy.
     * 
     * @param callables
     * @param exceptionHandler
     * @param strategy
     * @return
     */
    <E> ChargeFuture<E> submit( List<Callable<E>> callables, ExceptionHandler exceptionHandler,
                                ChargeStrategy<E> strategy );

    /**
     * If you crufted manually a Charge instance, just toss it here to start it's execution.
     * 
     * @param charge
     * @return
     */
    <E> ChargeFuture<E> submit( Charge<E> charge );

    /**
     * Shut's down charger internals.
     */
    void shutdown();
}
