/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.configuration.ConfigurationCommitEvent;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.configuration.ConfigurationSaveEvent;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

@Component( role = ApplicationConfiguration.class )
public class SimpleApplicationConfiguration
    implements ApplicationConfiguration, Contextualizable
{

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    private Configuration configuration;

    private LocalStorageContext localStorageContext = new SimpleLocalStorageContext();

    private RemoteStorageContext remoteStorageContext = new SimpleRemoteStorageContext();

    private File workingDirectory;

    public SimpleApplicationConfiguration()
    {
        super();

        this.configuration = new Configuration();

        // configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );
    }

    public LocalStorageContext getGlobalLocalStorageContext()
    {
        return localStorageContext;
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
        return workingDirectory;
    }

    public File getWorkingDirectory( String key )
    {
        return getWorkingDirectory( key, true );
    }

    public File getWorkingDirectory( String key, boolean create )
    {
        final File result = new File( getWorkingDirectory(), key );
        if ( !result.exists() )
        {
            result.mkdirs();
        }
        return result;
    }

    public File getTemporaryDirectory()
    {
        File dir = getWorkingDirectory( "tmp" );
        dir.mkdirs();

        return dir;
    }

    public File getWastebasketDirectory()
    {
        File dir = getWorkingDirectory( "trash" );
        dir.mkdirs();

        return dir;
    }

    public File getConfigurationDirectory()
    {
        File dir = new File( getWorkingDirectory(), "conf" );
        dir.mkdirs();

        return dir;
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

    @Override
    public void contextualize( final Context context )
        throws ContextException
    {
        try
        {
            workingDirectory = new File( (String) context.get( NexusTestSupport.WORK_CONFIGURATION_KEY ) );
        }
        catch ( ContextException e )
        {
            throw new RuntimeException( "Missing key from plexus context: " + NexusTestSupport.WORK_CONFIGURATION_KEY,
                e );
        }
    }

}
