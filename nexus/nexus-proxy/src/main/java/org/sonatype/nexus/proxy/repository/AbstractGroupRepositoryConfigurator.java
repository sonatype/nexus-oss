package org.sonatype.nexus.proxy.repository;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;

public abstract class AbstractGroupRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    @Override
    protected void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
        CRepositoryCoreConfiguration coreConfiguration )
        throws ConfigurationException
    {        
        super.doApplyConfiguration( repository, configuration, coreConfiguration );
        
        // Groups are read only
        repository.setWritePolicy( RepositoryWritePolicy.READ_ONLY );
    }
}
