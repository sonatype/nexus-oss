package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CErrorReportingCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CErrorReportingCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CErrorReporting getConfiguration( boolean forWrite )
    {
        return (CErrorReporting) super.getConfiguration( forWrite );
    }

    @Override
    protected CErrorReporting extractConfiguration( Configuration configuration )
    {
        return configuration.getErrorReporting();
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }
}
