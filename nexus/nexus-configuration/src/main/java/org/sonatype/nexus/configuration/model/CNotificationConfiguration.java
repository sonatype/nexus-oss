package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CNotificationConfiguration
    extends AbstractCoreConfiguration
{
    public CNotificationConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CNotification getConfiguration( boolean forWrite )
    {
        return (CNotification) super.getConfiguration( forWrite );
    }

    @Override
    protected CNotification extractConfiguration( Configuration configuration )
    {
        return configuration.getNotification();
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }
}
