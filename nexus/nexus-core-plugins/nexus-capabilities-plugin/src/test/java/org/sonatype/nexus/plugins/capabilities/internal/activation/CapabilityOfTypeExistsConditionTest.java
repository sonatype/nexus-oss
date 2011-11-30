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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;
import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistryEvent;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;

/**
 * {@link CapabilityOfTypeExistsCondition} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityOfTypeExistsConditionTest
        extends NexusEventBusTestSupport
{

    private CapabilityReference ref1;

    private CapabilityReference ref2;

    private CapabilityRegistry capabilityRegistry;

    private CapabilityOfTypeExistsCondition underTest;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        capabilityRegistry = mock( CapabilityRegistry.class );

        final TestCapability testCapability = new TestCapability();
        ref1 = mock( CapabilityReference.class );
        when( ref1.capability() ).thenReturn( testCapability );
        ref2 = mock( CapabilityReference.class );
        when( ref2.capability() ).thenReturn( testCapability );

        underTest = new CapabilityOfTypeExistsCondition(
            eventBus, capabilityRegistry, TestCapability.class
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
     * Condition should become satisfied if an active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists01()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( true );
        underTest.handle( new CapabilityRegistryEvent.Created( ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should become satisfied if a non active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists02()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        when( ref1.isActive() ).thenReturn( false );
        underTest.handle( new CapabilityRegistryEvent.Created( ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should not be re-satisfied if a new active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists03()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        underTest.handle( new CapabilityRegistryEvent.Created( ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        underTest.handle( new CapabilityRegistryEvent.Created( ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should remain satisfied if another capability of the specified type is removed.
     */
    @Test
    public void capabilityOfTypeExists04()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        underTest.handle( new CapabilityRegistryEvent.Created( ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        underTest.handle( new CapabilityRegistryEvent.Created( ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref2 ) );
        underTest.handle( new CapabilityRegistryEvent.Removed( ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should become unsatisfied when all capabilities have been removed.
     */
    @Test
    public void capabilityOfTypeExists05()
    {
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        underTest.handle( new CapabilityRegistryEvent.Created( ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        when( capabilityRegistry.getAll() ).thenReturn( Collections.<CapabilityReference>emptyList() );
        underTest.handle( new CapabilityRegistryEvent.Removed( ref1 ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
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

    private static class TestCapability
        extends AbstractCapability
    {

        protected TestCapability()
        {
            super( "test-capability" );
        }

    }

}
