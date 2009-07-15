package org.sonatype.plugin.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;
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
    private static final String NXPLUGIN_PACKAGING = "nexus-plugin";

    /**
     * The output location for the generated plugin descriptor.
     * 
     * @parameter default-value="${project.build.outputDirectory}/META-INF/nexus/plugin.xml"
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
     * @parameter expression="nexus"
     * @required
     */
    private String applicationId;

    /**
     * The edition of the target application. Some applications come in multiple flavors, OSS, PRO, Free, light, etc.
     * 
     * @parameter expression="OSS"
     */
    private String applicationEdition;

    /**
     * The minimum product version of the target application.
     * 
     * @parameter expression="1.4.0"
     */
    private String applicationMinVersion;

    /**
     * The maximum product version of the target application.
     * 
     * @parameter
     */
    private String applicationMaxVersion;

    /**
     * The list of user defined MIME types
     * 
     * @parameter
     */
    @SuppressWarnings( "unused" )
    private List<String> userMimeTypes;

    /**
     * The output location for the generated plugin descriptor.
     * 
     * @parameter default-value="${project.build.outputDirectory}/META-INF/nexus/userMimeTypes.properties"
     * @required
     * @readonly
     */
    private File userMimeTypesFile;

    /** @component */
    private PluginMetadataGenerator metadataGenerator;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !this.mavenProject.getPackaging().equals( NXPLUGIN_PACKAGING ) )
        {
            this.getLog().info( "Project is not of packaging type 'nexus-plugin'." );
            return;
        }

        // get the user customization
        Properties userMimeTypes = null;

        if ( userMimeTypes != null && !userMimeTypes.isEmpty() )
        {
            FileOutputStream fos = null;

            try
            {
                fos = new FileOutputStream( userMimeTypesFile );

                userMimeTypes.store( fos, "User MIME types" );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Cannot write the User MIME types file!", e );
            }
            finally
            {
                IOUtil.close( fos );
            }
        }

        PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
        request.setGroupId( this.mavenProject.getGroupId() );
        request.setArtifactId( this.mavenProject.getArtifactId() );
        request.setVersion( this.mavenProject.getVersion() );
        request.setName( this.mavenProject.getName() );
        request.setDescription( this.mavenProject.getDescription() );
        request.setPluginSiteURL( this.mavenProject.getUrl() );

        request.setApplicationId( this.applicationId );
        request.setApplicationEdition( this.applicationEdition );
        request.setApplicationMinVersion( this.applicationMinVersion );
        request.setApplicationMaxVersion( this.applicationMaxVersion );

        // licenses
        if ( this.mavenProject.getLicenses() != null )
        {
            for ( License mavenLicenseModel : (List<License>) this.mavenProject.getLicenses() )
            {
                request.addLicense( mavenLicenseModel.getName(), mavenLicenseModel.getUrl() );
            }
        }

        // dependencies
        if ( this.mavenProject.getDependencies() != null )
        {
            for ( Dependency mavenDependency : (List<Dependency>) this.mavenProject.getDependencies() )
            {
                if ( mavenDependency.getType().equals( NXPLUGIN_PACKAGING ) )
                {
                    // enforce provided scope?
                    if ( !mavenDependency.getScope().equals( "provided" ) )
                    {
                        throw new MojoFailureException( "Nexus plugin dependency \""
                            + mavenDependency.getManagementKey() + "\" must have the \"provided\" scope!" );
                    }
                    
                    request.addPluginDependency( mavenDependency.getGroupId(), mavenDependency.getArtifactId(),
                                                 mavenDependency.getVersion() );
                }
                else if ( ( mavenDependency.getScope().equals( "compile" ) || mavenDependency.getScope()
                    .equals( "runtime" ) )
                    && ( !mavenDependency.getGroupId().equals( "org.sonatype.nexus" ) ) )
                {
                    request.addClasspathDependency( mavenDependency.getGroupId(), mavenDependency.getArtifactId(),
                                                    mavenDependency.getVersion() );
                }
            }
        }

        request.setOutputFile( this.generatedPluginMetadata );
        request.setClassesDirectory( new File( mavenProject.getBuild().getOutputDirectory() ) );
        try
        {
            if ( mavenProject.getCompileClasspathElements() != null )
            {
                for ( String classpathElement : (List<String>) mavenProject.getCompileClasspathElements() )
                {
                    request.getClasspath().add( new File( classpathElement ) );
                }
            }
        }
        catch ( DependencyResolutionRequiredException e )
        {
            throw new MojoFailureException( "Plugin failed to resolve dependencies: " + e.getMessage(), e );
        }

        request.getAnnotationClasses().add( ExtensionPoint.class );
        request.getAnnotationClasses().add( Managed.class );

        // do the work
        try
        {
            this.metadataGenerator.generatePluginDescriptor( request );
        }
        catch ( GleanerException e )
        {
            throw new MojoFailureException( "Failed to generate plugin xml file: " + e.getMessage(), e );
        }
    }
}
