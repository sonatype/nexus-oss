/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;
import org.sonatype.nexus.plugins.capabilities.support.activation.RepositoryConditions;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * {@link RepositoryLocalStatusCondition} UTs.
 *
 * @since 1.10.0
 */
public class RepositoryLocalStatusConditionTest
    extends NexusEventBusTestSupport
{

    static final String TEST_REPOSITORY = "test-repository";

    private RepositoryLocalStatusCondition underTest;

    private Repository repository;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        final RepositoryConditions.RepositoryId repositoryId = mock( RepositoryConditions.RepositoryId.class );
        when( repositoryId.get() ).thenReturn( TEST_REPOSITORY );

        repository = mock( Repository.class );
        when( repository.getId() ).thenReturn( TEST_REPOSITORY );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );

        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );

        underTest = new RepositoryLocalStatusCondition(
            eventBus, repositoryRegistry, LocalStatus.IN_SERVICE, repositoryId
        );
        underTest.bind();

        verify( eventBus ).register( underTest );

        assertThat( underTest.isSatisfied(), is( false ) );

        underTest.handle( new RepositoryRegistryEventAdd( repositoryRegistry, repository ) );
    }

    /**
     * Condition should become unsatisfied and notification sent when repository is out of service.
     */
    @Test
    public void repositoryIsInService01()
    {
        assertThat( underTest.isSatisfied(), is( true ) );

        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );
        underTest.handle( new RepositoryConfigurationUpdatedEvent( repository ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * Condition should become satisfied and notification sent when repository is back on service.
     */
    @Test
    public void repositoryIsInService02()
    {
        assertThat( underTest.isSatisfied(), is( true ) );

        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );
        underTest.handle( new RepositoryConfigurationUpdatedEvent( repository ) );

        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        underTest.handle( new RepositoryConfigurationUpdatedEvent( repository ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ), satisfied( underTest ) );
    }

    /**
     * Event bus handler is removed when releasing.
     */
    @Test
    public void releaseRemovesItselfAsHandler()
    {
        underTest.release();

        verify( eventBus ).unregister( underTest );
    }

}
