package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CSmtpConfigurationCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CSmtpConfigurationCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CSmtpConfiguration getConfiguration( boolean forWrite )
    {
        return (CSmtpConfiguration) super.getConfiguration( forWrite );
    }

    @Override
    protected CSmtpConfiguration extractConfiguration( Configuration configuration )
    {
        return configuration.getSmtpConfiguration();
    }

    @Override
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
    }
}
