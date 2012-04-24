package org.sonatype.nexus.plugin.deploy;

import java.io.File;
import java.io.IOException;

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
import org.sonatype.maven.mojo.execution.MojoExecution;
import org.sonatype.maven.mojo.logback.LogbackUtils;
import org.sonatype.maven.mojo.settings.MavenSettings;
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
     * Specifies the URL of remote Nexus to deploy to.
     * 
     * @parameter expression="${deployUrl}"
     */
    private String deployUrl;

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
     * Deploys staged artifacts.
     * 
     * @param source the file to stage
     * @param artifact the artifact definition
     * @param stagingRepository the repository to stage to
     * @param localRepository the local repository to install into
     * @throws ArtifactDeploymentException if an error occurred deploying the artifact
     */
    protected void deployStagedArtifacts()
        throws ArtifactDeploymentException
    {
        try
        {
            final ZapperRequest request = new ZapperRequest( getStagingDirectory(), deployUrl );

            final Server server = MavenSettings.selectServer( mavenSession.getSettings(), serverId );
            if ( server != null )
            {
                // TODO: secDispatcher
                final Server dServer = MavenSettings.decrypt( null, server );
                request.setRemoteUsername( dServer.getUsername() );
                request.setRemotePassword( dServer.getPassword() );
            }

            final Proxy proxy = MavenSettings.selectProxy( mavenSession.getSettings(), deployUrl );
            if ( proxy != null )
            {
                // TODO: secDispatcher
                final Proxy dProxy = MavenSettings.decrypt( null, proxy );
                request.setProxyProtocol( dProxy.getProtocol() );
                request.setProxyHost( dProxy.getHost() );
                request.setProxyPort( dProxy.getPort() );
                request.setProxyUsername( dProxy.getUsername() );
                request.setProxyPassword( dProxy.getPassword() );
            }

            LogbackUtils.syncLogLevelWithMaven( getLog() );
            getLog().info( "Deploying staged artifacts to: " + deployUrl );
            zapper.deployDirectory( request );
        }
        catch ( IOException e )
        {
            throw new ArtifactDeploymentException( "Cannot deploy!", e );
        }
        catch ( SecDispatcherException e )
        {
            throw new ArtifactDeploymentException( "Cannot decipher passwords for deploy!", e );
        }
    }

    // ==

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