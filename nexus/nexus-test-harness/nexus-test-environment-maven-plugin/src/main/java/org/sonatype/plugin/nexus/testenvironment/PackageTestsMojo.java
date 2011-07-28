/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

    /**
     * @parameter expression="${maven.test.skip}"
     */
    private boolean testSkip;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( testSkip )
        {
            return;
        }

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

            archiver.addFile( project.getFile(), "pom.xml" );

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
