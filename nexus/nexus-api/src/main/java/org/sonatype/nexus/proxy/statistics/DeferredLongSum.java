package org.sonatype.nexus.proxy.statistics;

import java.util.Collection;

public class DeferredLongSum
    implements DeferredLong
{
    private final Collection<DeferredLong> longValues;

    public DeferredLongSum( Collection<DeferredLong> longValues )
    {
        this.longValues = longValues;
    }

    public boolean isDone()
    {
        for ( DeferredLong longValue : longValues )
        {
            if ( !longValue.isDone() )
            {
                return false;
            }
        }

        return true;
    }

    public Long getValue()
    {
        long result = 0;

        for ( DeferredLong longValue : longValues )
        {
            result += longValue.getValue();
        }

        return result;
    }

}
