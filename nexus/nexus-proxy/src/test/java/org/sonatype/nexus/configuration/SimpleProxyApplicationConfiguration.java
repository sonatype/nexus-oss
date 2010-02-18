package org.sonatype.nexus.configuration;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings;
import org.sonatype.nexus.configuration.application.SimpleApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

@Component( role = ApplicationConfiguration.class )
public class SimpleProxyApplicationConfiguration
    extends SimpleApplicationConfiguration
        implements Initializable
{
    private RemoteStorageContext remoteStorageContext = null;
    
    @Requirement
    private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;
    
    @Requirement
    private GlobalHttpProxySettings globalHttpProxySettings;
    
    public void initialize()
        throws InitializationException
    {
        Configuration configuration = getConfigurationModel();

        configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        configuration.getGlobalConnectionSettings().setConnectionTimeout( 1000 );
        configuration.getGlobalConnectionSettings().setRetrievalRetryCount( 3 );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );

        // remote storage context
        remoteStorageContext = new DefaultRemoteStorageContext( null );

        try
        {
            globalRemoteConnectionSettings.configure( this );
            remoteStorageContext.setRemoteConnectionSettings( globalRemoteConnectionSettings );

            globalHttpProxySettings.configure( this );
            remoteStorageContext.setRemoteProxySettings( globalHttpProxySettings );
        }
        catch ( ConfigurationException e )
        {
            throw new InitializationException( "Error configuring nexus!", e );
        }   
    }
    
    @Override
    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return remoteStorageContext;
    }
}
