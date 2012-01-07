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
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;

/**
 * {@link CapabilityOfTypeActiveCondition} UTs.
 *
 * @since 2.0
 */
public class CapabilityOfTypeActiveConditionTest
    extends NexusEventBusTestSupport
{

    private CapabilityReference ref1;

    private CapabilityReference ref2;

    private CapabilityReference ref3;

    private CapabilityRegistry capabilityRegistry;

    private CapabilityOfTypeActiveCondition underTest;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        capabilityRegistry = mock( CapabilityRegistry.class );

        final CapabilityType capabilityType = capabilityType( this.getClass().getName() );

        ref1 = mock( CapabilityReference.class );
        when( ref1.context() ).thenReturn( mock( CapabilityContext.class ) );
        when( ref1.context().type() ).thenReturn( capabilityType );

        ref2 = mock( CapabilityReference.class );
        when( ref2.context() ).thenReturn( mock( CapabilityContext.class ) );
        when( ref2.context().type() ).thenReturn( capabilityType );

        ref3 = mock( CapabilityReference.class );
        when( ref3.context() ).thenReturn( mock( CapabilityContext.class ) );
        when( ref3.context().type() ).thenReturn( capabilityType( Capability.class.getName() ) );

        final CapabilityDescriptorRegistry descriptorRegistry = mock( CapabilityDescriptorRegistry.class );
        final CapabilityDescriptor descriptor = mock( CapabilityDescriptor.class );

        when( descriptor.name() ).thenReturn( this.getClass().getSimpleName() );
        when( descriptorRegistry.get( capabilityType ) ).thenReturn( descriptor );

        underTest = new CapabilityOfTypeActiveCondition(
            eventBus, descriptorRegistry, capabilityRegistry, capabilityType
        );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Initially, condition should be unsatisfied.
     */
    @Test
    public void initiallyNotSatisfied()
    {
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Condition should not be satisfied if capability of specified type exists but is not active.
     */
    @Test
    public void capabilityOfTypeActive01()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( false );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Condition should be satisfied if capability of specified type exists and is active.
     */
    @Test
    public void capabilityOfTypeActive02()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should not be re-satisfied if a new active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeActive03()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref2 ) ).when( capabilityRegistry ).getAll();
        when( ref2.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should remain satisfied if another active capability of the specified type is removed.
     */
    @Test
    public void capabilityOfTypeActive04()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref2 ) ).when( capabilityRegistry ).getAll();
        when( ref2.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref2 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.AfterRemove( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should remain satisfied if another active capability of the specified type is passivated.
     */
    @Test
    public void capabilityOfTypeActive05()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref2 ) ).when( capabilityRegistry ).getAll();
        when( ref2.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref2 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.BeforePassivated( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should become unsatisfied when all capabilities have been removed.
     */
    @Test
    public void capabilityOfTypeActive06()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Collections.emptyList() ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.AfterRemove( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * Condition should remain satisfied if a condition of a different type is passivated.
     */
    @Test
    public void capabilityOfTypeActive07()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref3 ) ).when( capabilityRegistry ).getAll();
        when( ref3.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref3 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref3 ) ).when( capabilityRegistry ).getAll();
        when( ref3.context().isActive() ).thenReturn( false );
        underTest.handle( new CapabilityEvent.BeforePassivated( capabilityRegistry, ref3 ) );
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
