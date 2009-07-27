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
    public CRepository getConfiguration( boolean forWrite )
    {
        return (CRepository) super.getConfiguration( forWrite );
    }

    @Override
    protected void copyTransients( Object source, Object destination )
    {
        ( (CRepository) destination ).setExternalConfiguration( ( (CRepository) source ).getExternalConfiguration() );

        ( (CRepository) destination ).externalConfigurationImple = ( (CRepository) source ).externalConfigurationImple;

        // trick with RemoteStorage, which is an object, and XStream will not "overlap" it properly (ie. destionation !=
        // null but source == null)
        if ( ( (CRepository) source ).getRemoteStorage() == null )
        {
            ( (CRepository) destination ).setRemoteStorage( null );
        }
    }

    public ExternalConfiguration getExternalConfiguration()
    {
        return ( (CRepository) getOriginalConfiguration() ).externalConfigurationImple;
    }
}
