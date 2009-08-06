package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
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
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
        
    }
}
