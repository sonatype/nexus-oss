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
package org.sonatype.plugin.nexus.testenvironment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.shade.resource.ComponentsXmlResourceTransformer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.InputStreamFacade;

/**
 * @author velo
 * @goal unpackage
 * @requiresDependencyResolution test
 * @phase initialize
 */
public class UnpackTestsMojo
    extends AbstractEnvironmentMojo
{

    /**
     * @parameter
     */
    private MavenArtifact[] extraTestSuites;

    @SuppressWarnings( "unchecked" )
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( testSkip || pluginSkip )
        {
            return;
        }

        testOutputDirectory.mkdirs();
        resourcesSourceLocation.mkdirs();
        testResourcesDirectory.mkdirs();

        Resource resource = new Resource();
        resource.setDirectory( testResourcesDirectory.getAbsolutePath() );
        resource.addInclude( "*" );
        resource.setTargetPath( testOutputDirectory.getAbsolutePath() );
        project.addResource( resource );

        Collection<Artifact> plugins = new LinkedHashSet<Artifact>();
        plugins.addAll( getNexusPlugins() );
        plugins.addAll( getExtraTestSuites() );

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

            ZipFile zip = null;
            try
            {
                zip = new ZipFile( testResources.getFile() );

                copyAllTo( zip, "classes/", testOutputDirectory );
                copyAllTo( zip, "test-resources/", testResourcesDirectory );
                copyAllTo( zip, "resources/", resourcesSourceLocation );

                ZipEntry plexusXml = zip.getEntry( "test-resources/META-INF/plexus/components.xml" );
                if ( plexusXml != null )
                {
                    InputStream is = zip.getInputStream( plexusXml );
                    plxXml.processResource( is );
                    IOUtil.close( is );
                }
                ZipEntry componentsXml = zip.getEntry( "test-resources/components.xml" );
                if ( componentsXml != null )
                {
                    InputStream is = zip.getInputStream( componentsXml );
                    compXml.processResource( is );
                    IOUtil.close( is );
                }
                ZipEntry testProperties = zip.getEntry( "test-resources/baseTest.properties" );
                if ( testProperties != null )
                {
                    InputStream is = zip.getInputStream( testProperties );
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
            plxXml.processResource( getClass().getResourceAsStream( "/default-config/components.xml" ) );
            compXml.processResource( getClass().getResourceAsStream( "/default-config/components.xml" ) );

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

    private void copyAllTo( ZipFile zip, String baseEntry, java.io.File testOutputDirectory )
        throws IOException
    {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while ( entries.hasMoreElements() )
        {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if ( !name.startsWith( baseEntry ) )
            {
                continue;
            }

            name = name.replace( baseEntry, "" );

            File dest = new File( testOutputDirectory, name );
            if ( entry.isDirectory() )
            {
                dest.mkdirs();
            }
            else
            {
                final InputStream in = zip.getInputStream( entry );
                FileUtils.copyStreamToFile( new InputStreamFacade()
                {
                    public InputStream getInputStream()
                        throws IOException
                    {
                        return in;
                    }
                }, dest );
                IOUtil.close( in );
            }
        }
    }

    private Collection<? extends Artifact> getExtraTestSuites()
        throws MojoExecutionException, MojoFailureException
    {
        if ( extraTestSuites == null )
        {
            return Collections.emptyList();
        }
        Collection<Artifact> extra = new LinkedHashSet<Artifact>();

        for ( MavenArtifact ma : extraTestSuites )
        {
            ma.setClassifier( "test-resources" );
            ma.setType( "zip" );

            extra.add( getMavenArtifact( ma ) );
        }

        return extra;
    }

}
