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
package org.sonatype.nexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RemoteNexus
    implements Nexus
{
    private final Context context;

    private final URL contextRoot;

    private final Client client;

    public RemoteNexus( Context ctx, String contextRoot )
        throws MalformedURLException
    {
        super();

        this.context = ctx;

        if ( contextRoot.endsWith( "/" ) )
        {
            contextRoot = contextRoot.substring( 0, contextRoot.length() - 1 );
        }

        this.contextRoot = new URL( contextRoot );

        if ( "http".equalsIgnoreCase( this.contextRoot.getProtocol() ) )
        {
            client = new Client( Protocol.HTTP );
        }
        else if ( "https".equalsIgnoreCase( this.contextRoot.getProtocol() ) )
        {
            client = new Client( Protocol.HTTPS );
        }
        else
        {
            throw new IllegalArgumentException( "RemoteNexus is able to handle only HTTP or HTTPS protocols!" );
        }
    }

    protected URL getServiceURL( String service )
    {
        URL result = null;

        try
        {
            String path = contextRoot.getPath() + "/service/local" + service;

            if ( contextRoot.getPort() != -1 )
            {
                result = new URL( contextRoot.getProtocol(), contextRoot.getHost(), contextRoot.getPort(), path );
            }
            else
            {
                result = new URL( contextRoot.getProtocol(), contextRoot.getHost(), contextRoot.getDefaultPort(), path );
            }
        }
        catch ( MalformedURLException e )
        {

        }

        return result;
    }

    public void clearCaches( String path, String repositoryId, String repositoryGroupId )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException
    {
        // TODO Auto-generated method stub

    }

    public void createRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void createRepositoryTemplate( CRepository settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteRepositoryShadowTemplate( String id )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteRepositoryTemplate( String id )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public StorageItem dereferenceLinkItem( StorageItem item )
        throws NoSuchRepositoryException,
            ItemNotFoundException,
            AccessDeniedException,
            RepositoryNotAvailableException,
            StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream getApplicationLogAsStream( String logFile )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getApplicationLogFiles()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream getConfigurationAsStream()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream getDefaultConfigurationAsStream()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public FeedRecorder getFeedRecorder()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<Repository> getRepositories()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Repository getRepository( String repoId )
        throws NoSuchRepositoryException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public SystemStatus getSystemState()
    {
        try
        {
            URL remoteServiceUrl = getServiceURL( "/status" );

            Request req = new Request( Method.GET, remoteServiceUrl.toString() );

            Response res = client.handle( req );

            XStreamRepresentation repr = new XStreamRepresentation(
                (XStream) context.getAttributes().get( ApplicationBridge.XML_XSTREAM ),
                res.getEntity().getText(),
                res.getEntity().getMediaType(),
                Language.ALL );

            return (SystemStatus) repr.getPayload( new SystemStatus() );
        }
        catch ( IOException e )
        {
            return null;
        }
    }

    public ArtifactInfo identifyArtifact( InputStream artifactContent )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<CRepositoryShadow> listRepositoryShadowTemplates()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<CRepository> listRepositoryTemplates()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String readDefaultApplicationLogDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRouting readDefaultRouting()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String readDefaultWorkingDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRepositoryShadow readRepositoryShadowTemplate( String id )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRepository readRepositoryTemplate( String id )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void reindexAllRepositories()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void reindexRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void reindexRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        // TODO Auto-generated method stub

    }
    
    public void rebuildAttributesAllRepositories()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void rebuildAttributesRepository( String repositoryId )
        throws NoSuchRepositoryException, IOException
    {
        // TODO Auto-generated method stub

    }

    public void rebuildAttributesRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException, IOException
    {
        // TODO Auto-generated method stub

    }

    public Collection<ArtifactInfo> searchArtifactFlat( String term, String repositoryId, String groupId )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateRepositoryTemplate( CRepository settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void createGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void createRepository( CRepository settings )
        throws ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void createRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void createRepositoryShadow( CRepositoryShadow settings )
        throws ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteGlobalRemoteHttpProxySettings()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteGroupsSettingPathMapping( String id )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteRepository( String id )
        throws NoSuchRepositoryException,
            IOException,
            ConfigurationException
    {
        // TODO Auto-generated method stub

    }

    public void deleteRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void deleteRepositoryShadow( String id )
        throws NoSuchRepositoryException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public Collection<CGroupsSettingPathMappingItem> listGroupsSettingPathMapping()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<CRepository> listRepositories()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<CRepositoryGroup> listRepositoryGroups()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<CRepositoryShadow> listRepositoryShadows()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String readApplicationLogDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CGroupsSettingPathMappingItem readGroupsSettingPathMapping( String id )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRepository readRepository( String id )
        throws NoSuchRepositoryException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRepositoryGroup readRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRepositoryShadow readRepositoryShadow( String id )
        throws NoSuchRepositoryException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CRouting readRouting()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String readWorkingDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateApplicationLogDirectory( String settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateRepository( CRepository settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            InvalidGroupingException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateRepositoryShadow( CRepositoryShadow settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateRouting( CRouting settings )
        throws ConfigurationException,
            IOException
    {
        // TODO Auto-generated method stub

    }

    public void updateWorkingDirectory( String settings )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getAuthenticationSourceType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isSecurityEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSecurity( boolean enabled, String authenticationSourceType )
    {
        // TODO Auto-generated method stub
        
    }

    public boolean isAnonymousAccessEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public String getDefaultAuthenticationSourceType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDefaultAnonymousAccessEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDefaultSecurityEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Collection<ArtifactInfo> searchArtifactFlat( String term, String term2, String term3, String term4,
        String repositoryId, String groupId )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isSimpleSecurityModel()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public String getBaseUrl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setBaseUrl( String baseUrl )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

}
