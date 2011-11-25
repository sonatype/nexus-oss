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
package org.sonatype.nexus.plugins.capabilities.support.activation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * {@link AbstractCompositeCondition} UTs.
 *
 * @since 1.10.0
 */
public class AbstractCompositeConditionTest
{

    static final boolean SATISFIED = true;

    private ActivationContext activationContext;

    private Condition c1;

    private Condition c2;

    private Condition c3;

    private ActivationContext.Listener listener;

    @Before
    public void setUp()
    {
        activationContext = mock( ActivationContext.class );
        c1 = mock( Condition.class );
        c2 = mock( Condition.class );
        c3 = mock( Condition.class );
    }

    private Condition prepare()
    {
        final AbstractCompositeCondition underTest = new TestCondition( activationContext, c1, c2, c3 );
        ArgumentCaptor<ActivationContext.Listener> listenerCaptor = ArgumentCaptor.forClass(
            ActivationContext.Listener.class
        );

        verify( activationContext ).addListener( listenerCaptor.capture(), eq( c1 ), eq( c2 ), eq( c3 ) );
        listener = listenerCaptor.getValue();

        return underTest;
    }

    /**
     * On creation, condition is not satisfied.
     */
    @Test
    public void notSatisfiedInitially()
    {
        final Condition underTest = prepare();
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * When a condition is satisfied, check() is called and it returns true, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsSatisfiedAndCheckReturnsTrue()
    {
        final Condition underTest = prepare();

        when( c1.isSatisfied() ).thenReturn( true );
        listener.onSatisfied( c1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        verify( activationContext ).notifySatisfied( underTest );
    }

    /**
     * When a condition is satisfied, check() is called and it returns false, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsSatisfiedAndCheckReturnsFalse()
    {
        final Condition underTest = prepare();

        when( c1.isSatisfied() ).thenReturn( true );
        listener.onSatisfied( c1 );

        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( false );
        listener.onSatisfied( c2 );
        assertThat( underTest.isSatisfied(), is( false ) );

        verify( activationContext ).notifySatisfied( underTest );
        verify( activationContext ).notifyUnsatisfied( underTest );
    }

    /**
     * When a condition is unsatisfied, check() is called and it returns true, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsUnsatisfiedAndCheckReturnsTrue()
    {
        final Condition underTest = prepare();

        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( true );
        listener.onUnsatisfied( c1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        verify( activationContext ).notifySatisfied( underTest );
    }

    /**
     * When a condition is unsatisfied, check() is called and it returns false, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsUnsatisfiedAndCheckReturnsFalse()
    {
        final Condition underTest = prepare();

        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( true );
        listener.onUnsatisfied( c1 );

        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( false );
        listener.onUnsatisfied( c2 );
        assertThat( underTest.isSatisfied(), is( false ) );

        verify( activationContext ).notifySatisfied( underTest );
        verify( activationContext ).notifyUnsatisfied( underTest );
    }

    /**
     * On release activation context listener is removed.
     */
    @Test
    public void listenerRemovedOnRelease()
    {
        final Condition underTest = prepare();
        underTest.release();
        verify( activationContext ).removeListener( listener, c1, c2, c3 );
    }

    private static class TestCondition
        extends AbstractCompositeCondition
    {

        public TestCondition( final ActivationContext activationContext,
                              final Condition... conditions )
        {
            super( activationContext, conditions );
        }

        @Override
        protected boolean check( final Condition... conditions )
        {
            for ( final Condition condition : conditions )
            {
                if ( condition.isSatisfied() )
                {
                    return true;
                }
            }
            return false;
        }
    }

}
