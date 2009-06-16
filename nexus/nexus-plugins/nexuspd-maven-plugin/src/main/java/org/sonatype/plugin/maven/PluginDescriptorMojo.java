package org.sonatype.plugin.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.plugin.ExtensionPoint;
import org.sonatype.plexus.plugin.Managed;
import org.sonatype.plugin.metadata.PluginMetadataGenerationRequest;
import org.sonatype.plugin.metadata.PluginMetadataGenerator;
import org.sonatype.plugin.metadata.gleaner.GleanerException;

/**
 * Generates a plugin's <tt>plugin.xml</tt> descriptor file based on the project's pom and class annotations.
 * 
 * @goal generate-metadata
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class PluginDescriptorMojo
    extends AbstractMojo
{

    /**
     * The output location for the generated plugin descriptor.
     * 
     * @parameter default-value="${project.build.outputDirectory}/META-INF/plugin.xml"
     * @required
     */
    private File generatedPluginMetadata;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

    /**
     * The ID of the target application. For example if this plugin was for the Nexus Repository Manager, the ID would
     * be, 'nexus'.
     * 
     * @parameter
     * @required
     */
    private String applicationId;

    /**
     * The edition of the target application. Some applications come in multiple flavors, OSS, PRO, Free, light, etc.
     * 
     * @parameter
     */
    private String applicationEdition;

    /**
     * The minimum product version of the target application.
     * 
     * @parameter
     */
    private String applicationMinVersion;

    /**
     * The maximum product version of the target application.
     * 
     * @parameter
     */
    private String applicationMaxVersion;

    /** @component */
    private PluginMetadataGenerator metadataGenerator;

    public void execute()
        throws MojoExecutionException,
            MojoFailureException
    {
        PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
        request.groupId = this.mavenProject.getGroupId();
        request.artifactId = this.mavenProject.getArtifactId();
        request.version = this.mavenProject.getVersion();
        request.name = this.mavenProject.getName();
        request.description = this.mavenProject.getDescription();
        request.pluginSiteURL = this.mavenProject.getUrl();

        request.applicationId = this.applicationId;
        request.applicationEdition = this.applicationEdition;
        request.applicationMinVersion = this.applicationMinVersion;
        request.applicationMaxVersion = this.applicationMaxVersion;

        // licenses
        if ( this.mavenProject.getLicenses() != null )
        {
            for ( License mavenLicenseModel : (List<License>) this.mavenProject.getLicenses() )
            {
                request.licenses.put( mavenLicenseModel.getName(), mavenLicenseModel.getUrl() );
            }
        }
        
        // dependencies
        if ( this.mavenProject.getDependencies() != null )
        {
            for ( Dependency mavenDependency : (List<Dependency>) this.mavenProject.getCompileDependencies() )
            {
                Dependency dependency = new Dependency();
                dependency.setGroupId( mavenDependency.getGroupId() );
                dependency.setArtifactId( mavenDependency.getArtifactId() );
                dependency.setVersion( mavenDependency.getVersion() );
                request.dependencies.add( dependency );
            }
        }

        request.outputFile = this.generatedPluginMetadata;
        request.classesDirectory = new File( mavenProject.getBuild().getOutputDirectory() );
        try
        {
            if ( mavenProject.getCompileClasspathElements() != null )
            {
                for ( String classpathElement : (List<String>) mavenProject.getCompileClasspathElements() )
                {
                    request.classpath.add( new File( classpathElement ) );
                }
            }
        }
        catch ( DependencyResolutionRequiredException e )
        {
            throw new MojoFailureException( "Plugin failed to resolve dependencies: " + e.getMessage(), e );
        }

        request.annotationClasses.add( ExtensionPoint.class );
        request.annotationClasses.add( Managed.class );

        // do the work
        try
        {
            this.metadataGenerator.generatePluginDescriptor( request );
        }
        catch ( GleanerException e )
        {
            throw new MojoFailureException( "Failed to generante plugin xml file: " + e.getMessage(), e );
        }

    }

}
