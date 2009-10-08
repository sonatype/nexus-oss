package org.sonatype.plugin.nexus.testenvironment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.shade.resource.ComponentsXmlResourceTransformer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

/**
 * @author velo
 * @goal unpackage
 * @requiresDependencyResolution test
 * @phase initialize
 */
public class UnpackTestsMojo
    extends AbstractEnvironmentMojo
{

    @SuppressWarnings( "unchecked" )
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        testOutputDirectory.mkdirs();
        resourcesSourceLocation.mkdirs();
        testResourcesDirectory.mkdirs();

        Resource resource = new Resource();
        resource.setDirectory( testResourcesDirectory.getAbsolutePath() );
        resource.addInclude( "*" );
        resource.setTargetPath( testOutputDirectory.getAbsolutePath() );
        project.addResource( resource );

        Collection<Artifact> plugins = getNexusPlugins();

        Set<Artifact> classpath = new LinkedHashSet<Artifact>();

        if ( project.getDependencyArtifacts() != null )
        {
            classpath.addAll( project.getDependencyArtifacts() );
        }

        ComponentsXmlResourceTransformer plxXml = new ComponentsXmlResourceTransformer();
        ComponentsXmlResourceTransformer compXml = new ComponentsXmlResourceTransformer();
        Properties baseProps = new Properties();

        for ( Artifact artifact : plugins )
        {
            Artifact jar =
                artifactFactory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                                "test", "jar" );

            try
            {
                classpath.add( resolve( jar ) );
            }
            catch ( MojoExecutionException e )
            {
                // ignore for now
            }

            Artifact testResources =
                artifactFactory.createArtifactWithClassifier( artifact.getGroupId(), artifact.getArtifactId(),
                                                              artifact.getVersion(), "zip", "test-resources" );

            try
            {
                resolve( testResources );
            }
            catch ( MojoExecutionException e )
            {
                continue;
            }

            File zip = new File( testResources.getFile() );

            new File( zip, "classes" ).copyAllTo( new File( testOutputDirectory ) );
            new File( zip, "test-resources" ).copyAllTo( new File( testResourcesDirectory ) );
            new File( zip, "resources" ).copyAllTo( new File( resourcesSourceLocation ) );

            try
            {
                File plexusXml = new File( zip, "test-resources/META-INF/plexus/components.xml" );
                if ( plexusXml.exists() )
                {
                    FileInputStream is = new FileInputStream( plexusXml );
                    plxXml.processResource( is );
                    IOUtil.close( is );
                }
                File componentsXml = new File( zip, "test-resources/components.xml" );
                if ( componentsXml.exists() )
                {
                    FileInputStream is = new FileInputStream( componentsXml );
                    compXml.processResource( is );
                    IOUtil.close( is );
                }
                File testProperties = new File( zip, "test-resources/baseTest.properties" );
                if ( testProperties.exists() )
                {
                    FileInputStream is = new FileInputStream( testProperties );
                    baseProps.load( is );
                    IOUtil.close( is );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            MavenProject model;
            try
            {
                model = mavenProjectBuilder.buildFromRepository( artifact, remoteRepositories, localRepository );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            List<Dependency> deps = model.getDependencies();
            for ( Dependency dependency : deps )
            {
                if ( Artifact.SCOPE_TEST.equals( dependency.getScope() ) )
                {
                    Artifact dep =
                        artifactFactory.createArtifactWithClassifier( dependency.getGroupId(),
                                                                      dependency.getArtifactId(),
                                                                      dependency.getVersion(), dependency.getType(),
                                                                      dependency.getClassifier() );
                    dep.setScope( Artifact.SCOPE_TEST );

                    classpath.add( resolve( dep ) );
                }
            }
        }

        try
        {
            FileUtils.copyFile( plxXml.getTransformedResource(), new File( testResourcesDirectory,
                                                                           "META-INF/plexus/components.xml" ) );
            FileUtils.copyFile( compXml.getTransformedResource(), new File( testResourcesDirectory, "components.xml" ) );
            OutputStream out = new FileOutputStream( new File( testResourcesDirectory, "baseTest.properties" ) );
            baseProps.store( out, null );
            IOUtil.close( out );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        project.setDependencyArtifacts( classpath );
    }

}
