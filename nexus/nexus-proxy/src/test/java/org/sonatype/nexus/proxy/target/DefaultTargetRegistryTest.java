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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Simple DefaultTargetRegistry creation test
 */
public class DefaultTargetRegistryTest
    extends AbstractDefaultTargetRegistryTest
{
    @Test
    public void testSimpleM2()
    {
        // create a dummy
        Repository repository = createMock( Repository.class );
        expect( repository.getRepositoryContentClass() ).andReturn( maven2 ).anyTimes();
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        replay( repository );

        TargetSet ts =
            targetRegistry.getTargetsForRepositoryPath( repository,
                "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom" );

        assertThat( ts, notNullValue() );
        assertThat( ts.getMatches().size(), equalTo( 2 ) );
        assertThat( ts.getMatchedRepositoryIds().size(), equalTo( 1 ) );
        assertThat( ts.getMatchedRepositoryIds().iterator().next(), equalTo( "dummy" ) );

        TargetSet ts1 =
            targetRegistry.getTargetsForRepositoryPath( repository,
                "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9-sources.jar" );

        assertThat( ts1, notNullValue() );
        assertThat( ts1.getMatches().size(), equalTo( 1 ) );
        assertThat( ts1.getMatches().iterator().next().getTarget().getId(), equalTo( "maven2-with-sources" ) );

        // adding them
        ts.addTargetSet( ts1 );

        assertThat( ts, notNullValue() );
        assertThat( ts.getMatches().size(), equalTo( 2 ) );
        assertThat( ts.getMatchedRepositoryIds().size(), equalTo( 1 ) );
    }

    @Test
    public void testSimpleM1()
    {
        // create a dummy
        Repository repository = createMock( Repository.class );
        expect( repository.getRepositoryContentClass() ).andReturn( maven1 ).anyTimes();
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        replay( repository );

        TargetSet ts =
            targetRegistry.getTargetsForRepositoryPath( repository, "/org.apache.maven/jars/maven-model-v3-2.0.jar" );

        assertThat( ts, notNullValue() );
        assertThat( ts.getMatches().size(), equalTo( 1 ) );

        ts =
            targetRegistry.getTargetsForRepositoryPath( repository,
                "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9-sources.jar" );

        assertThat( ts, notNullValue() );
        assertThat( ts.getMatches().size(), equalTo( 0 ) );
    }

}