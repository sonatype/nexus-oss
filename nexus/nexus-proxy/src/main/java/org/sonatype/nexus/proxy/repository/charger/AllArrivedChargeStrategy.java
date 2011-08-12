package org.sonatype.nexus.proxy.repository.charger;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Strategy that will ensure all the payloads are here, are bailed out, or did fail.
 * 
 * @author cstamas
 * @param <E>
 */
public class AllArrivedChargeStrategy<E>
    extends AbstractChargeStrategy<E>
{
    @Override
    public boolean isDone( final Charge<E> charge )
    {
        // done if all done, otherwise not
        List<ChargeWrapperFuture<E>> ammoFutures = charge.getAmmoFutures();

        for ( Future<E> f : ammoFutures )
        {
            if ( !f.isDone() )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<E> getResult( final Charge<E> charge )
        throws Exception
    {
        return getAllResults( charge );
    }
}
