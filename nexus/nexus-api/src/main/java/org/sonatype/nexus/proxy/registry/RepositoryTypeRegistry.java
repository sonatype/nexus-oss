package org.sonatype.nexus.proxy.registry;

import java.util.Set;

/**
 * This is the registry of known repository types.
 * 
 * @author cstamas
 */
public interface RepositoryTypeRegistry
{
    Set<String> getExistingRepositoryTypes();

    ContentClass getTypeContentClass( String repositoryType );
}
