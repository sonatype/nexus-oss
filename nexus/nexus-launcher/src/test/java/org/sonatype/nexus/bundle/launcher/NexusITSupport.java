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
package org.sonatype.nexus.bundle.launcher;

import static org.sonatype.sisu.maven.bridge.support.ArtifactRequestBuilder.request;
import static org.sonatype.sisu.maven.bridge.support.ModelBuildingRequestBuilder.model;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.nexus.bundle.launcher.support.NexusSpecific;
import org.sonatype.sisu.bl.support.resolver.TargetDirectoryResolver;
import org.sonatype.sisu.litmus.testsupport.inject.InjectedTestSupport;
import org.sonatype.sisu.maven.bridge.MavenArtifactResolver;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;
import com.google.common.base.Throwables;
import com.google.inject.Binder;

/**
 * Base class for Nexus Integration Tests.
 *
 * @since 2.0
 */
public abstract class NexusITSupport
    extends InjectedTestSupport
{

    /**
     * Path in project where IT resources will be searched.
     */
    private static final String SRC_TEST_IT_RESOURCES = "src/test/it-resources";

    /**
     * Artifact resolver used to resolve artifacts by Maven coordinates.
     */
    @Inject
    @Named( "remote-artifact-resolver-using-settings" )
    private MavenArtifactResolver artifactResolver;

    @Inject
    @Named( "remote-model-resolver-using-settings" )
    private MavenModelResolver modelResolver;

    /**
     * Binds a {@link TargetDirectoryResolver} to an implementation that will set the bundle target directory to a
     * directory specific to test method.
     * <p/>
     * Format: {@code <project>/target/its/<test class package>/<test class name>/<test method name>/<path>}
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void configure( final Binder binder )
    {
        TargetDirectoryResolver targetDirectoryResolver = new TargetDirectoryResolver()
        {

            @Override
            public File resolve()
            {
                return methodSpecificDirectory( "bundle" );
            }

        };
        binder.bind( TargetDirectoryResolver.class ).annotatedWith( NexusSpecific.class ).toInstance(
            targetDirectoryResolver );

    }

    /**
     * Resolves a test file by looking up the specified path into test resources.
     * <p/>
     * It searches the following path locations:<br/>
     * {@code <project>/src/test/it-resources/<test class package>/<test class name>/<test method name>/<path>}<br/>
     * {@code <project>/src/test/it-resources/<test class package>/<test class name>/<path>}<br/>
     * {@code <project>/src/test/it-resources/<path>}<br/>
     *
     * @param path path to look up
     * @return found file
     * @throws RuntimeException if path cannot be found in any of above locations
     * @since 2.0
     */
    public File resolveTestFile( final String path )
        throws RuntimeException
    {
        File level1 = testMethodSourceDirectory( path );
        if ( level1.exists() )
        {
            return level1;
        }
        File level2 = testClassSourceDirectory( path );
        if ( level2.exists() )
        {
            return level2;
        }
        File level3 = testSourceDirectory( path );
        if ( level3.exists() )
        {
            return level3;
        }
        throw new RuntimeException(
            "Path " + path + " not found in any of: " + level1 + ", " + level2 + ", " + level3 );
    }

    /**
     * Resolves an artifact given its Maven coordinates.
     *
     * @param coordinates Maven artifact coordinates
     * @return resolved artifact file
     */
    protected File resolveArtifact( final String coordinates )
        throws RuntimeException
    {
        try
        {
            Artifact artifact = artifactResolver.resolveArtifact(
                request().artifact( coordinates )
            );
            if ( artifact == null || artifact.getFile() == null || !artifact.getFile().exists() )
            {
                throw new RuntimeException( String.format( "Artifact %s could not be resolved", coordinates ) );
            }
            return artifact.getFile();
        }
        catch ( ArtifactResolutionException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    /**
     * Resolves a Nexus plugin, given its coordinates, by looking it up in dependency management section of POM in which
     * the test resides.
     *
     * @param groupId    Maven group id of Nexus plugin to be resolved
     * @param artifactId Maven artifact id of Nexus plugin to be resolved
     * @return resolved artifact file
     * @since 2.1
     */
    protected File resolvePluginFromDependencyManagement( final String groupId, final String artifactId )
        throws RuntimeException
    {
        return resolveFromDependencyManagement( groupId, artifactId, "nexus-plugin", "zip", "bundle" );
    }

    /**
     * Resolves a Maven artifact, given its coordinates, by looking it up in dependency management section of POM in
     * which the test resides.
     *
     * @param groupId            Maven group id of artifact to be resolved
     * @param artifactId         Maven artifact id of artifact to be resolved
     * @param type               Maven type of artifact to be resolved. If not specified (null), type is not considered while finding
     *                           the dependency in dependency management
     * @param overrideType       an optional type to be used to override the type specified in dependency management (e.g
     *                           nexus-plugin -> zip)
     * @param overrideClassifier an optional classifier to override the classifier specified in dependency management
     *                           (e.g (not specified) -> bundle)
     * @return resolved artifact file
     * @since 2.1
     */
    private File resolveFromDependencyManagement( final String groupId,
                                                  final String artifactId,
                                                  final String type,
                                                  final String overrideType,
                                                  final String overrideClassifier )
    {
        try
        {
            final File thisProjectPom = util.resolveFile( "pom.xml" );
            final Model model = modelResolver.resolveModel( model().pom( thisProjectPom ) );

            final List<Dependency> dependencies = model.getDependencyManagement().getDependencies();

            for ( Dependency dependency : dependencies )
            {
                if ( !dependency.getGroupId().equalsIgnoreCase( groupId ) )
                {
                    continue;
                }
                if ( !dependency.getArtifactId().equalsIgnoreCase( artifactId ) )
                {
                    continue;
                }
                if ( type != null && !dependency.getType().equals( type ) )
                {
                    continue;
                }

                StringBuilder coordinates = new StringBuilder();
                coordinates.append( dependency.getGroupId() );
                coordinates.append( ":" ).append( dependency.getArtifactId() );

                String rExtension = dependency.getType();
                if ( overrideType != null )
                {
                    rExtension = overrideType;
                }
                if ( rExtension != null )
                {
                    coordinates.append( ":" ).append( rExtension );
                }

                String rClassifier = dependency.getClassifier();
                if ( overrideClassifier != null )
                {
                    rClassifier = overrideClassifier;
                }
                if ( rClassifier != null )
                {
                    coordinates.append( ":" ).append( rClassifier );
                }
                coordinates.append( ":" ).append( dependency.getVersion() );
                return resolveArtifact( coordinates.toString() );
            }
            throw new RuntimeException( String.format( "Dependency %s:%s was not found", groupId, artifactId ) );
        }
        catch ( Exception e )
        {
            throw Throwables.propagate( e );
        }
    }

    /**
     * Returns a test source directory specific to running test.
     * <p/>
     * Format: {@code <project>/src/test/it-resources/<path>}
     *
     * @param path path to be appended
     * @return test source directory specific to running test + provided path
     * @since 2.0
     */
    private File testSourceDirectory( String path )
    {
        return
            new File(
                new File(
                    util.getBaseDir(),
                    SRC_TEST_IT_RESOURCES
                ),
                path
            );
    }

    /**
     * Returns a test source directory specific to running test class.
     * <p/>
     * Format: {@code <project>/src/test/it-resources/<test class package>/<test class name>/<path>}
     *
     * @param path path to be appended
     * @return test source directory specific to running test class + provided path
     * @since 2.0
     */
    private File testClassSourceDirectory( String path )
    {
        return
            new File(
                new File(
                    new File(
                        util.getBaseDir(),
                        SRC_TEST_IT_RESOURCES
                    ),
                    getClass().getCanonicalName().replace( ".", "/" )
                ),
                path
            );
    }

    /**
     * Returns a test source directory specific to running test method.
     * <p/>
     * Format: {@code <project>/src/test/it-resources/<test class package>/<test class name>/<test method name>/<path>}
     *
     * @param path path to be appended
     * @return test source directory specific to running test method + provided path
     * @since 2.0
     */
    private File testMethodSourceDirectory( String path )
    {
        return
            new File(
                new File(
                    new File(
                        new File(
                            util.getBaseDir(),
                            SRC_TEST_IT_RESOURCES
                        ),
                        getClass().getCanonicalName().replace( ".", "/" )
                    ),
                    testName.getMethodName()
                ),
                path
            );
    }

    /**
     * Returns a directory specific to running test method.
     * <p/>
     * Format: {@code <project>/target/its/<test class package>/<test class name>/<test method name>/<path>}
     *
     * @param path path to be appended to test method specific directory
     * @return directory specific to running test method + provided path
     */
    protected File methodSpecificDirectory( String path )
    {
        return
            new File(
                new File(
                    new File(
                        new File(
                            util.getTargetDir(),
                            "its"
                        ),
                        getClass().getSimpleName()
                    ),
                    testName.getMethodName()
                ),
                path
            );
    }

}
