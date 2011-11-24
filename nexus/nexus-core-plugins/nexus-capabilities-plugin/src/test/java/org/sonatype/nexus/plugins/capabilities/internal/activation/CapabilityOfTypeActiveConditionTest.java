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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.CapabilityOfTypeActiveCondition;

/**
 * {@link CapabilityOfTypeActiveCondition} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityOfTypeActiveConditionTest
{

    private ActivationContext activationContext;

    private CapabilityReference ref1;

    private CapabilityReference ref2;

    private CapabilityReference ref3;

    private CapabilityRegistry capabilityRegistry;

    private CapabilityRegistry.Listener listener;

    private Condition underTest;

    @Before
    public void setUp()
    {
        activationContext = mock( ActivationContext.class );
        capabilityRegistry = mock( CapabilityRegistry.class );

        final TestCapability testCapability = new TestCapability();
        ref1 = mock( CapabilityReference.class );
        when( ref1.capability() ).thenReturn( testCapability );
        ref2 = mock( CapabilityReference.class );
        when( ref2.capability() ).thenReturn( testCapability );
        ref3 = mock( CapabilityReference.class );
        when( ref3.capability() ).thenReturn( mock( Capability.class ) );

        underTest = new CapabilityOfTypeActiveCondition(
            activationContext, capabilityRegistry, TestCapability.class
        );

        ArgumentCaptor<CapabilityRegistry.Listener> listenerCaptor = ArgumentCaptor.forClass(
            CapabilityRegistry.Listener.class
        );

        verify( capabilityRegistry ).addListener( listenerCaptor.capture() );
        listener = listenerCaptor.getValue();
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should not be satisfied if capability of specified type exists but is not active.
     */
    @Test
    public void capabilityOfTypeActive01()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( false );
        listener.onAdd( ref1 );
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should be satisfied if capability of specified type exists and is active.
     */
    @Test
    public void capabilityOfTypeActive02()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onActivate( ref1 );
        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should not be re-satisfied if a new active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeActive03()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref2.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( underTest.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should remain satisfied if another active capability of the specified type is removed.
     */
    @Test
    public void capabilityOfTypeActive04()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref2.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref2 ) );
        listener.onRemove( ref1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should remain satisfied if another active capability of the specified type is passivated.
     */
    @Test
    public void capabilityOfTypeActive05()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref2.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onPassivate( ref1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should become unsatisfied when all capabilities have been removed.
     */
    @Test
    public void capabilityOfTypeActive06()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Collections.<CapabilityReference>emptyList() );
        listener.onRemove( ref1 );
        assertThat( underTest.isSatisfied(), is( false ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
        verify( activationContext, times( 1 ) ).notifyUnsatisfied( underTest );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should remain satisfied if a condition of a different type is passivated.
     */
    @Test
    public void capabilityOfTypeActive07()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref3 ) );
        when( ref3.isActive() ).thenReturn( true );
        listener.onAdd( ref3 );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref3 ) );
        when( ref3.isActive() ).thenReturn( false );
        listener.onPassivate( ref3 );
        assertThat( underTest.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
    }

    private static class TestCapability
        extends AbstractCapability
    {

        protected TestCapability()
        {
            super( "test-capability" );
        }

    }

}
