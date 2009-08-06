package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CRepositoryGroupingCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CRepositoryGroupingCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CRepositoryGrouping getConfiguration( boolean forWrite )
    {
        return (CRepositoryGrouping) super.getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryGrouping extractConfiguration( Configuration configuration )
    {
        return configuration.getRepositoryGrouping();
    }

    @Override
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }
}
