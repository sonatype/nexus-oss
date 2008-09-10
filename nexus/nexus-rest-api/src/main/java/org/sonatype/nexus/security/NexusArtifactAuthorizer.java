package org.sonatype.nexus.security;

import javax.servlet.ServletRequest;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

public interface NexusArtifactAuthorizer
{
    String ROLE = NexusArtifactAuthorizer.class.getName();
    
    boolean authorizePath( Repository repository, String path );
    
    boolean authorizePath( ServletRequest request, ResourceStoreRequest rsr, String action );
}
