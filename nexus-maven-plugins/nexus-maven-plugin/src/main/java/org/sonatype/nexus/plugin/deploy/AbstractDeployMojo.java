/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.deploy;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.maven.mojo.execution.MojoExecution;
import org.sonatype.maven.mojo.logback.LogbackUtils;
import org.sonatype.maven.mojo.settings.MavenSettings;
import org.sonatype.nexus.restlight.common.ProxyConfig;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

public abstract class AbstractDeployMojo
    extends AbstractMojo
{
    /**
     * Maven Session.
     * 
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    protected MavenSession mavenSession;

    /**
     * Base working directory.
     * 
     * @parameter default-value="${project.basedir}"
     * @required
     * @readonly
     */
    protected File basedir;

    /**
     * @parameter default-value="${plugin.groupId}"
     * @readonly
     */
    private String pluginGroupId;

    /**
     * @parameter default-value="${plugin.artifactId}"
     * @readonly
     */
    private String pluginArtifactId;

    /**
     * @component
     */
    private ArtifactDeployer deployer;

    /**
     * Component used to create an artifact.
     * 
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * Component used to create a repository.
     * 
     * @component
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * Map that contains the layouts.
     * 
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout" hint="default"
     */
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;

    /**
     * Map that contains the layouts.
     * 
     * @component role="org.sonatype.nexus.plugin.deploy.Zapper" hint="default"
     */
    private Zapper zapper;

    /**
     * Sec Dispatcher.
     * 
     * @component role="org.sonatype.plexus.components.sec.dispatcher.SecDispatcher" hint="default"
     */
    private SecDispatcher secDispatcher;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Flag whether Maven is currently in online/offline mode.
     * 
     * @parameter default-value="${settings.offline}"
     * @readonly
     */
    private boolean offline;

    /**
     * Parameter used to update the metadata to make the artifact as release.
     * 
     * @parameter expression="${updateReleaseInfo}" default-value="false"
     */
    protected boolean updateReleaseInfo;

    /**
     * Specifies an alternative staging directory to which the project artifacts should be deployed. By default, staging
     * will happen under {@code /target} folder of the top level module (from where Maven was invoked).
     * 
     * @parameter expression="${altStagingDirectory}"
     */
    private File altStagingDirectory;

    /**
     * Specifies the URL of remote Nexus to deploy to. If specified, no Staging V2 kicks in, just an "ordinary" deploy
     * will happen, deploying the locally staged artifacts (still, deferred deploy happens, they will be uploaded
     * together).
     * 
     * @parameter expression="${deployUrl}"
     */
    private String deployUrl;

    /**
     * Specifies the profile ID of remote Nexus where staging should happen. If not given, Nexus will be asked to
     * perform a "match" and that profile will be used.
     * 
     * @parameter expression="${stagingProfileId}"
     */
    private String stagingProfileId;

    /**
     * The base URL for a Nexus Professional instance that includes the nexus-staging-plugin.
     * 
     * @parameter expression="${nexusUrl}"
     */
    private String nexusUrl;

    /**
     * The ID of the server entry in the Maven settings.xml from which to pick credentials to contact the Insight
     * service.
     * 
     * @parameter expression="${serverId}" default-value="nexus"
     */
    private String serverId;

    protected MavenSession getMavenSession()
    {
        return mavenSession;
    }

    protected File getBasedir()
    {
        return basedir;
    }

    protected ArtifactDeployer getDeployer()
    {
        return deployer;
    }

    protected ArtifactFactory getArtifactFactory()
    {
        return artifactFactory;
    }

    protected ArtifactRepositoryFactory getRepositoryFactory()
    {
        return repositoryFactory;
    }

    protected ArtifactRepositoryLayout getDefaultArtifactRepositoryLayout()
    {
        return defaultArtifactRepositoryLayout;
    }

    protected ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    protected boolean isOffline()
    {
        return offline;
    }

    protected boolean isUpdateReleaseInfo()
    {
        return updateReleaseInfo;
    }

    /**
     * Stages an artifact from a particular file.
     * 
     * @param source the file to stage
     * @param artifact the artifact definition
     * @param stagingRepository the repository to stage to
     * @param localRepository the local repository to install into
     * @throws ArtifactDeploymentException if an error occurred deploying the artifact
     */
    protected void stageArtifact( File source, Artifact artifact, ArtifactRepository stagingRepository,
                                  ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        getDeployer().deploy( source, artifact, stagingRepository, localRepository );
    }

    /**
     * Uploads staged artifacts.
     * 
     * @param source the file to stage
     * @param artifact the artifact definition
     * @param stagingRepository the repository to stage to
     * @param localRepository the local repository to install into
     * @throws ArtifactDeploymentException if an error occurred deploying the artifact
     */
    protected void uploadStagedArtifacts()
        throws ArtifactDeploymentException
    {
        boolean successful = false;
        try
        {
            final String deployUrl = beforeUpload();
            final ZapperRequest request = new ZapperRequest( getStagingDirectory(), deployUrl );

            final Server server = MavenSettings.selectServer( mavenSession.getSettings(), serverId );
            if ( server != null )
            {
                final Server dServer = MavenSettings.decrypt( secDispatcher, server );
                request.setRemoteUsername( dServer.getUsername() );
                request.setRemotePassword( dServer.getPassword() );
            }

            final Proxy proxy = MavenSettings.selectProxy( mavenSession.getSettings(), deployUrl );
            if ( proxy != null )
            {
                final Proxy dProxy = MavenSettings.decrypt( secDispatcher, proxy );
                request.setProxyProtocol( dProxy.getProtocol() );
                request.setProxyHost( dProxy.getHost() );
                request.setProxyPort( dProxy.getPort() );
                request.setProxyUsername( dProxy.getUsername() );
                request.setProxyPassword( dProxy.getPassword() );
            }

            LogbackUtils.syncLogLevelWithMaven( getLog() );
            getLog().info( "Deploying staged artifacts to: " + deployUrl );
            zapper.deployDirectory( request );
            successful = true;
        }
        catch ( IOException e )
        {
            throw new ArtifactDeploymentException( "Cannot deploy!", e );
        }
        catch ( SecDispatcherException e )
        {
            throw new ArtifactDeploymentException( "Cannot decipher credentials for deploy!", e );
        }
        finally
        {
            afterUpload( successful );
        }
    }

    // ==

    private StageClient stageClient;

    private String stagingRepositoryId;

    protected void createStageClient()
        throws ArtifactDeploymentException
    {
        try
        {
            // defaults
            String username = "deployment";
            String password = "deployment123";

            if ( serverId != null )
            {
                final Server server = MavenSettings.selectServer( mavenSession.getSettings(), serverId );
                if ( server != null )
                {
                    getLog().info( "Using server credentials with ID \"" + serverId + "\" to communicate with Nexus." );
                    final Server dServer = MavenSettings.decrypt( secDispatcher, server );
                    username = dServer.getUsername();
                    password = dServer.getPassword();
                }
                else
                {
                    getLog().warn(
                        "Server credentials with ID \"" + serverId + "\" to communicate with Nexus are not found!" );
                }
            }
            else
            {
                getLog().info(
                    "Using Nexus default credentials, as no \"serverId\" parameter is supplied to get credentials from!" );
            }

            final Proxy proxy = MavenSettings.selectProxy( mavenSession.getSettings(), nexusUrl );
            ProxyConfig proxyConfig = null;
            if ( proxy != null )
            {
                final Proxy dProxy = MavenSettings.decrypt( secDispatcher, proxy );
                if ( dProxy.getHost() != null && dProxy.getUsername() != null )
                {
                    proxyConfig =
                        new ProxyConfig( dProxy.getHost(), dProxy.getPort(), dProxy.getUsername(), dProxy.getPassword() );
                }
                else if ( dProxy.getHost() != null )
                {
                    proxyConfig = new ProxyConfig( dProxy.getHost(), dProxy.getPort() );
                }
            }

            LogbackUtils.syncLogLevelWithMaven( getLog() );
            if ( proxyConfig != null )
            {
                stageClient = new StageClient( nexusUrl, username, password, proxyConfig );
                getLog().info( "Nexus RESTLight client created (configured with HTTP proxy)." );
            }
            else
            {
                stageClient = new StageClient( nexusUrl, username, password );
                getLog().info( "Nexus RESTLight client created." );
            }
        }
        catch ( RESTLightClientException e )
        {
            throw new ArtifactDeploymentException( "Cannot create RESTLight Nexus client!", e );
        }
        catch ( SecDispatcherException e )
        {
            throw new ArtifactDeploymentException( "Cannot decipher credentials for deploy!", e );
        }
        catch ( MalformedURLException e )
        {
            throw new ArtifactDeploymentException( "Malformed Nexus base URL!", e );
        }
    }

    protected String beforeUpload()
        throws ArtifactDeploymentException
    {
        if ( deployUrl != null )
        {
            getLog().info( "Performing normal upload against URL: " + deployUrl );
            return deployUrl;
        }
        else if ( nexusUrl != null )
        {
            try
            {
                getLog().info( "Initiating staging against Nexus on URL " + nexusUrl );
                createStageClient();

                final MavenProject currentProject = mavenSession.getCurrentProject();

                // if profile is not "targeted", perform a match and save the result
                if ( StringUtils.isBlank( stagingProfileId ) )
                {
                    stagingProfileId =
                        stageClient.getStageProfileForUser( currentProject.getGroupId(),
                            currentProject.getArtifactId(), currentProject.getVersion() );
                    getLog().info( "Using staging profile \"" + stagingProfileId + "\" (matched by Nexus)." );
                }
                else
                {
                    getLog().info( "Using staging profile \"" + stagingProfileId + "\" (configured by user)." );
                }

                stagingRepositoryId = stageClient.startRepository( stagingProfileId, "Started by nexus-maven-plugin" );
                getLog().info( "Using staging repository \"" + stagingRepositoryId + "\"." );
                return concat( nexusUrl, "/service/local/staging/deployByRepositoryId", stagingRepositoryId );
            }
            catch ( RESTLightClientException e )
            {
                throw new ArtifactDeploymentException( "Error before upload while managing staging repository!", e );
            }
        }
        else
        {
            throw new ArtifactDeploymentException( "No deploy URL set, nor Nexus BaseURL given!" );
        }
    }

    protected String concat( String... paths )
    {
        StringBuilder result = new StringBuilder();

        for ( String path : paths )
        {
            while ( path.endsWith( "/" ) )
            {
                path = path.substring( 0, path.length() - 1 );
            }
            if ( result.length() > 0 && !path.startsWith( "/" ) )
            {
                result.append( "/" );
            }
            result.append( path );
        }

        return result.toString();
    }

    protected void afterUpload( final boolean successful )
        throws ArtifactDeploymentException
    {
        // in any other case nothing happens
        // by having stagingRepositoryId string non-empty, it means open of it was successful
        if ( stagingRepositoryId != null )
        {
            try
            {
                final StageRepository repo = new StageRepository( stagingProfileId, stagingRepositoryId, true );
                if ( successful )
                {
                    getLog().info( "Closing staging repository." );
                    stageClient.finishRepository( repo, "Finished by nexus-maven-plugin." );
                }
                else
                {
                    getLog().info( "Dropping staging repository (due to unsuccesful upload)." );
                    stageClient.dropRepository( repo, "Dropped by nexus-maven-plugin (due to unsuccesful upload)." );
                }
            }
            catch ( RESTLightClientException e )
            {
                throw new ArtifactDeploymentException(
                    "Error after upload while managing staging repository! Staging repository in question is "
                        + stagingRepositoryId, e );
            }
        }
    }

    protected File getStagingDirectory()
    {
        if ( altStagingDirectory != null )
        {
            return altStagingDirectory;
        }
        else
        {
            final MavenProject firstWithThisMojo =
                MojoExecution.getFirstProjectWithMojoInExecution( mavenSession, pluginGroupId, pluginArtifactId );
            if ( firstWithThisMojo != null )
            {
                // the target of 1st project having this mojo defined
                return new File( firstWithThisMojo.getBasedir().getAbsolutePath(), "target/nexus-staging" );
            }
            else
            {
                // top level (invocation place)
                return new File( getMavenSession().getExecutionRootDirectory() + "/target/nexus-staging" );
            }
        }
    }

    protected ArtifactRepository getStagingRepositoryFor( final File stagingDirectory )
        throws MojoFailureException
    {
        if ( stagingDirectory != null )
        {
            if ( stagingDirectory.exists() && ( !stagingDirectory.canWrite() || !stagingDirectory.isDirectory() ) )
            {
                // it exists but is not writable or is not a directory
                throw new MojoFailureException(
                    "Staging failed: staging directory points to an existing file but is not a directory or is not writable!" );
            }
            else if ( !stagingDirectory.exists() )
            {
                // it does not exists, create it
                stagingDirectory.mkdirs();
            }

            try
            {
                final String id = "nexus";
                final String url = stagingDirectory.getCanonicalFile().toURI().toURL().toExternalForm();

                return getRepositoryFactory().createDeploymentArtifactRepository( id, url,
                    getDefaultArtifactRepositoryLayout(), true );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException(
                    "Staging failed: staging directory path cannot be converted to canonical one!", e );
            }
        }
        else
        {
            throw new MojoFailureException( "Staging failed: staging directory is null!" );
        }
    }

    // ==

    /**
     * Fails if Maven is invoked offline.
     * 
     * @throws MojoFailureException if Maven is invoked offline.
     */
    protected void failIfOffline()
        throws MojoFailureException
    {
        if ( offline )
        {
            throw new MojoFailureException( "Cannot deploy artifacts when Maven is in offline mode" );
        }
    }

    /**
     * Returns true if the current project is the last one being executed in this build that has this mojo defined.
     * 
     * @return true if last project is being built.
     */
    protected boolean isThisLastProjectWithThisMojoInExecution()
    {
        return MojoExecution.isCurrentTheLastProjectWithMojoInExecution( mavenSession, pluginGroupId, pluginArtifactId );
    }
}