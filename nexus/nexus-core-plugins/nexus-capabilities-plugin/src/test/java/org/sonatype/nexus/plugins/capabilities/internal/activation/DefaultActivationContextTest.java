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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * {@link org.sonatype.nexus.plugins.capabilities.internal.DefaultCapabilityRegistry} UTs.
 *
 * @since 1.10.0
 */
public class DefaultActivationContextTest
{

    /**
     * Notify satisfied without listeners should not fail.
     */
    @Test
    public void notifySatisfiedWithoutListeners()
    {
        final Condition condition = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();
        underTest.notifySatisfied( condition  );
    }

    /**
     * Listeners are called when notifying satisfied event.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void notifySatisfiedWithAllConditionsListeners()
    {
        final ActivationContext.Listener listener1 = mock( ActivationContext.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onSatisfied(
            Mockito.<Condition>any()
        );
        final ActivationContext.Listener listener2 = mock( ActivationContext.Listener.class );

        final Condition condition = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();

        underTest.addListener( listener1 );
        underTest.addListener( listener2 );

        underTest.notifySatisfied( condition  );

        verify( listener1 ).onSatisfied( condition );
        verify( listener2 ).onSatisfied( condition );
    }

    /**
     * Listeners are called for specific conditions, when notifying satisfied event.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void notifySatisfiedWithConditionSpecificListeners()
    {
        final ActivationContext.Listener listener1 = mock( ActivationContext.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onSatisfied(
            Mockito.<Condition>any()
        );
        final ActivationContext.Listener listener2 = mock( ActivationContext.Listener.class );
        final ActivationContext.Listener listener3 = mock( ActivationContext.Listener.class );

        final Condition condition1 = mock( Condition.class );
        final Condition condition2 = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();

        underTest.addListener( listener1, condition1 );
        underTest.addListener( listener2, condition1, condition2 );
        underTest.addListener( listener3 );

        underTest.notifySatisfied( condition1  );
        underTest.notifySatisfied( condition2  );

        verify( listener1 ).onSatisfied( condition1 );
        verify( listener2 ).onSatisfied( condition1 );
        verify( listener2 ).onSatisfied( condition2 );
        verify( listener3 ).onSatisfied( condition1 );
        verify( listener3 ).onSatisfied( condition2 );

        verifyNoMoreInteractions( listener1 );
    }

    /**
     * Notify satisfied without listeners should not fail.
     */
    @Test
    public void notifyUnsatisfiedWithoutListeners()
    {
        final Condition condition = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();
        underTest.notifyUnsatisfied( condition );
    }

    /**
     * Listeners are called when notifying unsatisfied event.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void notifyUnsatisfiedWithAllConditionsListeners()
    {
        final ActivationContext.Listener listener1 = mock( ActivationContext.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onSatisfied(
            Mockito.<Condition>any()
        );
        final ActivationContext.Listener listener2 = mock( ActivationContext.Listener.class );

        final Condition condition = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();

        underTest.addListener( listener1 );
        underTest.addListener( listener2 );

        underTest.notifyUnsatisfied( condition );

        verify( listener1 ).onUnsatisfied( condition );
        verify( listener2 ).onUnsatisfied( condition );
    }

    /**
     * Listeners are called for specific conditions when notifying unsatisfied event,.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void notifyUnsatisfiedWithConditionSpecificListeners()
    {
        final ActivationContext.Listener listener1 = mock( ActivationContext.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onSatisfied(
            Mockito.<Condition>any()
        );
        final ActivationContext.Listener listener2 = mock( ActivationContext.Listener.class );
        final ActivationContext.Listener listener3 = mock( ActivationContext.Listener.class );

        final Condition condition1 = mock( Condition.class );
        final Condition condition2 = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();

        underTest.addListener( listener1, condition1 );
        underTest.addListener( listener2, condition1, condition2 );
        underTest.addListener( listener3 );

        underTest.notifyUnsatisfied( condition1 );
        underTest.notifyUnsatisfied( condition2 );

        verify( listener1 ).onUnsatisfied( condition1 );
        verify( listener2 ).onUnsatisfied( condition1 );
        verify( listener2 ).onUnsatisfied( condition2 );
        verify( listener3 ).onUnsatisfied( condition1 );
        verify( listener3 ).onUnsatisfied( condition2 );

        verifyNoMoreInteractions( listener1 );
    }

    /**
     * Listeners are not called once removed.
     */
    @Test
    public void listenersNotCalledAfterRemoved()
    {
        final ActivationContext.Listener listener = mock( ActivationContext.Listener.class );

        final Condition condition = mock( Condition.class );
        final DefaultActivationContext underTest = new DefaultActivationContext();

        underTest.addListener( listener );
        underTest.removeListener( listener );

        underTest.notifyUnsatisfied( condition  );

        verifyNoMoreInteractions( listener );
    }


}
