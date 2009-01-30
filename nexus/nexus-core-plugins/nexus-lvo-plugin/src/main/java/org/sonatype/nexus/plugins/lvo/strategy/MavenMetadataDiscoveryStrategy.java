package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * This is a "local" strategy, uses Nexus storage contents for calculation.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "maven-md" )
public class MavenMetadataDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest req )
        throws NoSuchRepositoryException,
            IOException
    {
        // TODO: Nexus already publishes over REST the resolving of LATEST and RELEASE version, which is basically the
        // same needed here. But the underlying API is sadly not accessible in "clean" programattical manner. Fix it,
        // and use it here too!
        return new DiscoveryResponse( req );
    }
}
