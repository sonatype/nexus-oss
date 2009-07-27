package org.sonatype.nexus.rest;

import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class NexusCompat
{
    public static CRepository getRepositoryRawConfiguration( Repository repository )
    {
        return ((CRepositoryCoreConfiguration) repository.getCurrentCoreConfiguration()).getConfiguration( false );
    }
    
    public static String getRepositoryProviderRole( Repository repository )
    {
        return getRepositoryRawConfiguration( repository ).getProviderRole();
    }

    public static String getRepositoryProviderHint( Repository repository )
    {
        return getRepositoryRawConfiguration( repository ).getProviderHint();
    }

    public static String getRepositoryPolicy( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
        {
            return repository.adaptToFacet( MavenRepository.class ).getRepositoryPolicy().toString();
        }
        else
        {
            return null;
        }
    }
}
