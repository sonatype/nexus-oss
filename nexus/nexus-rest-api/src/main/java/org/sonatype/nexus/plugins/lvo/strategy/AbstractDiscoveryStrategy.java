package org.sonatype.nexus.plugins.lvo.strategy;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;

/**
 * A simple helper superclass that gives you some help with getting some basic level of services available.
 * 
 * @author cstamas
 */
public abstract class AbstractDiscoveryStrategy
    extends AbstractLogEnabled
    implements DiscoveryStrategy
{
    @Requirement
    private Nexus nexus;

    protected Nexus getNexus()
    {
        return nexus;
    }
}
