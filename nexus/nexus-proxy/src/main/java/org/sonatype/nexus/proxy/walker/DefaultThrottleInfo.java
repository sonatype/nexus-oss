package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.walker.WalkerThrottleController.ThrottleInfo;

/**
 * A simple single-threaded ThrottleInfo used in Walker implementation.
 * 
 * @author cstamas
 * @since 2.0
 */
public class DefaultThrottleInfo
    implements ThrottleInfo
{
    private final long walkStarted;

    private long totalProcessItemSpentMillis;

    private long totalProcessItemInvocationCount;

    private long lastProcessItemEnterTime;

    public DefaultThrottleInfo()
    {
        this.walkStarted = now();
        this.totalProcessItemSpentMillis = 0;
        this.totalProcessItemInvocationCount = 0;
    }

    protected long now()
    {
        return System.currentTimeMillis();
    }

    public void enterProcessItem()
    {
        this.lastProcessItemEnterTime = now();
    }

    public void exitProcessItem()
    {
        totalProcessItemSpentMillis += now() - lastProcessItemEnterTime;
        totalProcessItemInvocationCount++;
    }

    @Override
    public long getTotalProcessItemSpentMillis()
    {
        return totalProcessItemSpentMillis;
    }

    @Override
    public long getTotalProcessItemInvocationCount()
    {
        return totalProcessItemInvocationCount;
    }

    @Override
    public long getTotalTimeWalking()
    {
        return now() - walkStarted;
    }
}
