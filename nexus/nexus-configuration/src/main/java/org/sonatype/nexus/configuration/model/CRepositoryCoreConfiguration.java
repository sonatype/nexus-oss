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
        ( (CRepository) destination ).setExternalConfiguration( ( (CRepository) source ).getExternalConfiguration() );

        ( (CRepository) destination ).externalConfigurationImple = ( (CRepository) source ).externalConfigurationImple;

        // trick with RemoteStorage, which is an object, and XStream will not "overlap" it properly (ie. destionation !=
        // null but source == null)
        ( (CRepository) destination ).setRemoteStorage( ( (CRepository) source ).getRemoteStorage() );
    }

    public ExternalConfiguration getExternalConfiguration()
    {
        return ( (CRepository) getOriginalConfiguration() ).externalConfigurationImple;
    }
}
