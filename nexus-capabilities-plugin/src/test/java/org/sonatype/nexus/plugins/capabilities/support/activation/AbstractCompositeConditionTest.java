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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;

/**
 * {@link AbstractCompositeCondition} UTs.
 *
 * @since 1.10.0
 */
public class AbstractCompositeConditionTest
    extends NexusEventBusTestSupport
{

    private Condition c1;

    private Condition c2;

    private Condition c3;

    private TestCondition underTest;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        c1 = mock( Condition.class );
        c2 = mock( Condition.class );
        c3 = mock( Condition.class );

        underTest = new TestCondition( eventBus, c1, c2, c3 );
        underTest.bind();

        verify( c1 ).bind();
        verify( c2 ).bind();
        verify( c3 ).bind();
    }

    /**
     * On creation, condition is not satisfied.
     */
    @Test
    public void notSatisfiedInitially()
    {
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * When a condition is satisfied, check() is called and it returns true, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsSatisfiedAndReevaluateReturnsTrue()
    {
        when( c1.isSatisfied() ).thenReturn( true );
        underTest.handle( new ConditionEvent.Satisfied( c1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * When a condition is satisfied, check() is called and it returns false, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsSatisfiedAndCheckReturnsFalse()
    {
        when( c1.isSatisfied() ).thenReturn( true );
        underTest.handle( new ConditionEvent.Satisfied( c1 ) );

        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( false );
        underTest.handle( new ConditionEvent.Satisfied( c2 ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * When a condition is unsatisfied, check() is called and it returns true, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsUnsatisfiedAndCheckReturnsTrue()
    {
        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( true );
        underTest.handle( new ConditionEvent.Unsatisfied( c1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * When a condition is unsatisfied, check() is called and it returns false, condition is satisfied and notification
     * sent.
     */
    @Test
    public void whenMemberConditionIsUnsatisfiedAndCheckReturnsFalse()
    {
        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( true );
        underTest.handle( new ConditionEvent.Unsatisfied( c1 ) );

        when( c1.isSatisfied() ).thenReturn( false );
        when( c2.isSatisfied() ).thenReturn( false );
        underTest.handle( new ConditionEvent.Unsatisfied( c2 ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * On release, member conditions are released and handler removed from event bus.
     */
    @Test
    public void listenerRemovedOnRelease()
    {
        underTest.release();
        verify( c1 ).release();
        verify( c2 ).release();
        verify( c3 ).release();
        verify( eventBus ).unregister( underTest );
    }

    private static class TestCondition
        extends AbstractCompositeCondition
    {

        public TestCondition( final NexusEventBus eventBus,
                              final Condition... conditions )
        {
            super( eventBus, conditions );
        }

        @Override
        protected boolean reevaluate( final Condition... conditions )
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
