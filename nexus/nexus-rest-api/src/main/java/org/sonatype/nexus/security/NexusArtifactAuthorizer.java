package org.sonatype.nexus.security;

import org.sonatype.nexus.proxy.repository.Repository;

public interface NexusArtifactAuthorizer
{
    String ROLE = NexusArtifactAuthorizer.class.getName();
    
    boolean authorizePath( Repository repository, String path );
}
