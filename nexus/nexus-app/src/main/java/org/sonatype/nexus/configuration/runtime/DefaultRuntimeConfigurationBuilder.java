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
package org.sonatype.nexus.configuration.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.configuration.ModelloUtils;
import org.sonatype.nexus.configuration.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CAuthzSource;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryShadowArtifactVersionConstraint;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.access.AccessDecisionVoter;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.OpenAccessManager;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.ConstrainedM2ShadowRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.security.AuthenticationSource;
import org.sonatype.nexus.security.OpenAuthenticationSource;

/**
 * The Class DefaultRuntimeConfigurationBuilder. Todo: all the bad thing is now concentrated in this class. We are
 * playing container instead of container. TODO: Slim this class and rely onto Plexus!
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultRuntimeConfigurationBuilder
    extends AbstractLogEnabled
    implements RuntimeConfigurationBuilder, Contextualizable
{
    public static final String DEFAULT_LS_PROVIDER = "file";

    /**
     * The repository registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    private PlexusContainer plexusContainer;

    private NexusConfiguration nexusConfiguration;

    private HashMap<String, AccessManager> accessManagers = new HashMap<String, AccessManager>();

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize( NexusConfiguration configuration )
    {
        this.nexusConfiguration = configuration;
    }

    public Repository createRepositoryFromModel( Configuration configuration, CRepository repo )
        throws InvalidConfigurationException
    {
        try
        {
            Repository repository = null;

            // Setting contentClass specific things on a repository
            if ( CRepository.TYPE_MAVEN2.equals( repo.getType() ) )
            {
                MavenRepository m2repository = (MavenRepository) plexusContainer.lookup( Repository.ROLE, "maven2" );

                m2repository.setId( repo.getId() );
                m2repository.setName( repo.getName() );
                m2repository.setLocalStatus( LocalStatus.fromModel( repo.getLocalStatus() ) );
                m2repository.setAllowWrite( repo.isAllowWrite() );
                m2repository.setBrowseable( repo.isBrowseable() );
                m2repository.setProxyMode( ProxyMode.fromModel( repo.getProxyMode() ) );
                m2repository.setIndexable( repo.isIndexable() );
                m2repository.setNotFoundCacheTimeToLive( repo.getNotFoundCacheTTL() );

                m2repository.setItemMaxAge( repo.getArtifactMaxAge() );
                m2repository.setReleaseMaxAge( repo.getArtifactMaxAge() );
                m2repository.setSnapshotMaxAge( repo.getArtifactMaxAge() );
                m2repository.setMetadataMaxAge( repo.getMetadataMaxAge() );
                m2repository.setCleanseRepositoryMetadata( repo.isMaintainProxiedRepositoryMetadata() );
                m2repository.setChecksumPolicy( ChecksumPolicy.fromModel( repo.getChecksumPolicy() ) );

                if ( CRepository.REPOSITORY_POLICY_RELEASE.equals( repo.getRepositoryPolicy() ) )
                {
                    m2repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
                }
                else
                {
                    m2repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
                }

                repository = m2repository;
            }
            else if ( CRepository.TYPE_MAVEN1.equals( repo.getType() ) )
            {
                MavenRepository m1repository = (MavenRepository) plexusContainer.lookup( Repository.ROLE, "maven1" );

                m1repository.setId( repo.getId() );
                m1repository.setName( repo.getName() );
                m1repository.setLocalStatus( LocalStatus.fromModel( repo.getLocalStatus() ) );
                m1repository.setAllowWrite( repo.isAllowWrite() );
                m1repository.setBrowseable( repo.isBrowseable() );
                m1repository.setProxyMode( ProxyMode.fromModel( repo.getProxyMode() ) );
                m1repository.setIndexable( repo.isIndexable() );
                m1repository.setNotFoundCacheTimeToLive( repo.getNotFoundCacheTTL() );

                m1repository.setItemMaxAge( repo.getArtifactMaxAge() );
                m1repository.setReleaseMaxAge( repo.getArtifactMaxAge() );
                m1repository.setSnapshotMaxAge( repo.getArtifactMaxAge() );
                m1repository.setMetadataMaxAge( repo.getMetadataMaxAge() );
                m1repository.setCleanseRepositoryMetadata( repo.isMaintainProxiedRepositoryMetadata() );
                m1repository.setChecksumPolicy( ChecksumPolicy.fromModel( repo.getChecksumPolicy() ) );

                if ( CRepository.REPOSITORY_POLICY_RELEASE.equals( repo.getRepositoryPolicy() ) )
                {
                    m1repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
                }
                else
                {
                    m1repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
                }

                repository = m1repository;
            }
            else
            {
                throw new InvalidConfigurationException( "Repository type '" + repo.getType()
                    + "' creation is not (yet) implemented!" );
            }

            // Setting common things on a repository

            repository.setAccessManager( getAccessManagerForRealm( configuration, repo.getRealmId() ) );

            // NX-198: filling up the default variable to store the "default" local URL
            File defaultStorageFile = new File( new File( configuration.getWorkingDirectory(), "storage" ), repository
                .getId() );

            repo.defaultLocalStorageUrl = defaultStorageFile.toURL().toString();

            if ( repo.getLocalStorage() != null )
            {
                repository.setLocalUrl( repo.getLocalStorage().getUrl() );
                repository.setLocalStorage( getLocalRepositoryStorage( repo.getId(), repo
                    .getLocalStorage().getProvider() ) );

            }
            else
            {
                defaultStorageFile.mkdirs();
                repository.setLocalUrl( repo.defaultLocalStorageUrl );
                repository.setLocalStorage( getLocalRepositoryStorage( repository.getId(), DEFAULT_LS_PROVIDER ) );
            }

            if ( repo.getRemoteStorage() != null )
            {
                repository.setRemoteUrl( repo.getRemoteStorage().getUrl() );

                repository.setRemoteStorage( getRemoteRepositoryStorage( repo.getId(), repo
                    .getRemoteStorage().getProvider() ) );

                if ( repo.getRemoteStorage().getAuthentication() != null
                    || repo.getRemoteStorage().getConnectionSettings() != null
                    || repo.getRemoteStorage().getHttpProxySettings() != null )
                {
                    DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext( nexusConfiguration
                        .getRemoteStorageContext() );

                    ctx.setRemoteConnectionSettings( repo.getRemoteStorage().getConnectionSettings() );

                    ctx.setRemoteHttpProxySettings( repo.getRemoteStorage().getHttpProxySettings() );

                    ctx.setRemoteAuthenticationSettings( repo.getRemoteStorage().getAuthentication() );

                    repository.setRemoteStorageContext( ctx );
                }
                else
                {
                    repository.setRemoteStorageContext( nexusConfiguration.getRemoteStorageContext() );
                }
            }

            return repository;
        }
        catch ( MalformedURLException e )
        {
            throw new InvalidConfigurationException( "Malformed local storage URL!", e );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Could not lookup a new instance of Repository!", e );
        }
    }

    public Repository createRepositoryFromModel( Configuration configuration, CRepositoryShadow shadow )
        throws InvalidConfigurationException
    {
        try
        {
            ShadowRepository shadowRepository = null;

            Repository master = null;
            try
            {
                master = (Repository) repositoryRegistry.getRepository( shadow.getShadowOf() );
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new InvalidConfigurationException( "Shadow repository points to a nonexistent master with ID="
                    + shadow.getShadowOf() );
            }

            if ( CRepositoryShadow.TYPE_MAVEN1.equals( shadow.getType() ) )
            {
                shadowRepository = (ShadowRepository) plexusContainer.lookup(
                    Repository.ROLE,
                    "m2-m1-shadow" );

                shadowRepository.setMasterRepository( master );
                shadowRepository.setId( shadow.getId() );
                shadowRepository.setName( shadow.getName() );

                shadowRepository.setLocalStatus( LocalStatus.fromModel( shadow.getLocalStatus() ) );
                shadowRepository.setAllowWrite( false );
                shadowRepository.setBrowseable( true );
                shadowRepository.setProxyMode( null );
                shadowRepository.setIndexable( false );
                shadowRepository.setNotFoundCacheTimeToLive( master.getNotFoundCacheTimeToLive() );

                shadowRepository.setItemMaxAge( ( (MavenRepository) master ).getItemMaxAge() );
            }
            else if ( CRepositoryShadow.TYPE_MAVEN2.equals( shadow.getType() ) )
            {
                shadowRepository = (ShadowRepository) plexusContainer.lookup(
                    Repository.ROLE,
                    "m1-m2-shadow" );

                shadowRepository.setMasterRepository( master );
                shadowRepository.setId( shadow.getId() );
                shadowRepository.setName( shadow.getName() );

                shadowRepository.setLocalStatus( LocalStatus.fromModel( shadow.getLocalStatus() ) );
                shadowRepository.setAllowWrite( false );
                shadowRepository.setBrowseable( true );
                shadowRepository.setProxyMode( null );
                shadowRepository.setIndexable( false );
                shadowRepository.setNotFoundCacheTimeToLive( master.getNotFoundCacheTimeToLive() );

                shadowRepository.setItemMaxAge( ( (MavenRepository) master ).getItemMaxAge() );
            }
            else if ( CRepositoryShadow.TYPE_MAVEN2_CONSTRAINED.equals( shadow.getType() ) )
            {
                shadowRepository = (ShadowRepository) plexusContainer.lookup(
                    Repository.ROLE,
                    "m2-constrained" );

                shadowRepository.setMasterRepository( master );
                shadowRepository.setId( shadow.getId() );
                shadowRepository.setName( shadow.getName() );

                shadowRepository.setLocalStatus( LocalStatus.fromModel( shadow.getLocalStatus() ) );
                shadowRepository.setAllowWrite( false );
                shadowRepository.setBrowseable( true );
                shadowRepository.setProxyMode( null );
                shadowRepository.setIndexable( false );
                shadowRepository.setNotFoundCacheTimeToLive( master.getNotFoundCacheTimeToLive() );

                shadowRepository.setItemMaxAge( ( (MavenRepository) master ).getItemMaxAge() );

                if ( shadow.getArtifactVersionConstraints() != null )
                {
                    Map<String, String> versionMap = new HashMap<String, String>( shadow
                        .getArtifactVersionConstraints().size() );

                    for ( CRepositoryShadowArtifactVersionConstraint constraint : (List<CRepositoryShadowArtifactVersionConstraint>) shadow
                        .getArtifactVersionConstraints() )
                    {
                        versionMap.put( constraint.getGroupId() + ":" + constraint.getArtifactId(), constraint
                            .getVersion() );
                    }

                    ( (ConstrainedM2ShadowRepository) shadowRepository ).setVersionMap( versionMap );
                }
            }
            else
            {
                throw new InvalidConfigurationException( "Shadow repository of " + shadow.getShadowOf()
                    + " has unsupported type: " + shadow.getType() );
            }

            // NX-198: filling up the default variable to store the "default" local URL
            File defaultStorageFile = new File(
                new File( configuration.getWorkingDirectory(), "storage" ),
                shadowRepository.getId() );

            defaultStorageFile.mkdirs();

            shadow.defaultLocalStorageUrl = defaultStorageFile.toURL().toString();

            shadowRepository.setLocalUrl( shadow.defaultLocalStorageUrl );

            shadowRepository
                .setLocalStorage( getLocalRepositoryStorage( shadowRepository.getId(), DEFAULT_LS_PROVIDER ) );

            shadowRepository.setAccessManager( getAccessManagerForRealm( configuration, shadow.getRealmId() ) );

            if ( shadow.isSyncAtStartup() )
            {
                shadowRepository.synchronizeWithMaster();
            }
            return shadowRepository;

        }
        catch ( MalformedURLException e )
        {
            throw new InvalidConfigurationException( "Malformed local storage URL!", e );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Could not lookup a new instance of Repository!", e );
        }
    }

    public AuthenticationSource getAuthenticationSource( Configuration configuration )
        throws InvalidConfigurationException
    {
        if ( configuration.getSecurity().isEnabled() )
        {
            try
            {
                return (AuthenticationSource) plexusContainer.lookup( AuthenticationSource.ROLE, configuration
                    .getSecurity().getAuthenticationSource().getType() );
            }
            catch ( ComponentLookupException e )
            {
                throw new InvalidConfigurationException( "Unsupported authentication source type: "
                    + configuration.getSecurity().getAuthenticationSource().getType(), e );
            }
        }
        else
        {
            return new OpenAuthenticationSource();
        }
    }

    public AccessManager getAccessManagerForRealm( Configuration configuration, String realm )
        throws InvalidConfigurationException
    {
        if ( configuration.getSecurity().isEnabled() && realm != null )
        {
            if ( accessManagers.containsKey( realm ) )
            {
                return accessManagers.get( realm );
            }
            else
            {
                CAuthzSource authz = null;
                try
                {
                    AccessManager am = (AccessManager) plexusContainer.lookup( AccessManager.ROLE, "affirmative" );

                    for ( CAuthzSource authzSource : (List<CAuthzSource>) configuration.getSecurity().getRealms() )
                    {
                        if ( realm.equals( authzSource.getId() ) )
                        {
                            authz = authzSource;

                            break;
                        }
                    }

                    if ( authz == null )
                    {
                        throw new InvalidConfigurationException( "Could not find realm with ID=" + realm );
                    }

                    AccessDecisionVoter voter = (AccessDecisionVoter) plexusContainer.lookup(
                        AccessDecisionVoter.ROLE,
                        authz.getType() );

                    voter.setConfiguration( ModelloUtils.getMapFromConfigList( authz.getProperties() ) );

                    am.getVoters().add( voter );

                    accessManagers.put( realm, am );

                    return am;
                }
                catch ( ComponentLookupException e )
                {
                    throw new InvalidConfigurationException( "Unsupported authentication source type: "
                        + authz.getType(), e );
                }
            }
        }
        else
        {
            return new OpenAccessManager();
        }
    }

    // ----------------------------------------
    // private stuff

    private LocalRepositoryStorage getLocalRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return (LocalRepositoryStorage) plexusContainer.lookup( LocalRepositoryStorage.ROLE, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have local storage with unsupported provider: " + provider, e );
        }
    }

    private RemoteRepositoryStorage getRemoteRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return (RemoteRepositoryStorage) plexusContainer.lookup( RemoteRepositoryStorage.ROLE, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have remote storage with unsupported provider: " + provider, e );
        }
    }
}
