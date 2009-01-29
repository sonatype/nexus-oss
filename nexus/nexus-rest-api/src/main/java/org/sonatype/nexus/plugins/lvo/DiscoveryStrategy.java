package org.sonatype.nexus.plugins.lvo;

import java.io.IOException;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public interface DiscoveryStrategy
{
    DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException;
}
