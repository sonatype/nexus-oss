package org.sonatype.plugin.nexus.testenvironment;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author velo
 * @goal setup-nexus-plugin-environment
 * @requiresDependencyResolution test
 * @phase pre-integration-test
 */
public class PluginEnvironmentMojo
    extends AbstractEnvironmentMojo
{

    /**
     * @parameter expression="${nexus.version}"
     * @required
     */
    private String nexusVersion;

    @Override
    protected Artifact getMavenArtifact( MavenArtifact ma )
        throws MojoExecutionException
    {
        if ( equivalent( ma, project.getArtifact() ) )
        {
            Artifact da;
            File bundle;
            if ( "nexus-plugin".equals( ma.getType() ) )
            {
                da =
                    artifactFactory.createArtifactWithClassifier( project.getArtifact().getGroupId(),
                                                                  project.getArtifact().getArtifactId(),
                                                                  project.getArtifact().getVersion(), "zip", "bundle" );
                bundle =
                    new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-bundle.zip" );
            }
            else
            {
                da =
                    artifactFactory.createArtifactWithClassifier( project.getArtifact().getGroupId(),
                                                                  project.getArtifact().getArtifactId(),
                                                                  project.getArtifact().getVersion(),
                                                                  project.getArtifact().getType(),
                                                                  project.getArtifact().getClassifier() );
                String classifier =
                    project.getArtifact().getClassifier() == null ? "" : "-" + project.getArtifact().getClassifier();
                bundle =
                    new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + classifier + "."
                        + project.getArtifact().getType() );
            }

            da.setResolved( true );
            bundle = bundle.getAbsoluteFile();
            if ( !bundle.exists() )
            {
                throw new MojoExecutionException( "Project bundle doesn't exists " + bundle );
            }
            da.setFile( bundle );
            return da;
        }

        try
        {
            return super.getMavenArtifact( ma );
        }
        catch ( AbstractMojoExecutionException e )
        {
            if ( getLog().isDebugEnabled() )
            {
                getLog().warn( "Dependency not found: '" + ma + "', trying with version " + nexusVersion, e );
            }
            else
            {
                getLog().warn( "Dependency not found: '" + ma + "', trying with version " + nexusVersion );
            }

            Artifact artifact =
                artifactFactory.createArtifactWithClassifier( ma.getGroupId(), ma.getArtifactId(), nexusVersion,
                                                              ma.getType(), ma.getClassifier() );

            return resolve( artifact );
        }
    }

    private boolean equivalent( MavenArtifact ma, Artifact artifact )
    {
        if ( ma == artifact )
        {
            return true;
        }
        if ( ma == null )
        {
            return false;
        }

        if ( ma.getArtifactId() == null )
        {
            if ( artifact.getArtifactId() != null )
            {
                return false;
            }
        }
        else if ( !ma.getArtifactId().equals( artifact.getArtifactId() ) )
        {
            return false;
        }
        if ( ma.getClassifier() == null )
        {
            if ( artifact.getClassifier() != null )
            {
                return false;
            }
        }
        else if ( !ma.getClassifier().equals( artifact.getClassifier() ) )
        {
            return false;
        }
        if ( ma.getGroupId() == null )
        {
            if ( artifact.getGroupId() != null )
            {
                return false;
            }
        }
        else if ( !ma.getGroupId().equals( artifact.getGroupId() ) )
        {
            return false;
        }
        if ( ma.getType() == null )
        {
            if ( artifact.getType() != null )
            {
                return false;
            }
        }
        else if ( !ma.getType().equals( artifact.getType() ) )
        {
            return false;
        }
        return true;
    }

    @Override
    protected Collection<Artifact> getNexusPlugins()
        throws MojoExecutionException
    {
        Collection<Artifact> plugins = new LinkedHashSet<Artifact>();
        plugins.add( project.getArtifact() );
        plugins.addAll( super.getNexusPlugins() );
        return plugins;
    }

    @Override
    protected Collection<MavenArtifact> getNexusPluginsArtifacts()
        throws MojoExecutionException
    {
        Set<MavenArtifact> plugins = new LinkedHashSet<MavenArtifact>();

        if ( super.getNexusPluginsArtifacts() != null )
        {
            plugins.addAll( super.getNexusPluginsArtifacts() );
        }

        Collection<Artifact> depPlugins = getNexusPlugins();
        for ( Artifact artifact : depPlugins )
        {
            MavenArtifact ma =
                new MavenArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                                   artifact.getType() );
            ma.setVersion( artifact.getVersion() );
            plugins.add( ma );
        }

        return plugins;
    }

    @Override
    protected Collection<MavenArtifact> getExtraResourcesArtifacts()
    {
        Collection<MavenArtifact> artifacts = new LinkedHashSet<MavenArtifact>();
        artifacts.addAll( super.getExtraResourcesArtifacts() );
        artifacts.add( new MavenArtifact( "org.sonatype.nexus", "nexus-test-harness-launcher", "repo", "zip",
                                          "maven-repository" ) );
        return artifacts;
    }
}
