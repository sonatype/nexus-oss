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
package org.sonatype.nexus.plugin;

import static java.util.Arrays.asList;
import static org.codehaus.plexus.util.FileUtils.createTempFile;
import static org.codehaus.plexus.util.FileUtils.forceDelete;
import static org.sonatype.nexus.restlight.common.AbstractRESTLightClient.SVC_BASE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.restlight.common.ProxyConfig;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageProfile;
import org.sonatype.nexus.restlight.stage.StageRepository;

public abstract class AbstractStagingMojo
    extends AbstractNexusMojo
{

    /**
     * If provided, and this repository is available for selection, use it.
     *
     * @parameter expression="${nexus.repositoryId}"
     */
    private String repositoryId;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private WagonManager wagonManager;

    private StageClient client;

    public AbstractStagingMojo()
    {
        super();
    }

    @Override
    protected final synchronized StageClient connect()
        throws RESTLightClientException
    {
        final String url = formatUrl( getNexusUrl() );

        setAndValidateProxy();
        getLog().info( "Logging into Nexus: " + url );
        getLog().info( "User: " + getUsername() );

        ProxyConfig proxyConfig = null;
        if ( getProxyHost() != null )
        {
            proxyConfig = new ProxyConfig( getProxyHost(), getProxyPort(), getProxyUsername(), getProxyPassword() );
        }

        client = new StageClient( url, getUsername(), getPassword(), proxyConfig );
        return client;
    }

    protected StageClient getClient()
        throws MojoExecutionException
    {
        if ( client == null )
        {
            try
            {
                connect();
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to connect to Nexus at: " + getNexusUrl() );
            }
        }

        return client;
    }

    protected void listRepos( final String groupId, final String artifactId, final String version, final String prompt )
        throws MojoExecutionException
    {
        List<StageRepository> repos;
        StringBuilder builder = new StringBuilder();

        try
        {
            if ( groupId != null )
            {
                repos = getClient().getClosedStageRepositoriesForUser( groupId, artifactId, version );
                builder.append( prompt ).append( " for: '" ).append( groupId ).append( ":" ).append(
                    artifactId ).append(
                    ":" ).append( version ).append( "':" );
            }
            else
            {
                repos = getClient().getClosedStageRepositories();
                builder.append( prompt ).append( ": " );
            }
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to list staging repositories: " + e.getMessage(), e );
        }

        if ( repos.isEmpty() )
        {
            builder.append( "\n\nNone." );
        }
        else
        {
            for ( StageRepository repo : repos )
            {
                builder.append( "\n\n-  " );
                builder.append( listRepo( repo ) );
            }
        }

        builder.append( "\n\n" );

        getLog().info( builder.toString() );
    }

    protected CharSequence listRepo( final StageRepository repo )
    {
        StringBuilder builder = new StringBuilder();

        builder.append( repo.getRepositoryId() ).append( " (profile: " ).append( repo.getProfileName() ).append( ")" );

        if ( repo.getUrl() != null )
        {
            builder.append( "\n   URL: " ).append( repo.getUrl() );
        }

        if ( repo.getDescription() != null )
        {
            builder.append( "\n   Description: " ).append( repo.getDescription() );
        }

        builder.append( "\n   Details: (user: " ).append( repo.getUser() ).append( ", " );
        builder.append( "ip: " ).append( repo.getIpAddress() ).append( ", " );
        builder.append( "user agent: " ).append( repo.getUserAgent() ).append( ")" );

        return builder;
    }

    protected CharSequence listProfile( final StageProfile profile )
    {
        StringBuilder builder = new StringBuilder();

        builder.append( "Id: " ).append( profile.getProfileId() ).append( "\tname: " ).append(
            profile.getName() ).append(
            "\tmode: " ).append( profile.getMode() );

        return builder;
    }

    protected StageRepository select( final List<StageRepository> stageRepos, final String basicPrompt,
                                      final boolean allowAutoSelect )
        throws MojoExecutionException
    {
        List<StageRepository> stageRepositories = stageRepos;

        if ( stageRepositories == null || stageRepositories.isEmpty() )
        {
            throw new MojoExecutionException( "No repositories available." );
        }

        if ( allowAutoSelect && isAutomatic() )
        {
            stageRepositories = filterForAutomaticSelection( stageRepositories );
            if ( stageRepositories == null || stageRepositories.isEmpty() )
            {
                throw new MojoExecutionException( "No repositories available." );
            }
        }

        if ( getRepositoryId() != null )
        {
            for ( StageRepository repo : stageRepositories )
            {
                if ( getRepositoryId().equals( repo.getRepositoryId() ) )
                {
                    return repo;
                }
            }
        }

        if ( allowAutoSelect && isAutomatic() && stageRepositories.size() == 1 )
        {
            StageRepository repo = stageRepositories.get( 0 );
            getLog().info( "Using the only staged repository available: " + repo.getRepositoryId() );

            return repo;
        }

        LinkedHashMap<String, StageRepository> repoMap = new LinkedHashMap<String, StageRepository>();
        StringBuilder menu = new StringBuilder();
        List<String> choices = new ArrayList<String>();

        menu.append( "\n\n\nAvailable Staging Repositories:\n\n" );

        int i = 0;
        for ( StageRepository repo : stageRepositories )
        {
            ++i;
            repoMap.put( Integer.toString( i ), repo );
            choices.add( Integer.toString( i ) );

            menu.append( "\n" ).append( i ).append( ": " ).append( listRepo( repo ) ).append( "\n" );
        }

        menu.append( "\n\n" );

        if ( isAutomatic() )
        {
            getLog().info( menu.toString() );
            throw new MojoExecutionException(
                "Cannot auto-select; multiple staging repositories are available, and none are specified for use." );
        }
        else
        {
            String choice = null;
            while ( choice == null || !repoMap.containsKey( choice ) )
            {
                getLog().info( menu.toString() );
                try
                {
                    choice = getPrompter().prompt( basicPrompt, choices, "1" );
                }
                catch ( PrompterException e )
                {
                    throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
                }
            }

            return repoMap.get( choice );
        }
    }

    protected Identity whoAmI()
        throws MojoExecutionException
    {
        final Artifact whoAmIArtifact = artifactFactory.createArtifact(
            "whoami", "whoami", "1", null, "properties"
        );
        String serverId = getServerAuthId();
        if ( serverId == null )
        {
            serverId = "nexus-maven-plugin";
        }

        ArtifactRepository fakeLocal = null;
        try
        {
            final ArtifactRepository whoAmIRepository = new DefaultArtifactRepository(
                serverId, formatUrl( getNexusUrl() ) + SVC_BASE + "/staging/", new DefaultRepositoryLayout()
            )
            {
                @Override
                public String getUsername()
                {
                    if ( AbstractStagingMojo.this.getUsername() != null )
                    {
                        return AbstractStagingMojo.this.getUsername();
                    }
                    return super.getUsername();
                }

                @Override
                public String getPassword()
                {
                    if ( AbstractStagingMojo.this.getPassword() != null )
                    {
                        return AbstractStagingMojo.this.getPassword();
                    }
                    return super.getPassword();
                }

                @Override
                public String toString()
                {
                    return "(WhoAmI) " + super.toString();
                }
            };

            fakeLocal = new DefaultArtifactRepository(
                "whoami", createTempFile( "whoami-", "", null ).toURI().toASCIIString(), new DefaultRepositoryLayout()
            );

            // looks like Maven 2 does not give any chance of artifact provided username/password so try a fallback
            final AuthenticationInfo backupAuthInfo = wagonManager.getAuthenticationInfo( whoAmIRepository.getId() );
            wagonManager.addAuthenticationInfo( whoAmIRepository.getId(), getUsername(), getPassword(), null, null );

            try
            {
                artifactResolver.resolve( whoAmIArtifact, asList( whoAmIRepository ), fakeLocal );
            }
            finally
            {
                wagonManager.addAuthenticationInfo( whoAmIRepository.getId(),
                                                    backupAuthInfo.getUserName(), backupAuthInfo.getPassword(),
                                                    backupAuthInfo.getPrivateKey(), backupAuthInfo.getPassphrase() );
            }
            final File whoAmIFile = whoAmIArtifact.getFile();
            if ( whoAmIFile != null && whoAmIFile.exists() )
            {
                InputStream in = null;
                try
                {
                    in = new FileInputStream( whoAmIFile );
                    final Properties whoAmIProperties = new Properties();
                    whoAmIProperties.load( in );
                    final String ipAddress = whoAmIProperties.getProperty( "ipAddress" );
                    final String userAgent = whoAmIProperties.getProperty( "userAgent" );
                    if ( ipAddress != null && userAgent != null )
                    {
                        return new Identity( ipAddress, userAgent );
                    }
                }
                finally
                {
                    IOUtil.close( in );
                }
            }
        }
        catch ( Exception ignore )
        {
            // we ignore any exception during resolving as we could be talking with a nexus server that does not
            // support it
        }
        finally
        {
            if ( fakeLocal != null )
            {
                try
                {
                    forceDelete( fakeLocal.getBasedir() );
                }
                catch ( IOException ignore )
                {
                    // we did our best
                }
            }
        }
        return null;
    }

    /**
     * Filters out all repositories that does not match the current user ip / user agent.
     *
     * @param repositories to be filtered
     * @return filtered
     * @throws MojoExecutionException in case current user agent could not be determined
     */
    private List<StageRepository> filterForAutomaticSelection( final List<StageRepository> repositories )
        throws MojoExecutionException
    {
        final List<StageRepository> filtered = new ArrayList<StageRepository>();

        final Identity i = whoAmI();
        if ( i != null )
        {
            for ( final StageRepository repository : repositories )
            {
                if ( i.wasTheOneThatStaged( repository ) )
                {
                    filtered.add( repository );
                }
            }
            return filtered;
        }
        else
        {
            return repositories;
        }
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId( final String repositoryId )
    {
        this.repositoryId = repositoryId;
    }

    protected static class Identity
    {

        private final String ipAddress;

        private final String userAgent;

        private Identity( String ipAddress, String userAgent )
        {

            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }

        public String getIpAddress()
        {
            return ipAddress;
        }

        public String getUserAgent()
        {
            return userAgent;
        }

        public boolean wasTheOneThatStaged( final StageRepository repository )
        {
            return getIpAddress().equals( repository.getIpAddress() )
                && getUserAgent().equals( repository.getUserAgent() );
        }
    }

}