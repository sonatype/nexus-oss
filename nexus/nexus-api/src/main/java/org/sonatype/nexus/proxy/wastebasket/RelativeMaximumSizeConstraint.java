package org.sonatype.nexus.proxy.wastebasket;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.statistics.DeferredLong;

public class RelativeMaximumSizeConstraint
    implements MaximumSizeConstraint
{
    private final double ratioThreshold;

    public RelativeMaximumSizeConstraint( final double ratioThreshold )
    {
        this.ratioThreshold = ratioThreshold;
    }

    public boolean isOverMaximum( SmartWastebasket wastebasket, Repository repository )
    {
        DeferredLong wastebasketSize = wastebasket.getSize( repository );

        if ( wastebasketSize.isDone() )
        {
            double actualRatio = wastebasketSize.getValue() / 1; // repository.getStatistics().getRepositoryUsefulSize();

            return actualRatio > ratioThreshold;
        }
        else
        {
            return false;
        }

    }
}
