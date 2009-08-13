package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.ConfigurationException;
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
        // Groups are read only
        repository.setWritePolicy( RepositoryWritePolicy.READ_ONLY );
        
        // TODO Auto-generated method stub
        super.doApplyConfiguration( repository, configuration, coreConfiguration );
    }
}
