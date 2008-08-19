/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.application;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;

public interface MutableConfiguration
{
    // ----------------------------------------------------------------------------------------------------------
    // Repositories
    // ----------------------------------------------------------------------------------------------------------

    boolean isSecurityEnabled();

    void setSecurityEnabled( boolean enabled )
        throws IOException;

    boolean isAnonymousAccessEnabled();

    void setAnonymousAccessEnabled( boolean enabled )
        throws IOException;

    String getAnonymousUsername();

    void setAnonymousUsername( String val )
        throws IOException;

    String getAnonymousPassword();

    void setAnonymousPassword( String val )
        throws IOException;

    List<String> getRealms();

    // ----------------------------------------------------------------------------
    // ContentClasses
    // ----------------------------------------------------------------------------

    Collection<ContentClass> listRepositoryContentClasses();

    // ----------------------------------------------------------------------------------------------------------
    // REST API
    // ----------------------------------------------------------------------------------------------------------

    String getBaseUrl();

    void setBaseUrl( String baseUrl )
        throws IOException;

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections

    // Globals are mandatory: RU

    // CRemoteConnectionSettings are mandatory: RU

    CRemoteConnectionSettings readGlobalRemoteConnectionSettings();

    void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException,
            IOException;

    // CRemoteHttpProxySettings are optional: CRUD

    void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException;

    CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings();

    void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException;

    void deleteGlobalRemoteHttpProxySettings()
        throws IOException;

    // CRouting are mandatory: RU

    CRouting readRouting();

    void updateRouting( CRouting settings )
        throws ConfigurationException,
            IOException;

    // CRepository: CRUD

    Collection<CRepository> listRepositories();

    void createRepository( CRepository settings )
        throws ConfigurationException,
            IOException;

    CRepository readRepository( String id )
        throws NoSuchRepositoryException;

    void updateRepository( CRepository settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException;

    void deleteRepository( String id )
        throws NoSuchRepositoryException,
            IOException,
            ConfigurationException;

    // CRepositoryShadow: CRUD

    Collection<CRepositoryShadow> listRepositoryShadows();

    void createRepositoryShadow( CRepositoryShadow settings )
        throws ConfigurationException,
            IOException;

    CRepositoryShadow readRepositoryShadow( String id )
        throws NoSuchRepositoryException;

    void updateRepositoryShadow( CRepositoryShadow settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException;

    void deleteRepositoryShadow( String id )
        throws NoSuchRepositoryException,
            IOException;

    // CGroupsSettingPathMapping: CRUD

    Collection<CGroupsSettingPathMappingItem> listGroupsSettingPathMapping();

    void createGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException;

    CGroupsSettingPathMappingItem readGroupsSettingPathMapping( String id )
        throws IOException;

    void updateGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException;

    void deleteGroupsSettingPathMapping( String id )
        throws IOException;

    // CRepositoryGroup: CRUD

    Collection<CRepositoryGroup> listRepositoryGroups();

    void createRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            IOException;

    CRepositoryGroup readRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException;

    void updateRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            InvalidGroupingException,
            IOException;

    void deleteRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException,
            IOException;

    // CRepositoryTarget: CRUD

    Collection<CRepositoryTarget> listRepositoryTargets();

    void createRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException,
            IOException;

    CRepositoryTarget readRepositoryTarget( String id );

    void updateRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException,
            IOException;

    void deleteRepositoryTarget( String id )
        throws IOException;

    // CRemoteNexusInstance

    Collection<CRemoteNexusInstance> listRemoteNexusInstances();

    CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException;

    void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException;

    void deleteRemoteNexusInstance( String alias )
        throws IOException;

    // Smtp settings
    CSmtpConfiguration readSmtpConfiguration();

    void updateSmtpConfiguration( CSmtpConfiguration settings )
        throws ConfigurationException,
            IOException;
}
