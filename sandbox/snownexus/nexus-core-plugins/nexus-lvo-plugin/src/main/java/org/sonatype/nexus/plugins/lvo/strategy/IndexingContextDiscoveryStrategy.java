package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * This is a "local" strategy, uses Nexus index contents for calculation. Since Nexus index is updated on-the-fly, as
 * soon as something gets deployed to Nexus, it will appear on the index too, and hence, will be published.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "index" )
public class IndexingContextDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{

    // TODO TONI - Uses IndexingManager which does not exist anymore.
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest req )
        throws NoSuchRepositoryException, IOException
    {
        return new DiscoveryResponse( req );
    }
}
