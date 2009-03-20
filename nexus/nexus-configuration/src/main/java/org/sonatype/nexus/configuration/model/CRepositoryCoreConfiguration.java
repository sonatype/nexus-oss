package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ExternalConfiguration;

public class CRepositoryCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CRepositoryCoreConfiguration( CRepository configuration )
    {
        super( configuration );
    }

    @Override
    protected void copyTransients( Object source, Object destination )
    {
        ( (CRepository) destination ).externalConfigurationImple = ( (CRepository) source ).externalConfigurationImple;
    }

    public ExternalConfiguration getExternalConfiguration()
    {
        return ( (CRepository) getOriginalConfiguration() ).externalConfigurationImple;
    }
}
