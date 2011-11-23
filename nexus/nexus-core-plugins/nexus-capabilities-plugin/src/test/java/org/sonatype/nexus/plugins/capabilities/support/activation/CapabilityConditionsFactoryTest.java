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

/**
 * {@link CapabilityConditionsFactory} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityConditionsFactoryTest
{

    private ActivationContext activationContext;

    private CapabilityReference ref1;

    private CapabilityReference ref2;

    private CapabilityReference ref3;

    private CapabilityConditionsFactory underTest;

    private CapabilityRegistry capabilityRegistry;

    private CapabilityRegistry.Listener listener;

    private Condition condition;

    @Before
    public void setUp()
    {
        activationContext = mock( ActivationContext.class );
        capabilityRegistry = mock( CapabilityRegistry.class );
        underTest = new CapabilityConditionsFactory(
            capabilityRegistry, activationContext
        );

        final TestCapability testCapability = new TestCapability();
        ref1 = mock( CapabilityReference.class );
        when( ref1.capability() ).thenReturn( testCapability );
        ref2 = mock( CapabilityReference.class );
        when( ref2.capability() ).thenReturn( testCapability );
        ref3 = mock( CapabilityReference.class );
        when( ref3.capability() ).thenReturn( mock( Capability.class ) );
    }

    /**
     * Tests condition on a capability of certain type to exist.
     * <p/>
     * Condition should become satisfied if an active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists01()
    {
        prepare( underTest.capabilityOfTypeExists( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );
        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to exist.
     * <p/>
     * Condition should become satisfied if a non active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists02()
    {
        prepare( underTest.capabilityOfTypeExists( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( false );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );
        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to exist.
     * <p/>
     * Condition should not be re-satisfied if a new active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists03()
    {
        prepare( underTest.capabilityOfTypeExists( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        listener.onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to exist.
     * <p/>
     * Condition should remain satisfied if another capability of the specified type is removed.
     */
    @Test
    public void capabilityOfTypeExists04()
    {
        prepare( underTest.capabilityOfTypeExists( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        listener.onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref2 ) );
        listener.onRemove( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to exist.
     * <p/>
     * Condition should become unsatisfied when all capabilities have been removed.
     */
    @Test
    public void capabilityOfTypeExists05()
    {
        prepare( underTest.capabilityOfTypeExists( TestCapability.class ) );

        // now remove the capability to check if condition becomes unsatisfied
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Collections.<CapabilityReference>emptyList() );
        listener.onRemove( ref1 );
        assertThat( condition.isSatisfied(), is( false ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
        verify( activationContext, times( 1 ) ).notifyUnsatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should not be satisfied if capability of specified type exists but is not active.
     */
    @Test
    public void capabilityOfTypeActive01()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( false );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( false ) );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should be satisfied if capability of specified type exists and is active.
     */
    @Test
    public void capabilityOfTypeActive02()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onActivate( ref1 );
        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should not be re-satisfied if a new active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeActive03()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref2.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should remain satisfied if another active capability of the specified type is removed.
     */
    @Test
    public void capabilityOfTypeActive04()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref2.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref2 ) );
        listener.onRemove( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should remain satisfied if another active capability of the specified type is passivated.
     */
    @Test
    public void capabilityOfTypeActive05()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref2.isActive() ).thenReturn( true );
        listener.onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onPassivate( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should become unsatisfied when all capabilities have been removed.
     */
    @Test
    public void capabilityOfTypeActive06()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Collections.<CapabilityReference>emptyList() );
        listener.onRemove( ref1 );
        assertThat( condition.isSatisfied(), is( false ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
        verify( activationContext, times( 1 ) ).notifyUnsatisfied( condition );
    }

    /**
     * Tests condition on a capability of certain type to be active.
     * <p/>
     * Condition should remain satisfied if a condition of a different type is passivated.
     */
    @Test
    public void capabilityOfTypeActive07()
    {
        prepare( underTest.capabilityOfTypeActive( TestCapability.class ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        listener.onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref3 ) );
        when( ref3.isActive() ).thenReturn( true );
        listener.onAdd( ref3 );
        assertThat( condition.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref3 ) );
        when( ref3.isActive() ).thenReturn( false );
        listener.onPassivate( ref3 );
        assertThat( condition.isSatisfied(), is( true ) );

        verify( activationContext, times( 1 ) ).notifySatisfied( condition );
    }

    private void prepare( final Condition condition )
    {
        this.condition = condition;

        ArgumentCaptor<CapabilityRegistry.Listener> listenerCaptor = ArgumentCaptor.forClass(
            CapabilityRegistry.Listener.class
        );

        verify( capabilityRegistry ).addListener( listenerCaptor.capture() );
        listener = listenerCaptor.getValue();
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
