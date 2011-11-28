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
 * {@link InversionCondition} UTs.
 *
 * @since 1.10.0
 */
public class InversionConditionTest
{

    static final boolean UNSATISFIED = false;

    static final boolean SATISFIED = true;

    private ActivationContext activationContext;

    private InversionCondition underTest;

    private ActivationContext.Listener listener;

    private Condition condition;

    @Before
    public void setUp()
    {
        activationContext = mock( ActivationContext.class );
        condition = mock( Condition.class );
        underTest = new InversionCondition( activationContext, condition );
        underTest.bind();

        ArgumentCaptor<ActivationContext.Listener> listenerCaptor = ArgumentCaptor.forClass(
            ActivationContext.Listener.class
        );

        verify( activationContext ).addListener( listenerCaptor.capture(), eq( this.condition ) );
        listener = listenerCaptor.getValue();
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
        listener.onSatisfied( condition );
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Condition is satisfied when negated is not satisfied.
     */
    @Test
    public void not03()
    {
        when( condition.isSatisfied() ).thenReturn( false );
        listener.onSatisfied( condition );
        assertThat( underTest.isSatisfied(), is( true ) );
    }

}
