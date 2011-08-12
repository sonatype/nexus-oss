package org.sonatype.nexus.proxy.repository.charger;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Handle to a Charge's future. Despite it's name, it is NOT java.concurrent.Future implementor!
 * 
 * @author cstamas
 * @param <E>
 */
public class ChargeFuture<E>
{
    private final Charge<E> charge;

    public ChargeFuture( final Charge<E> charge )
    {
        this.charge = Preconditions.checkNotNull( charge );
    }

    /**
     * Cancels the execution of charge.
     * 
     * @return
     */
    public boolean cancel()
    {
        return charge.cancel();
    }

    /**
     * Returns true if charge "is done with work" according to it's strategy (does not mean all the Ammunition is
     * done!).
     * 
     * @return
     */
    public boolean isDone()
    {
        return charge.isDone();
    }

    /**
     * Returns the charge' results. This method BLOCKS as long Charge is not done.
     * 
     * @return
     * @throws Exception
     */
    public List<E> getResult()
        throws Exception
    {
        return charge.getResult();
    }
}
