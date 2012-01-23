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
package org.sonatype.nexus.proxy.target;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;

import org.junit.Test;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

public class DefaultTargetRegistryTest
    extends AbstractNexusTestCase
{
    protected ApplicationConfiguration applicationConfiguration;

    protected TargetRegistry targetRegistry;

    protected ContentClass maven1;

    protected ContentClass maven2;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        applicationConfiguration = lookup( ApplicationConfiguration.class );

        maven1 = new Maven1ContentClass();

        maven2 = new Maven2ContentClass();

        targetRegistry = lookup( TargetRegistry.class );

        // adding two targets
        Target t1 = new Target( "maven2-public", "Maven2 (public)", maven2, Arrays
            .asList( new String[] { "/org/apache/maven/((?!sources\\.).)*" } ) );

        targetRegistry.addRepositoryTarget( t1 );

        Target t2 = new Target( "maven2-with-sources", "Maven2 sources", maven2, Arrays
            .asList( new String[] { "/org/apache/maven/.*" } ) );

        targetRegistry.addRepositoryTarget( t2 );

        Target t3 = new Target( "maven1", "Maven1", maven1, Arrays.asList( new String[] { "/org\\.apache\\.maven.*" } ) );

        targetRegistry.addRepositoryTarget( t3 );

        applicationConfiguration.saveConfiguration();
    }

    @Test
    public void testSimpleM2()
    {
        // create a dummy
        Repository repository = createMock( Repository.class );
        expect( repository.getRepositoryContentClass() ).andReturn( maven2 ).anyTimes();
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        replay( repository );

        TargetSet ts = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom" );

        assertNotNull( ts );

        assertEquals( 2, ts.getMatches().size() );

        assertEquals( 1, ts.getMatchedRepositoryIds().size() );

        assertEquals( "dummy", ts.getMatchedRepositoryIds().iterator().next() );

        TargetSet ts1 = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9-sources.jar" );

        assertNotNull( ts1 );

        assertEquals( 1, ts1.getMatches().size() );

        assertEquals( "maven2-with-sources", ts1.getMatches().iterator().next().getTarget().getId() );

        // adding them
        ts.addTargetSet( ts1 );

        assertEquals( 2, ts.getMatches().size() );

        assertEquals( 1, ts.getMatchedRepositoryIds().size() );
    }

    @Test
    public void testSimpleM1()
    {
        // create a dummy
        Repository repository = createMock( Repository.class );
        expect( repository.getRepositoryContentClass() ).andReturn( maven1 ).anyTimes();
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        replay( repository );

        TargetSet ts = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org.apache.maven/jars/maven-model-v3-2.0.jar" );

        assertNotNull( ts );

        assertEquals( 1, ts.getMatches().size() );

        ts = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9-sources.jar" );

        assertNotNull( ts );

        assertEquals( 0, ts.getMatches().size() );
    }

}
