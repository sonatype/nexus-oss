package org.sonatype.nexus.plugins.p2.repository;

import org.sonatype.nexus.proxy.repository.ProxyRepository;

public interface UpdateSiteProxyRepository
    extends ProxyRepository
{
    void mirror( final boolean force );
    
    int getArtifactMaxAge();

    void setArtifactMaxAge( final int maxAge );

    int getMetadataMaxAge();
    
    void setMetadataMaxAge( final int metadataMaxAge );
}
