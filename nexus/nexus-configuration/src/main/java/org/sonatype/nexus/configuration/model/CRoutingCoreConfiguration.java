package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
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
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }
}
