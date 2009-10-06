package org.sonatype.plugin.nexus.testenvironment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * @author velo
 * @goal package
 * @phase package
 */
public class PackageTestsMojo
    extends AbstractMojo
{

    /**
     * @component
     */
    private ArchiverManager archiverManager;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${project.build.testOutputDirectory}"
     */
    private File testClasses;

    /**
     * @parameter default-value="${project.testResources}"
     */
    private List<Resource> testResources;

    /**
     * @parameter default-value="${basedir}/resources"
     */
    private File resourcesSourceLocation;

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}-test-resources.zip"
     */
    private File destinationFile;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Archiver archiver;
        try
        {
            archiver = archiverManager.getArchiver( "zip" );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        archiver.setDestFile( destinationFile );
        try
        {
            if ( testClasses.exists() )
            {
                archiver.addDirectory( testClasses, "classes/" );
            }

            if ( resourcesSourceLocation.exists() )
            {
                archiver.addDirectory( resourcesSourceLocation, "resources/" );
            }

            for ( Resource resource : testResources )
            {
                File dir = new File( resource.getDirectory() );
                if ( !dir.exists() )
                {
                    continue;
                }

                String[] includes = (String[]) resource.getIncludes().toArray( new String[0] );
                String[] excludes = (String[]) resource.getExcludes().toArray( new String[0] );

                archiver.addDirectory( dir, "test-resources/", includes, excludes );
            }

            archiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        projectHelper.attachArtifact( project, "zip", "test-resources", destinationFile );
    }

}
