/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

@Component( role = ApplicationConfiguration.class )
public class SimpleApplicationConfiguration
    implements ApplicationConfiguration, Initializable
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;

    @Requirement
    private GlobalHttpProxySettings globalHttpProxySettings;

    private Configuration configuration;

    private RemoteStorageContext remoteStorageContext;

    public void initialize()
        throws InitializationException
    {
        this.configuration = new Configuration();

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
            throw new InitializationException( "Test environment is broken!", e );
        }
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
        return AbstractNexusTestCase.WORK_HOME;
    }

    public File getConfigurationDirectory()
    {
        return AbstractNexusTestCase.CONF_HOME;
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
        return new File( getWorkingDirectory(), "trash" );
    }

    public File getSecurityConfigurationFile()
    {
        return new File( getConfigurationDirectory(), "security.xml" );
    }

    public void saveConfiguration()
        throws IOException
    {
        // send events out, but nothing else
        applicationEventMulticaster.notifyEventListeners( new ConfigurationPrepareForSaveEvent( this ) );
        applicationEventMulticaster.notifyEventListeners( new ConfigurationCommitEvent( this ) );
        applicationEventMulticaster.notifyEventListeners( new ConfigurationSaveEvent( this ) );
    }

    public boolean isSecurityEnabled()
    {
        return false;
    }
}
