package org.sonatype.nexus.proxy.wastebasket;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.statistics.DeferredLong;

public class AbsoluteMaximumSizeConstraint
    implements MaximumSizeConstraint
{
    private final long maximumSizeInBytes;

    public AbsoluteMaximumSizeConstraint( final long maximumSizeInBytes )
    {
        super();
        this.maximumSizeInBytes = maximumSizeInBytes;
    }

    public boolean isOverMaximum( SmartWastebasket wastebasket, Repository repository )
    {
        DeferredLong wastebasketSize = wastebasket.getSize( repository );

        if ( wastebasketSize.isDone() )
        {
            return wastebasketSize.getValue() > maximumSizeInBytes;
        }
        else
        {
            return false;
        }
    }

}
