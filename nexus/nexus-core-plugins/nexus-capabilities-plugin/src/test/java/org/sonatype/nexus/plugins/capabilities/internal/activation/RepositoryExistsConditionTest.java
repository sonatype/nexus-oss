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
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * {@link RepositoryExistsCondition} UTs.
 *
 * @since 1.10.0
 */
public class RepositoryExistsConditionTest
{

    static final String TEST_REPOSITORY = "test-repository";

    private ActivationContext activationContext;

    private RepositoryExistsCondition underTest;

    private RepositoryEventsNotifier.Listener listener;

    private Repository repository;

    @Before
    public void setUp()
        throws Exception
    {
        activationContext = mock( ActivationContext.class );
        final RepositoryEventsNotifier repositoryEventsNotifier = mock( RepositoryEventsNotifier.class );
        final RepositoryConditions.RepositoryId repositoryId = mock( RepositoryConditions.RepositoryId.class );
        when( repositoryId.get() ).thenReturn( TEST_REPOSITORY );

        repository = mock( Repository.class );
        when( repository.getId() ).thenReturn( TEST_REPOSITORY );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );

        underTest = new RepositoryExistsCondition(
            activationContext, repositoryEventsNotifier, repositoryId
        );
        underTest.bind();

        ArgumentCaptor<RepositoryEventsNotifier.Listener> listenerCaptor = ArgumentCaptor.forClass(
            RepositoryEventsNotifier.Listener.class
        );

        verify( repositoryEventsNotifier ).addListener( listenerCaptor.capture() );
        listener = listenerCaptor.getValue();

        assertThat( underTest.isSatisfied(), is( false ) );
        listener.onAdded( repository );
    }

    /**
     * Condition should be satisfied initially (because mocking done in setup).
     */
    @Test
    public void satisfiedWhenRepositoryExists()
    {
        assertThat( underTest.isSatisfied(), is( true ) );
    }

    /**
     * Condition should become unsatisfied and notification sent when repository is removed.
     */
    @Test
    public void unsatisfiedWhenRepositoryRemoved()
    {
        assertThat( underTest.isSatisfied(), is( true ) );

        listener.onRemoved( repository );
        assertThat( underTest.isSatisfied(), is( false ) );

        verify( activationContext, times( 1 ) ).notifyUnsatisfied( underTest );
    }

    /**
     * Condition should become satisfied and notification sent when repository is added.
     */
    @Test
    public void satisfiedWhenRepositoryAdded()
    {
        assertThat( underTest.isSatisfied(), is( true ) );

        listener.onRemoved( repository );
        listener.onAdded( repository );
        assertThat( underTest.isSatisfied(), is( true ) );

        // twice as one is the initial notification
        verify( activationContext, times( 2) ).notifySatisfied( underTest );
    }

    /**
     * Condition should remain satisfied when another repository is removed.
     */
    @Test
    public void noReactionWhenAnotherRepositoryIsRemoved()
    {
        assertThat( underTest.isSatisfied(), is( true ) );
        final Repository anotherRepository = mock( Repository.class );
        when( anotherRepository.getId() ).thenReturn( "another" );
        listener.onRemoved( anotherRepository );
        assertThat( underTest.isSatisfied(), is( true ) );
    }

}
