package org.sonatype.nexus.proxy.walker;

/**
 * Interface to controle throttling (if desired) in executing of Walks.
 * 
 * @author cstamas
 * @since 2.0
 */
public interface WalkerThrottleController
{
    /**
     * Key used in RequestContext to pass it into Walker.
     */
    String CONTEXT_KEY = WalkerThrottleController.class.getName();

    /**
     * WalkerThrottleController that does not emply walk throttling.
     */
    WalkerThrottleController NO_THROTTLING = new WalkerThrottleController()
    {
        @Override
        public boolean isThrottled()
        {
            return false;
        }

        @Override
        public long throttleTime( long processItemSpentMillis )
        {
            return -1;
        }
    };

    /**
     * Returns true if the controllers wants to use "throttled walker" execution. Throttling in this case would mean
     * intentionally slowing down the "walk" by inserting some amount (see {@link #throttleTime(long)} method) of
     * Thread.sleep() invocations.
     * 
     * @return
     */
    boolean isThrottled();

    /**
     * Returns the next desired sleep time this context wants to have applied. It might be in some relation to the time
     * spent in processItem() methods of registered WalkerProcessors, but does not have to be.
     * 
     * @param processItemSpentMillis time in millis WalkerProcessors spent in their
     *            {@link WalkerProcessor#processItem(WalkerContext, org.sonatype.nexus.proxy.item.StorageItem)} method.
     * @return any value bigger than zero means "sleep as many millis to throttle". Any values less or equal to zero are
     *         neglected (will not invoke sleep).
     */
    long throttleTime( long processItemSpentMillis );
}
