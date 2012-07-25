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

import java.io.File;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.sonatype.nexus.bundle.launcher.support.NexusSpecific;
import org.sonatype.sisu.bl.support.resolver.BundleResolver;
import org.sonatype.sisu.bl.support.resolver.MavenBridgedBundleResolver;
import org.sonatype.sisu.bl.support.resolver.TargetDirectoryResolver;
import org.sonatype.sisu.litmus.testsupport.inject.InjectedTestSupport;
import org.sonatype.sisu.maven.bridge.MavenArtifactResolver;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;
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
     * Test specific artifact resolver utility.
     * Cannot be null.
     */
    private NexusITArtifactResolver testArtifactResolver;

    /**
     * Test specific file resolver utility.
     * Cannot be null.
     */
    private NexusITFileResolver testFileResolver;

    /**
     * Nexus bundle coordinates to run the IT against. If null, it will look up the coordinates from
     * "injected-test.properties".
     */
    protected final String nexusBundleCoordinates;

    /**
     * Runs IT by against Nexus bundle coordinates specified in "injected-test.properties".
     */
    public NexusITSupport()
    {
        this( null );
    }

    /**
     * Runs IT by against specified Nexus bundle coordinates.
     *
     * @param nexusBundleCoordinates nexus bundle coordinates to run the test against. If null, it will look up the
     *                               coordinates from "injected-test.properties".
     * @since 2.2
     */
    public NexusITSupport( final String nexusBundleCoordinates )
    {
        this.nexusBundleCoordinates = nexusBundleCoordinates;
    }

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
        binder.bind( TargetDirectoryResolver.class ).annotatedWith( NexusSpecific.class ).toInstance(
            new TargetDirectoryResolver()
            {

                @Override
                public File resolve()
                {
                    return fileResolver().methodSpecificDirectory( "bundle" );
                }

            } );
        if ( nexusBundleCoordinates != null )
        {
            binder.bind( BundleResolver.class ).annotatedWith( NexusSpecific.class ).toInstance(
                new BundleResolver()
                {
                    @Override
                    public File resolve()
                    {
                        return new MavenBridgedBundleResolver( nexusBundleCoordinates, artifactResolver ).resolve();
                    }
                }
            );
        }
    }

    @Before
    public void logNexusBundleCoordinates()
    {
        logger.info(
            "TEST {} is running against Nexus bundle {}",
            testName.getMethodName(),
            nexusBundleCoordinates == null ? "resolved from injected-test.properties" : nexusBundleCoordinates
        );
    }

    public NexusITArtifactResolver artifactResolver()
    {
        if ( testArtifactResolver == null )
        {
            testArtifactResolver = new NexusITArtifactResolver(
                util.resolveFile( "pom.xml" ), artifactResolver, modelResolver
            );
        }
        return testArtifactResolver;
    }

    public NexusITFileResolver fileResolver()
    {
        if ( testFileResolver == null )
        {
            testFileResolver = new NexusITFileResolver(
                util.getBaseDir(), util.getTargetDir(), testName.getMethodName()
            );
        }
        return testFileResolver;
    }
}
