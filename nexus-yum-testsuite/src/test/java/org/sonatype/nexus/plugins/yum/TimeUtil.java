package org.sonatype.nexus.plugins.yum;

import java.util.concurrent.TimeUnit;

public final class TimeUtil
{
    public static void sleep( int timeout, TimeUnit unit )
        throws InterruptedException
    {
        Thread.sleep( unit.toMillis( timeout ) );
    }
}
