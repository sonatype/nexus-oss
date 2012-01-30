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

import com.google.inject.Binder;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.nexus.bundle.launcher.support.NexusSpecific;
import org.sonatype.sisu.bl.support.resolver.TargetDirectoryResolver;
import org.sonatype.sisu.litmus.testsupport.inject.InjectedTestSupport;
import org.sonatype.sisu.maven.bridge.MavenArtifactResolver;

import javax.inject.Inject;
import java.io.File;

import static org.sonatype.sisu.maven.bridge.support.ArtifactRequestBuilder.request;

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
    private MavenArtifactResolver artifactResolver;

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
                        getClass().getCanonicalName().replace( ".", "/" )
                    ),
                    testName.getMethodName()
                ),
                path
            );
    }

}
