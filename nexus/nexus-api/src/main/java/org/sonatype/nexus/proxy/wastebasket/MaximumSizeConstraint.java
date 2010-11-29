package org.sonatype.nexus.proxy.wastebasket;

import org.sonatype.nexus.proxy.repository.Repository;

public interface MaximumSizeConstraint
{
    boolean isOverMaximum( SmartWastebasket wastebasket, Repository repository );
}
