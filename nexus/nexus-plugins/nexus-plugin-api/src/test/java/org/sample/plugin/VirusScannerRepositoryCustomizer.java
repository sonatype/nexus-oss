package org.sample.plugin;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.RepositoryCustomizer;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

public class VirusScannerRepositoryCustomizer
    implements RepositoryCustomizer
{
    @Inject
    private @Named( "virusScanner" )
    RequestProcessor virusScannerRequestProcessor;

    public boolean isHandledRepository( Repository repository )
    {
        // handle proxy reposes only
        return repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class );
    }

    public void configureRepository( Repository repository )
        throws ConfigurationException
    {
        repository.getRequestProcessors().put( "virusScanner", virusScannerRequestProcessor );
    }

}
