package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CRestApiSettingsCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CRestApiSettingsCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CRestApiSettings getConfiguration( boolean forWrite )
    {
        return (CRestApiSettings) super.getConfiguration( forWrite );
    }

    @Override
    protected CRestApiSettings extractConfiguration( Configuration configuration )
    {
        return configuration.getRestApi();
    }

    @Override
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }
}
