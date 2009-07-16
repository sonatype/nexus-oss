package org.sonatype.maven.plugin.nx.descriptor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugin.metadata.PluginMetadataGenerationRequest;
import org.sonatype.plugin.metadata.PluginMetadataGenerator;
import org.sonatype.plugin.metadata.gleaner.GleanerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Generates a plugin's <tt>plugin.xml</tt> descriptor file based on the project's pom and class annotations.
 * 
 * @goal generate-metadata
 * @phase process-classes
 * @requiresDependencyResolution test
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

    /**
     * The temporary working directory for storing classpath artifacts that should be bundled with the plugin.
     * 
     * @parameter default-value="${project.build.directory}/bundle-classpath"
     */
    private File classpathWorkdir;

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
        List<Artifact> artifacts = mavenProject.getTestArtifacts();
        Set<Artifact> classpathArtifacts = new HashSet<Artifact>();
        if ( artifacts != null )
        {
            Set<String> excludedArtifactIds = new HashSet<String>();

            artifactLoop: for ( Artifact artifact : artifacts )
            {
                GAVCoordinate artifactCoordinate =
                    new GAVCoordinate( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact
                        .getClassifier(), artifact.getType() );

                if ( artifact.getType().equals( NXPLUGIN_PACKAGING ) )
                {
                    if ( !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
                    {
                        throw new MojoFailureException( "Nexus plugin dependency \""
                            + artifact.getDependencyConflictId() + "\" must have the \"provided\" scope!" );
                    }

                    excludedArtifactIds.add( artifact.getId() );

                    request.addPluginDependency( artifactCoordinate );
                }
                else if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() )
                    || Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
                {
                    excludedArtifactIds.add( artifact.getId() );
                }
                else if ( ( Artifact.SCOPE_COMPILE.equals( artifact.getScope() ) || Artifact.SCOPE_RUNTIME
                    .equals( artifact.getScope() ) )
                    && ( !artifact.getGroupId().equals( "org.sonatype.nexus" ) ) )
                {
                    if ( artifact.getDependencyTrail() != null )
                    {
                        for ( String trailId : (List<String>) artifact.getDependencyTrail() )
                        {
                            if ( excludedArtifactIds.contains( trailId ) )
                            {
                                getLog()
                                    .debug(
                                            "Dependency artifact: "
                                                + artifact.getId()
                                                + " is part of the transitive dependency set for a dependency with 'provided' or 'test' scope: "
                                                + trailId
                                                + "\nThis artifact will be excluded from the plugin classpath." );
                                continue artifactLoop;
                            }
                        }
                    }

                    request.addClasspathDependency( artifactCoordinate );
                    classpathArtifacts.add( artifact );
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

        for ( Artifact artifact : classpathArtifacts )
        {
            File artifactFile = artifact.getFile();

            try
            {
                FileUtils.copyFile( artifactFile, new File( classpathWorkdir, artifactFile.getName() ) );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Failed to copy classpath artifact: " + artifactFile
                    + "\nto working directory: " + classpathWorkdir + "\nReason: " + e.getMessage(), e );
            }
        }
    }
}
