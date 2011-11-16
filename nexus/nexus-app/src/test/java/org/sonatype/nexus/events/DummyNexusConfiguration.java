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
package org.sonatype.nexus.events;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

public class DummyNexusConfiguration
    implements NexusConfiguration
{
    @Override
    public String getAnonymousUsername()
    {
        return "anonymous";
    }

    // ==

    @Override
    public File getWorkingDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getWorkingDirectory( String key )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getConfigurationDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getTemporaryDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSecurityEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public LocalStorageContext getGlobalLocalStorageContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveConfiguration()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Configuration getConfigurationModel()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSecurityEnabled( boolean enabled )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isAnonymousAccessEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAnonymousAccessEnabled( boolean enabled )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAnonymousUsername( String val )
        throws InvalidConfigurationException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getAnonymousPassword()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAnonymousPassword( String val )
        throws InvalidConfigurationException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getRealms()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRealms( List<String> realms )
        throws InvalidConfigurationException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<ScheduledTaskDescriptor> listScheduledTaskDescriptors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScheduledTaskDescriptor getScheduledTaskDescriptor( String id )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaultRepositoryMaxInstanceCount( int count )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRepositoryMaxInstanceCount( RepositoryTypeDescriptor rtd, int count )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getRepositoryMaxInstanceCount( RepositoryTypeDescriptor rtd )
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Repository createRepository( CRepository settings )
        throws ConfigurationException, IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadConfiguration()
        throws ConfigurationException, IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadConfiguration( boolean forceReload )
        throws ConfigurationException, IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ApplicationConfigurationSource getConfigurationSource()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInstanceUpgraded()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isConfigurationUpgraded()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isConfigurationDefaulted()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Repository createRepositoryFromModel( CRepository repository )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createInternals()
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropInternals()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, String> getConfigurationFiles()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NexusStreamResponse getConfigurationAsStreamByKey( String key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
