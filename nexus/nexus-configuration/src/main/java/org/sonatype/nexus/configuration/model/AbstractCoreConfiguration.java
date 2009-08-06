package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractCoreConfiguration
    extends AbstractRevertableConfiguration
    implements CoreConfiguration
{
    private ApplicationConfiguration applicationConfiguration;

    private ExternalConfiguration<?> externalConfiguration;

    public AbstractCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        setOriginalConfiguration( extractConfiguration( applicationConfiguration.getConfigurationModel() ) );

        this.applicationConfiguration = applicationConfiguration;
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    protected ExternalConfiguration<?> prepareExternalConfiguration( Object configuration )
    {
        // usually nothing, but CRepository and CPlugin does have them
        return null;
    }

    public ExternalConfiguration<?> getExternalConfiguration()
    {
        if ( externalConfiguration == null )
        {
            externalConfiguration = prepareExternalConfiguration( getOriginalConfiguration() );
        }

        return externalConfiguration;
    }

    @Override
    public boolean isDirty()
    {
        return isThisDirty() || ( getExternalConfiguration() != null && getExternalConfiguration().isDirty() );
    }

    @Override
    public void validateChanges()
        throws ConfigurationException
    {
        super.validateChanges();

        if ( getExternalConfiguration() != null )
        {
            getExternalConfiguration().validateChanges();
        }
    }

    @Override
    public void commitChanges()
        throws ConfigurationException
    {
        super.commitChanges();

        if ( getExternalConfiguration() != null )
        {
            getExternalConfiguration().commitChanges();
        }
    }

    @Override
    public void rollbackChanges()
    {
        super.rollbackChanges();

        if ( getExternalConfiguration() != null )
        {
            getExternalConfiguration().rollbackChanges();
        }
    }

    // ==

    protected abstract Object extractConfiguration( Configuration configuration );
}
