package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CGlobalRemoteConnectionSettingsCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CGlobalRemoteConnectionSettingsCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }

    @Override
    public CRemoteConnectionSettings getConfiguration( boolean forWrite )
    {
        if ( getOriginalConfiguration() == null )
        {
            // create default
            CRemoteConnectionSettings newConn = new CRemoteConnectionSettings();

            newConn.setConnectionTimeout( 1000 );
            
            newConn.setRetrievalRetryCount( 3 );

            getApplicationConfiguration().getConfigurationModel().setGlobalConnectionSettings( newConn );

            setOriginalConfiguration( newConn );
        }

        return (CRemoteConnectionSettings) super.getConfiguration( forWrite );
    }

    @Override
    protected CRemoteConnectionSettings extractConfiguration( Configuration configuration )
    {
        return configuration.getGlobalConnectionSettings();
    }

    @Override
    public void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }
}
