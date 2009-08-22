package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CRoutingCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CRoutingCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CRouting getConfiguration( boolean forWrite )
    {
        return (CRouting) super.getConfiguration( forWrite );
    }

    @Override
    protected CRouting extractConfiguration( Configuration configuration )
    {
        return configuration.getRouting();
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }
}
