package org.sonatype.nexus.proxy.walker;

/**
 * Handy abstract class to create new throttle controllers. Just override method you need.
 * 
 * @author cstamas
 * @since 2.0
 */
public abstract class AbstractWalkerThrottleController
    implements WalkerThrottleController
{
    @Override
    public void walkStarted( final WalkerContext context )
    {
        // nop
    }

    @Override
    public void walkEnded( final WalkerContext context, final ThrottleInfo info )
    {
        // nop
    }

    @Override
    public boolean isThrottled()
    {
        return false;
    }

    @Override
    public long throttleTime( final ThrottleInfo info )
    {
        return -1;
    }
}
