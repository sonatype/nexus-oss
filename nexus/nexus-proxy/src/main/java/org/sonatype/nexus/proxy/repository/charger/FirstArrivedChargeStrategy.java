package org.sonatype.nexus.proxy.repository.charger;

import java.util.Collections;
import java.util.List;

/**
 * ChargeStrategy for "first with payload or unhandled exception". This strategy will block as long as first Callable
 * delivers some payload or fails with unhandled exception -- making whole Charge to fail. In case of "bail out", the
 * next Callable is processed in same way, as long as there are Callables.
 * 
 * @author cstamas
 * @param <E>
 */
public class FirstArrivedChargeStrategy<E>
    extends AbstractChargeStrategy<E>
{
    @Override
    public boolean isDone( final Charge<E> charge )
    {
        List<ChargeWrapperFuture<E>> ammoFutures = charge.getAmmoFutures();

        for ( ChargeWrapperFuture<E> f : ammoFutures )
        {
            if ( f.isDone() )
            {
                try
                {
                    if ( getFutureResult( f ) != null )
                    {
                        return true;
                    }
                }
                catch ( Exception e )
                {
                    // nope, not done but failed badly
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public List<E> getResult( final Charge<E> charge )
        throws Exception
    {
        final List<ChargeWrapperFuture<E>> futures = charge.getAmmoFutures();

        for ( ChargeWrapperFuture<E> f : futures )
        {
            E e = getFutureResult( f );

            if ( e != null )
            {
                return Collections.singletonList( e );
            }
        }

        return Collections.emptyList();
    }
}
