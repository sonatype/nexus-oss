package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.validation.ValidationResponse;
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
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }
}
