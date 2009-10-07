package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

@Component( role = ApplicationConfiguration.class )
public class MockApplicationConfiguration
    implements ApplicationConfiguration
{
    private Configuration configuration;

    private RemoteStorageContext remoteStorageContext = new SimpleRemoteStorageContext();

    public MockApplicationConfiguration()
    {
        super();

        this.configuration = new Configuration();

        configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );
    }

    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public Configuration getConfigurationModel()
    {
        return configuration;
    }

    public File getWorkingDirectory()
    {
        return new File( "target/plexus-home/" );
    }

    public File getWorkingDirectory( String key )
    {
        return new File( getWorkingDirectory(), key );
    }

    public File getTemporaryDirectory()
    {
        File result = new File( "target/tmp" );

        result.mkdirs();

        return result;
    }

    public File getWastebasketDirectory()
    {
        return getWorkingDirectory( "trash" );
    }

    public File getConfigurationDirectory()
    {
        File result = new File( getWorkingDirectory(), "conf" );
        if ( !result.exists() )
        {
            result.mkdirs();
        }
        return result;
    }

    public void saveConfiguration()
        throws IOException
    {
        // DO NOTHING, this is test
    }

    public boolean isSecurityEnabled()
    {
        return false;
    }
}
