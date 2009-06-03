package org.sonatype.nexus.proxy.registry;

import java.util.List;

import org.sonatype.nexus.proxy.repository.Repository;

public interface SecuredRepositoryRegistry
{

    Repository getRepository( String repositoryId );
    
    List<Repository> getRepositories();
    
    
    
    
}
