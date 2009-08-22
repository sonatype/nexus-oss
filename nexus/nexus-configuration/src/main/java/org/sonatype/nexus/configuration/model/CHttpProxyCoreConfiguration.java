package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CHttpProxyCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CHttpProxyCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }

    @Override
    public CHttpProxySettings getConfiguration( boolean forWrite )
    {
        return (CHttpProxySettings) super.getConfiguration( forWrite );
    }

    @Override
    protected CHttpProxySettings extractConfiguration( Configuration configuration )
    {
        return configuration.getHttpProxy();
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }
}
