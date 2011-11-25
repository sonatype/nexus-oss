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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.support.activation.RepositoryConditions;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * {@link RepositoryLocalStatusCondition} UTs.
 *
 * @since 1.10.0
 */
public class RepositoryLocalStatusConditionTest
{

    static final String TEST_REPOSITORY = "test-repository";

    private ActivationContext activationContext;

    private RepositoryLocalStatusCondition underTest;

    private RepositoryEventsNotifier.Listener listener;

    private Repository repository;

    private RepositoryRegistry repositoryRegistry;

    private RepositoryEventsNotifier repositoryEventsNotifier;

    private RepositoryConditions.RepositoryId repositoryId;

    @Before
    public void setUp()
        throws Exception
    {
        activationContext = mock( ActivationContext.class );
        repositoryEventsNotifier = mock( RepositoryEventsNotifier.class );
        repositoryId = mock( RepositoryConditions.RepositoryId.class );
        when( repositoryId.get() ).thenReturn( TEST_REPOSITORY );

        repository = mock( Repository.class );
        when( repository.getId() ).thenReturn( TEST_REPOSITORY );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        repositoryRegistry = mock( RepositoryRegistry.class );
        when( repositoryRegistry.getRepository( TEST_REPOSITORY ) ).thenReturn( repository );

        underTest = new RepositoryLocalStatusCondition(
            activationContext, repositoryRegistry, repositoryEventsNotifier, LocalStatus.IN_SERVICE, repositoryId
        );
        underTest.bind();

        ArgumentCaptor<RepositoryEventsNotifier.Listener> listenerCaptor = ArgumentCaptor.forClass(
            RepositoryEventsNotifier.Listener.class
        );

        verify( repositoryEventsNotifier ).addListener( listenerCaptor.capture() );
        listener = listenerCaptor.getValue();
    }

    /**
     * When repository exists on start, condition should be unsatisfied and notification sent.
     */
    @Test
    public void repositoryIsInService01()
    {
        assertThat( underTest.isSatisfied(), is( true ) );
        verify( activationContext ).notifySatisfied( underTest );
    }

    /**
     * When repository does not exist on start, condition should be unsatisfied.
     */
    @Test
    public void repositoryIsInService02()
    {
        when( repositoryId.get() ).thenReturn( "unexistent" );
        underTest = new RepositoryLocalStatusCondition(
            activationContext, repositoryRegistry, repositoryEventsNotifier, LocalStatus.IN_SERVICE, repositoryId
        );
        underTest.bind();
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * When repository is not in service on start, condition should be unsatisfied,
     */
    @Test
    public void repositoryIsInService03()
    {
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );
        underTest = new RepositoryLocalStatusCondition(
            activationContext, repositoryRegistry, repositoryEventsNotifier, LocalStatus.IN_SERVICE, repositoryId
        );
        underTest.bind();
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Condition should become unsatisfied and notification sent when repository is out of service.
     */
    @Test
    public void repositoryIsInService04()
    {
        assertThat( underTest.isSatisfied(), is( true ) );

        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );
        listener.onUpdated( repository );
        assertThat( underTest.isSatisfied(), is( false ) );

        verify( activationContext ).notifyUnsatisfied( underTest );
    }

    /**
     * Condition should become satisfied and notification sent when repository is back on service.
     */
    @Test
    public void repositoryIsInService05()
    {
        assertThat( underTest.isSatisfied(), is( true ) );

        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );
        listener.onUpdated( repository );

        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        listener.onUpdated( repository );
        assertThat( underTest.isSatisfied(), is( true ) );

        // twice as one is the initial notification on start
        verify( activationContext, times( 2 ) ).notifySatisfied( underTest );
    }

}
