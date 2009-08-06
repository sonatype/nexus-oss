package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
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
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }
}
