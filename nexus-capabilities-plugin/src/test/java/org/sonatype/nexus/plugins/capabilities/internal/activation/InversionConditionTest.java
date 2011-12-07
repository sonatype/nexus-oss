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
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;

/**
 * {@link InversionCondition} UTs.
 *
 * @since 1.10.0
 */
public class InversionConditionTest
        extends NexusEventBusTestSupport
{

    private InversionCondition underTest;

    private Condition condition;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        condition = mock( Condition.class );
        underTest = new InversionCondition( eventBus, condition );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Condition is satisfied initially (because mock returns false).
     */
    @Test
    public void not01()
    {
        assertThat( underTest.isSatisfied(), is( true ) );
    }

    /**
     * Condition is not satisfied when negated is satisfied.
     */
    @Test
    public void not02()
    {
        when( condition.isSatisfied() ).thenReturn( true );
        underTest.handle( new ConditionEvent.Satisfied( condition ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * Condition is satisfied when negated is not satisfied.
     */
    @Test
    public void not03()
    {
        when( condition.isSatisfied() ).thenReturn( false );
        underTest.handle( new ConditionEvent.Unsatisfied( condition ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
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
