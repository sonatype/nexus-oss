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
package org.sonatype.nexus.testsuite.support;

import static org.sonatype.nexus.testsuite.support.NexusITFilter.contextEntry;
import static org.sonatype.nexus.testsuite.support.filters.TestProjectFilter.TEST_PROJECT_POM_FILE;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.sonatype.nexus.bundle.launcher.support.NexusBundleResolver;
import org.sonatype.nexus.bundle.launcher.support.NexusSpecific;
import org.sonatype.sisu.bl.support.resolver.BundleResolver;
import org.sonatype.sisu.bl.support.resolver.MavenBridgedBundleResolver;
import org.sonatype.sisu.bl.support.resolver.TargetDirectoryResolver;
import org.sonatype.sisu.litmus.testsupport.TestData;
import org.sonatype.sisu.litmus.testsupport.TestIndex;
import org.sonatype.sisu.litmus.testsupport.inject.InjectedTestSupport;
import org.sonatype.sisu.litmus.testsupport.junit.TestDataRule;
import org.sonatype.sisu.litmus.testsupport.junit.TestIndexRule;
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
     * Artifact resolver used to resolve artifacts by Maven coordinates.
     * Never null.
     */
    @Inject
    @Named( "remote-artifact-resolver-using-settings" )
    private MavenArtifactResolver artifactResolver;

    /**
     * Model resolver used to resolve effective Maven models.
     * Never null.
     */
    @Inject
    @Named( "remote-model-resolver-using-settings" )
    private MavenModelResolver modelResolver;

    @Inject
    private NexusBundleResolver nexusBundleResolver;

    /**
     * List of available filters.
     * Never null.
     */
    @Inject
    private List<Filter> filters;

    /**
     * Test specific artifact resolver utility.
     * Lazy initialized on first usage.
     */
    private NexusITArtifactResolver testArtifactResolver;

    /**
     * Filter used to filter coordinates.
     * Lazy initialized on first usage.
     */
    private NexusITFilter filter;

    /**
     * Nexus bundle coordinates to run the IT against. If null, it will look up the coordinates from
     * "injected-test.properties".
     */
    protected final String nexusBundleCoordinates;

    /**
     * Filtered Nexus bundle coordinates to run the IT against. If null, it will look up the coordinates from
     * "injected-test.properties".
     */
    protected String filteredNexusBundleCoordinates;

    /**
     * Test index.
     * Never null.
     */
    @Rule
    public TestIndexRule testIndex = new TestIndexRule( util.resolveFile( "target/its" ) );

    /**
     * Test data.
     * Never null.
     */
    @Rule
    public TestDataRule testData = new TestDataRule( util.resolveFile( "src/test/it-resources" ) );

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
    public NexusITSupport( @Nullable final String nexusBundleCoordinates )
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
                    return testIndex().getDirectory();
                }

            } );
        binder.bind( BundleResolver.class ).annotatedWith( NexusSpecific.class ).toInstance(
            new BundleResolver()
            {
                @Override
                public File resolve()
                {
                    final BundleResolver resolver;
                    if ( filteredNexusBundleCoordinates == null )
                    {
                        resolver = nexusBundleResolver;
                    }
                    else
                    {
                        resolver = new MavenBridgedBundleResolver( filteredNexusBundleCoordinates, artifactResolver );
                    }
                    return resolver.resolve();
                }
            }
        );
    }

    /**
     * Filters nexus bundle coordinates, if present (not null).
     */
    @Before
    public void filterNexusBundleCoordinates()
    {
        if ( nexusBundleCoordinates != null )
        {
            filteredNexusBundleCoordinates = filter().filter( nexusBundleCoordinates );

            logger.info(
                "TEST {} is running against Nexus bundle {}",
                testName.getMethodName(), filteredNexusBundleCoordinates
            );

            testIndex().recordInfo( "bundle", filteredNexusBundleCoordinates );
        }
        else
        {
            logger.info(
                "TEST {} is running against a Nexus bundle resolved from injected-test.properties",
                testName.getMethodName()
            );
            testIndex().recordLink( "bundle", "../test-classes/injected-test.properties" );
        }
    }

    @After
    public void recordSurefireAndFailsafeInfo()
    {
        {
            final String name = "target/failsafe-reports/" + getClass().getName();
            testIndex().recordLink( "failsafe result", util.resolveFile( name + ".txt" ) );
            testIndex().recordLink( "failsafe output", util.resolveFile( name + "-output.txt" ) );
        }
        {
            final String name = "target/surefire-reports/" + getClass().getName();
            testIndex().recordLink( "surefire result", util.resolveFile( name + ".txt" ) );
            testIndex().recordLink( "surefire output", util.resolveFile( name + "-output.txt" ) );
        }
    }

    /**
     * Lazy initializes IT specific artifact resolver.
     *
     * @return IT specific artifact resolver. Never null.
     */
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

    /**
     * Returns test data accessor.
     *
     * @return test data accessor. Never null.
     */
    public TestData testData()
    {
        return testData;
    }

    /**
     * Returns test index.
     *
     * @return test index. Never null.
     */
    public TestIndex testIndex()
    {
        return testIndex;
    }

    /**
     * Lazy initializes IT specific filter.
     *
     * @return IT specific filter. Never null.
     */
    public NexusITFilter filter()
    {
        if ( filter == null )
        {
            filter = new NexusITFilter(
                filters,
                contextEntry( TEST_PROJECT_POM_FILE, util.resolveFile( "pom.xml" ).getAbsolutePath() )
            );
        }
        return filter;
    }

}
