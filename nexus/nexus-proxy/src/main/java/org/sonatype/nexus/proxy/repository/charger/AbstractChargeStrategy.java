package org.sonatype.nexus.proxy.repository.charger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public abstract class AbstractChargeStrategy<E>
    implements ChargeStrategy<E>
{
    protected E getFutureResult( final ChargeWrapperFuture<E> future )
        throws Exception
    {
        try
        {
            return future.get();
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof InterruptedException )
            {
                // we bailed out, just ignore it then
            }
            else if ( e.getCause() instanceof Exception )
            {
                final Exception cause = (Exception) e.getCause();

                if ( !future.getChargeWrapper().handle( cause ) )
                {
                    throw cause;
                }
            }
            else
            {
                throw new RuntimeException( e.getCause() );
            }
        }
        catch ( CancellationException e )
        {
            // we ignore this
        }
        catch ( InterruptedException e )
        {
            // we ignore this
        }

        return null;
    }

    protected List<E> getAllResults( final Charge<E> charge )
        throws Exception
    {
        final List<ChargeWrapperFuture<E>> futures = charge.getAmmoFutures();

        final ArrayList<E> result = new ArrayList<E>( futures.size() );

        for ( ChargeWrapperFuture<E> f : futures )
        {
            E e = getFutureResult( f );

            if ( e != null )
            {
                result.add( e );
            }
        }

        return result;
    }

    // ==

    @Override
    public abstract boolean isDone( Charge<E> charge );

    @Override
    public abstract List<E> getResult( Charge<E> charge )
        throws Exception;

}
