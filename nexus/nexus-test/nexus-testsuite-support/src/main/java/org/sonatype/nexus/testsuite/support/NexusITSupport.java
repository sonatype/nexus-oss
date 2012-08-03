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

import static org.sonatype.nexus.testsuite.support.filters.TestProjectFilter.TEST_PROJECT_POM_FILE;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.sonatype.nexus.bundle.launcher.support.NexusBundleResolver;
import org.sonatype.nexus.bundle.launcher.support.NexusSpecific;
import org.sonatype.nexus.testsuite.support.filters.CompositeFilter;
import org.sonatype.nexus.testsuite.support.filters.ImplicitVersionFilter;
import org.sonatype.sisu.bl.support.resolver.BundleResolver;
import org.sonatype.sisu.bl.support.resolver.MavenBridgedBundleResolver;
import org.sonatype.sisu.bl.support.resolver.TargetDirectoryResolver;
import org.sonatype.sisu.litmus.testsupport.inject.InjectedTestSupport;
import org.sonatype.sisu.maven.bridge.MavenArtifactResolver;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
     * List of filters used to filter coordinates.
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
     * Test specific file resolver utility.
     * Lazy initialized on first usage.
     */
    private NexusITFileResolver testFileResolver;

    /**
     * Transformer used to transform coordinates.
     * Lazy initialized on first usage.
     */
    private Filter filter;

    /**
     * Nexus bundle coordinates to run the IT against. If null, it will look up the coordinates from
     * "injected-test.properties".
     */
    protected final String nexusBundleCoordinates;

    /**
     * Transformed Nexus bundle coordinates to run the IT against. If null, it will look up the coordinates from
     * "injected-test.properties".
     */
    protected String filteredNexusBundleCoordinates;

    @Rule
    public TestIndex testIndex = new TestIndex( util.resolveFile( "target/its" ) );

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
                    return testIndex.getDirectory();
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
            // TODO create this map only once?
            final Map<String, String> context = Maps.newHashMap();
            fillContext( context );

            filteredNexusBundleCoordinates = filter().filter( context, nexusBundleCoordinates );

            logger.info(
                "TEST {} is running against Nexus bundle {}",
                testName.getMethodName(), filteredNexusBundleCoordinates
            );

            testIndex.recordInfo( "bundle", filteredNexusBundleCoordinates );
        }
        else
        {
            logger.info(
                "TEST {} is running against a Nexus bundle resolved from injected-test.properties",
                testName.getMethodName()
            );
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
     * Lazy initializes IT specific file resolver.
     *
     * @return IT specific artifact file. Never null.
     */
    public NexusITFileResolver fileResolver()
    {
        if ( testFileResolver == null )
        {
            testFileResolver = new NexusITFileResolver(
                util.getBaseDir(), util.getTargetDir(), getClass(), testName.getMethodName()
            );
        }
        return testFileResolver;
    }

    /**
     * Lazy initializes IT specific filter.
     *
     * @return IT specific filter. Never null.
     */
    public Filter filter()
    {
        if ( filter == null )
        {
            final List<Filter> memberFilters = Lists.newArrayList();
            memberFilters.add( new ImplicitVersionFilter() );
            memberFilters.addAll( filters );
            filter = new CompositeFilter( memberFilters );
        }
        return filter;
    }

    /**
     * Fills teh context with test related mappings.
     *
     * @param context to fill up
     */
    public void fillContext( final Map<String, String> context )
    {
        context.put( TEST_PROJECT_POM_FILE, util.resolveFile( "pom.xml" ).getAbsolutePath() );
    }

}
