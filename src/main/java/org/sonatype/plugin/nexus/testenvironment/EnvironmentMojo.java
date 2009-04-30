package org.sonatype.plugin.nexus.testenvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugins.portallocator.Port;
import org.sonatype.plugins.portallocator.PortAllocatorMojo;

/**
 * @author velo
 * @goal setup-environment
 * @requiresDependencyResolution test
 * @phase generate-test-resources
 */
public class EnvironmentMojo
    extends AbstractMojo
    implements Contextualizable
{

    /** @component */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    /** @component */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /** @parameter expression="${localRepository}" */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    /** @parameter expression="${project.remoteArtifactRepositories}" */
    private java.util.List<?> remoteRepositories;

    /** @component */
    private MavenFileFilter mavenFileFilter;

    /**
     * @parameter expression="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;

    private PlexusContainer plexus;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Where nexus instance should be extracted
     *
     * @parameter default-value="${project.build.directory}/nexus"
     * @required
     */
    private File destination;

    /**
     * Artifact file containing nexus bundle
     *
     * @parameter
     */
    private MavenArtifact nexusBundleArtifact;

    /**
     * Emma used on ITs
     *
     * @parameter
     */
    private MavenArtifact emmaArtifact;

    /**
     * Artifact file containing nexus bundle
     *
     * @parameter
     */
    private MavenArtifact[] nexusPluginsArtifacts;

    /**
     * Artifact file containing nexus bundle
     *
     * @parameter
     */
    private MavenArtifact[] extraResourcesArtifacts;

    /**
     * When true setup a maven instance
     *
     * @parameter default-value="true"
     */
    private boolean setupMaven;

    /**
     * Maven used on ITs
     *
     * @parameter
     * @see EnvironmentMojo#setupMaven
     */
    private MavenArtifact mavenArtifact;

    /**
     * Where maven instace should be created
     *
     * @parameter default-value="${project.build.directory}/maven"
     * @see EnvironmentMojo#setupMaven
     */
    private File mavenLocation;

    /**
     * @parameter default-value="${basedir}/resources"
     */
    private File resourcesSourceLocation;

    /**
     * @parameter default-value="${project.build.directory}/resources"
     */
    private File resourcesDestinationLocation;

    /**
     * @parameter expression="${maven.test.skip}"
     */
    private boolean testSkip;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( testSkip )
        {
            return;
        }

        init();

        allocatePorts();

        project.getProperties().put( "jetty-application-host", "0.0.0.0" );
        project.getProperties().put(
                                     "nexus-base-url",
                                     "http://localhost:"
                                         + project.getProperties().getProperty( "nexus-application-port" ) + "/nexus/" );
        project.getProperties().put(
                                     "proxy-repo-base-url",
                                     "http://localhost:" + project.getProperties().getProperty( "proxy-repo-port" )
                                         + "/remote/" );
        project.getProperties().put( "proxy-repo-base-dir", getPath( new File( destination, "proxy-repo" ) ) );
        project.getProperties().put( "proxy-repo-target-dir", getPath( new File( destination, "proxy-repo" ) ) );

        Artifact bundle = getMavenArtifact( nexusBundleArtifact );

        unpack( bundle.getFile(), destination, bundle.getType() );

        File nexusBaseDir = new File( destination, bundle.getArtifactId() + "-" + bundle.getBaseVersion() );
        project.getProperties().put( "nexus-base-dir", getPath( nexusBaseDir ) );
        project.getProperties().put( "nexus-work-dir", getPath( new File( destination, "nexus-work-dir" ) ) );

        copyUrl( "/default-config/plexus.properties", new File( nexusBaseDir, "conf/plexus.properties" ) );
        project.getProperties().put( "nexus-plexus-config-file", getPath( new File( nexusBaseDir, "conf/plexus.xml" ) ) );

        File pluginFolder = new File( nexusBaseDir, "runtime/apps/nexus/lib" );
        copyEmma( pluginFolder );

        if ( nexusPluginsArtifacts != null )
        {
            setupPlugins( nexusBaseDir, nexusPluginsArtifacts, pluginFolder );
        }

        if ( setupMaven )
        {
            String mavenVersion = setupMaven().getBaseVersion();
            project.getProperties().put( "maven-version", mavenVersion );
            project.getProperties().put( "maven-basedir",
                                         getPath( new File( mavenLocation, "apache-maven-" + mavenVersion ) ) );
        }

        if ( resourcesSourceLocation.isDirectory() )
        {
            project.getProperties().put( "test-resources-source-folder", getPath( resourcesSourceLocation ) );
            project.getProperties().put( "test-resources-folder", getPath( resourcesDestinationLocation ) );

            File fakeRepository = new File( resourcesSourceLocation, "fake-central" );
            File fakeRepoDest = new File( mavenLocation, "fake-repo" );
            project.getProperties().put( "maven-repository", getPath( fakeRepoDest ) );
            if ( fakeRepository.isDirectory() )
            {
                copyDirectory( fakeRepository, fakeRepoDest );
            }

            File defaultConfig = new File( resourcesDestinationLocation, "default-configs" );
            project.getProperties().put( "default-configs", getPath( defaultConfig ) );

            copyUrl( "/default-config/nexus.xml", new File( defaultConfig, "nexus.xml" ) );
            copyUrl( "/default-config/security.xml", new File( defaultConfig, "security.xml" ) );
            copyUrl( "/default-config/settings.xml", new File( defaultConfig, "settings.xml" ) );
            copyUrl( "/default-config/log4j.properties", new File( defaultConfig, "log4j.properties" ) );

            File sourceDefaultConfig = new File( resourcesSourceLocation, "default-config" );
            if ( sourceDefaultConfig.isDirectory() )
            {
                copyAndInterpolate( sourceDefaultConfig, defaultConfig );
            }

            File baseTestProperties = new File( project.getBuild().getTestOutputDirectory(), "baseTest.properties" );
            copyUrl( "/default-config/baseTest.properties", baseTestProperties );

            File testSuiteProperties = new File( resourcesSourceLocation, "baseTest.properties" );
            if ( testSuiteProperties.isFile() )
            {
                merge( baseTestProperties, testSuiteProperties, "properties" );
            }

            addProjectProperties( baseTestProperties );

        }

        if ( extraResourcesArtifacts != null )
        {
            copyExtraResources();
        }

        File destinationComponents =
            new File( project.getBuild().getTestOutputDirectory(), "META-INF/plexus/components.xml" );
        copyUrl( "/default-config/components.xml", destinationComponents );

        File componentsXml = new File( project.getBasedir(), "src/test/resources/components.xml" );
        if ( componentsXml.exists() )
        {
            copyAndInterpolate( componentsXml.getParentFile(), destinationComponents.getParentFile() );
        }
    }

    private void addProjectProperties( File baseTestProperties )
        throws MojoExecutionException
    {
        InputStream input = null;
        OutputStream output = null;
        try
        {
            input = new FileInputStream( baseTestProperties );

            Properties original = new Properties();
            original.load( input );
            IOUtil.close( input );

            original.putAll( this.project.getProperties() );

            output = new FileOutputStream( baseTestProperties );

            original.store( output, "Updated by EnvironmentMojo" );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException(
                                              "Error adding properties '" + baseTestProperties.getAbsolutePath() + "'.",
                                              e );
        }
        finally
        {
            IOUtil.close( input );
            IOUtil.close( output );
        }
    }

    private void allocatePorts()
        throws MojoExecutionException, MojoFailureException
    {
        List<Port> portsList = new ArrayList<Port>();
        portsList.add( new Port( "proxy-repo-port" ) );
        portsList.add( new Port( "proxy-repo-control-port" ) );
        portsList.add( new Port( "nexus-application-port" ) );
        portsList.add( new Port( "nexus-control-port" ) );
        portsList.add( new Port( "email-server-port" ) );
        portsList.add( new Port( "webproxy-server-port" ) );

        PortAllocatorMojo portAllocator = new PortAllocatorMojo();
        portAllocator.setProject( project );
        portAllocator.setLog( getLog() );
        portAllocator.setPorts( portsList.toArray( new Port[0] ) );
        portAllocator.execute();
    }

    private void copyExtraResources()
        throws MojoExecutionException, MojoFailureException
    {
        for ( MavenArtifact extraResource : extraResourcesArtifacts )
        {
            Artifact artifact = getMavenArtifact( extraResource );

            File destination;
            if ( extraResource.getOutputDirectory() != null )
            {
                destination = extraResource.getOutputDirectory();
            }
            else if ( extraResource.getOutputProperty() != null )
            {
                destination = new File( project.getProperties().getProperty( extraResource.getOutputProperty() ) );
            }
            else
            {
                destination = resourcesDestinationLocation;
            }
            unpack( artifact.getFile(), destination, artifact.getType() );
        }
    }

    private void merge( File originalFile, File extraContentFile, String type )
        throws MojoFailureException, MojoExecutionException
    {
        InputStream originalReader = null;
        InputStream extraContentReader = null;
        OutputStream originalWriter = null;
        try
        {
            String name = FileUtils.removeExtension( extraContentFile.getName() );
            String extension = FileUtils.getExtension( extraContentFile.getName() );

            if ( "properties".equals( type ) )
            {
                File tempFile = File.createTempFile( name, extension );
                mavenFileFilter.copyFile( extraContentFile, tempFile, true, project, null, true, "UTF-8", session );

                originalReader = new FileInputStream( originalFile );
                extraContentReader = new FileInputStream( tempFile );

                Properties original = new Properties();
                original.load( originalReader );
                IOUtil.close( originalReader );

                originalWriter = new FileOutputStream( originalFile );

                Properties extra = new Properties();
                extra.load( extraContentReader );
                IOUtil.close( extraContentReader );

                for ( Object key : extra.keySet() )
                {
                    original.put( key, extra.get( key ) );
                }

                original.store( originalWriter, "Updated by EnvironmentMojo" );
            }
            else
            {
                throw new MojoFailureException( "Invalid file type: " + type );
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error merging files: Original '" + originalFile.getAbsolutePath()
                + "', extraContent '" + extraContentFile.getAbsolutePath() + "'.", e );
        }
        finally
        {
            IOUtil.close( originalReader );
            IOUtil.close( extraContentReader );
            IOUtil.close( originalWriter );
        }
    }

    private void copyUrl( String sourceUrl, File destinationFile )
        throws MojoExecutionException
    {
        getLog().debug( "Copying url '" + sourceUrl + "'" );

        String name = FileUtils.removeExtension( FileUtils.removePath( sourceUrl, '/' ) );
        String extension = FileUtils.getExtension( sourceUrl );

        try
        {
            destinationFile.getParentFile().mkdirs();
            destinationFile.createNewFile();

            File tempFile = File.createTempFile( name, extension );
            FileUtils.copyURLToFile( getClass().getResource( sourceUrl ), tempFile );
            mavenFileFilter.copyFile( tempFile, destinationFile, true, project, null, true, "UTF-8", session );
            tempFile.delete();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to copy resouce " + sourceUrl + name + extension, e );
        }
    }

    private void copyDirectory( File sourceDir, File destinationDir )
        throws MojoExecutionException
    {
        destinationDir.mkdirs();

        getLog().debug( "Copying dir '" + sourceDir + "'" );
        try
        {
            FileUtils.copyDirectoryStructure( sourceDir, destinationDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy dir " + sourceDir, e );
        }
    }

    private void copyAndInterpolate( File sourceDir, File destinationDir )
        throws MojoExecutionException
    {
        destinationDir.mkdirs();

        getLog().debug( "Copying and interpolating dir '" + sourceDir + "'" );
        try
        {
            DirectoryScanner scanner = new DirectoryScanner();

            scanner.setBasedir( sourceDir );
            scanner.addDefaultExcludes();
            scanner.scan();

            String[] files = scanner.getIncludedFiles();
            for ( String file : files )
            {
                String extension = FileUtils.getExtension( file );

                File source = new File( sourceDir, file );

                File destination = new File( destinationDir, file );
                destination.getParentFile().mkdirs();

                if ( Arrays.asList( "zip", "jar", "tar.gz" ).contains( extension ) )
                {
                    // just copy know binaries
                    FileUtils.copyFile( source, destination );
                }
                else
                {
                    mavenFileFilter.copyFile( source, destination, true, project, null, false, "UTF-8", session );
                }
            }
        }
        catch ( MavenFilteringException e )
        {
            throw new MojoExecutionException( "Failed to copy : " + sourceDir, e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy : " + sourceDir, e );
        }
    }

    private Artifact setupMaven()
        throws MojoExecutionException, MojoFailureException
    {
        Artifact artifact = getMavenArtifact( mavenArtifact );
        unpack( artifact.getFile(), mavenLocation, artifact.getType() );
        return artifact;
    }

    private void init()
    {
        destination.mkdirs();
        mavenLocation.mkdirs();
        resourcesDestinationLocation.mkdirs();

        if ( nexusBundleArtifact == null )
        {
            nexusBundleArtifact = new MavenArtifact( "org.sonatype.nexus", "nexus-webapp", "bundle", "tar.gz" );
        }

        if ( emmaArtifact == null )
        {
            emmaArtifact = new MavenArtifact( "emma", "emma" );
        }

        if ( mavenArtifact == null )
        {
            mavenArtifact = new MavenArtifact( "org.apache.maven", "apache-maven", "bin", "tar.gz" );
        }

    }

    private void copyEmma( File pluginFolder )
        throws MojoExecutionException, MojoFailureException
    {
        Artifact artifact = getMavenArtifact( emmaArtifact );
        copy( artifact.getFile(), pluginFolder );
    }

    private void setupPlugins( File nexusBaseDir, MavenArtifact[] nexusPluginsArtifacts, File pluginsFolder )
        throws MojoFailureException, MojoExecutionException
    {

        for ( MavenArtifact plugin : nexusPluginsArtifacts )
        {
            Artifact pluginArtifact = getMavenArtifact( plugin );

            File destination;
            if ( plugin.getOutputDirectory() != null )
            {
                destination = plugin.getOutputDirectory();
            }
            else if ( plugin.getOutputProperty() != null )
            {
                destination = new File( project.getProperties().getProperty( plugin.getOutputProperty() ) );
            }
            else
            {
                destination = pluginsFolder;
            }

            String type = pluginArtifact.getType();
            if ( "jar".equals( type ) )
            {
                copy( pluginArtifact.getFile(), destination );
            }
            else if ( "zip".equals( type ) || "tar.gz".equals( type ) )
            {
                unpack( pluginArtifact.getFile(), destination, plugin.getType() );
            }
            else
            {
                throw new MojoFailureException( "Invalid plugin type: " + type );
            }
        }
    }

    private void copy( File sourceFile, File destinationDir )
        throws MojoExecutionException
    {
        getLog().debug( "Copying file '" + sourceFile + "'" );

        try
        {
            FileUtils.copyFileToDirectory( sourceFile, destinationDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy : " + sourceFile, e );
        }
    }

    private String getPath( File nexusBaseDir )
    {
        try
        {
            return nexusBaseDir.getCanonicalPath();
        }
        catch ( IOException e )
        {
            return nexusBaseDir.getAbsolutePath();
        }
    }

    private void unpack( File sourceFile, File destDirectory, String type )
        throws MojoExecutionException
    {
        destDirectory.mkdirs();

        UnArchiver unarchiver;
        try
        {
            unarchiver = (UnArchiver) plexus.lookup( UnArchiver.ROLE, type );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Unable to lookup for unarchiver", e );
        }

        unarchiver.setSourceFile( sourceFile );
        unarchiver.setDestDirectory( destDirectory );
        try
        {
            unarchiver.extract();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to unpack " + sourceFile, e );
        }
    }

    @SuppressWarnings( "unchecked" )
    private Artifact getMavenArtifact( MavenArtifact mavenArtifact )
        throws MojoExecutionException, MojoFailureException
    {
        // add filters in well known order, least specific to most specific
        FilterArtifacts filter = new FilterArtifacts();

        filter.addFilter( new TypeFilter( mavenArtifact.getType(), null ) );
        filter.addFilter( new ClassifierFilter( mavenArtifact.getClassifier(), null ) );
        filter.addFilter( new GroupIdFilter( mavenArtifact.getGroupId(), null ) );
        filter.addFilter( new ArtifactIdFilter( mavenArtifact.getArtifactId(), null ) );

        Set<Artifact> projectArtifacts = project.getArtifacts();

        // perform filtering
        try
        {
            projectArtifacts = filter.filter( projectArtifacts );
        }
        catch ( ArtifactFilterException e )
        {
            throw new MojoExecutionException( "Error filtering artifacts", e );
        }

        if ( projectArtifacts.size() == 0 )
        {
            throw new MojoFailureException( "Maven artifact: '" + mavenArtifact.toString()
                + "' not found on dependencies list" );
        }
        else if ( projectArtifacts.size() != 1 )
        {
            throw new MojoFailureException( "More then one artifact found on dependencies list: '"
                + mavenArtifact.toString() + "'" );
        }

        Artifact artifact = projectArtifacts.iterator().next();

        if ( !artifact.isResolved() )
        {
            try
            {
                resolver.resolve( artifact, remoteRepositories, localRepository );
            }
            catch ( AbstractArtifactResolutionException e )
            {
                throw new MojoExecutionException( "Unable to resolve artifact: " + artifact, e );
            }
        }

        return artifact;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        plexus = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
