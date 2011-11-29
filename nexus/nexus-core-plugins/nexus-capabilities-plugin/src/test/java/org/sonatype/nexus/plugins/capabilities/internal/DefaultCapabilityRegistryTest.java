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
package org.sonatype.nexus.plugins.capabilities.internal;

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
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;

/**
 * {@link DefaultCapabilityRegistry} UTs.
 *
 * @since 1.10.0
 */
public class DefaultCapabilityRegistryTest
{

    static final String CAPABILITY_TYPE = "test";

    private HashMap<String, CapabilityFactory> factoryMap;

    private ActivationContext activationContext;

    private DefaultCapabilityRegistry underTest;

    @Before
    public void setUp()
    {
        final CapabilityFactory factory = mock( CapabilityFactory.class );
        factoryMap = new HashMap<String, CapabilityFactory>();
        factoryMap.put( CAPABILITY_TYPE, factory );
        when( factory.create( Matchers.<String>any() ) ).thenAnswer( new Answer<Capability>()
        {
            @Override
            public Capability answer( final InvocationOnMock invocation )
                throws Throwable
            {
                final Capability capability = mock( Capability.class );
                when( capability.id() ).thenReturn( (String) invocation.getArguments()[0] );
                return capability;
            }

        } );
        activationContext = mock( ActivationContext.class );

        final CapabilityConfiguration configuration = mock( CapabilityConfiguration.class );
        final Conditions conditions = mock( Conditions.class );

        underTest = new DefaultCapabilityRegistry( factoryMap, activationContext, configuration, conditions )
        {
            @Override
            CapabilityReference createReference( final Capability capability )
            {
                return mock( CapabilityReference.class );
            }
        };
    }

    /**
     * Create capability without listeners.
     */
    @Test
    public void createWithoutListeners()
    {
        final CapabilityReference reference = underTest.create( "capability-1", CAPABILITY_TYPE );
        assertThat( reference, is( not( nullValue() ) ) );
    }

    /**
     * Remove an existent capability without listeners.
     */
    @Test
    public void removeWithoutListeners()
    {
        final CapabilityReference reference = underTest.create( "capability-1", CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.remove( "capability-1" );

        assertThat( reference1, is( equalTo( reference ) ) );
    }

    /**
     * Remove an inexistent capability without listeners.
     */
    @Test
    public void removeInexistentWithoutListeners()
    {
        underTest.create( "capability-1", CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.remove( "capability-2" );

        assertThat( reference1, is( nullValue() ) );
    }

    /**
     * Get a created capability.
     */
    @Test
    public void get()
    {
        underTest.create( "capability-1", CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.get( "capability-1" );

        assertThat( reference1, is( not( nullValue() ) ) );
    }

    /**
     * Get an inexistent capability.
     */
    @Test
    public void getInexistent()
    {
        underTest.create( "capability-1", CAPABILITY_TYPE );
        final CapabilityReference reference = underTest.get( "capability-2" );

        assertThat( reference, is( nullValue() ) );
    }

    /**
     * Get all created capabilities.
     */
    @Test
    public void getAll()
    {
        underTest.create( "capability-1", CAPABILITY_TYPE );
        underTest.create( "capability-2", CAPABILITY_TYPE );
        final Collection<CapabilityReference> references = underTest.getAll();

        assertThat( references, hasSize( 2 ) );
    }

    /**
     * Listeners get called when a capability is created.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void createWithExistingListeners()
    {
        final CapabilityRegistry.Listener listener1 = mock( CapabilityRegistry.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onAdd(
            Mockito.<CapabilityReference>any()
        );
        final CapabilityRegistry.Listener listener2 = mock( CapabilityRegistry.Listener.class );

        underTest.addListener( listener1 );
        underTest.addListener( listener2 );

        final CapabilityReference reference = underTest.create( "capability-1", CAPABILITY_TYPE );
        assertThat( reference, is( not( nullValue() ) ) );

        verify( listener1 ).onAdd( reference );
        verify( listener2 ).onAdd( reference );

    }

    /**
     * Listeners get called for each capability even if added after capabilities were created.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void addListenersAfterCreate()
    {
        final CapabilityRegistry.Listener listener1 = mock( CapabilityRegistry.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onAdd(
            Mockito.<CapabilityReference>any()
        );
        final CapabilityRegistry.Listener listener2 = mock( CapabilityRegistry.Listener.class );

        final CapabilityReference reference = underTest.create( "capability-1", CAPABILITY_TYPE );

        underTest.addListener( listener1 );
        underTest.addListener( listener2 );

        verify( listener1 ).onAdd( reference );
        verify( listener2 ).onAdd( reference );
    }

    /**
     * Remove an existent capability with listeners should call listeners.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void removeWithListeners()
    {
        final CapabilityRegistry.Listener listener1 = mock( CapabilityRegistry.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onAdd(
            Mockito.<CapabilityReference>any()
        );
        final CapabilityRegistry.Listener listener2 = mock( CapabilityRegistry.Listener.class );

        underTest.addListener( listener1 );
        underTest.addListener( listener2 );

        underTest.create( "capability-1", CAPABILITY_TYPE );
        final CapabilityReference reference = underTest.remove( "capability-1" );

        verify( listener1 ).onRemove( reference );
        verify( listener2 ).onRemove( reference );
    }

    /**
     * Remove an inexistent capability with listeners should not call listeners.
     * Ensures that exceptions are ignored (and logged).
     */
    @Test
    public void removeInexistentWithListeners()
    {
        final CapabilityRegistry.Listener listener1 = mock( CapabilityRegistry.Listener.class );
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( listener1 ).onAdd(
            Mockito.<CapabilityReference>any()
        );
        final CapabilityRegistry.Listener listener2 = mock( CapabilityRegistry.Listener.class );

        underTest.addListener( listener1 );
        underTest.addListener( listener2 );

        underTest.create( "capability-1", CAPABILITY_TYPE );
        underTest.remove( "capability-2" );

        verify( listener1 ).onAdd( Mockito.<CapabilityReference>any() );
        verify( listener2 ).onAdd( Mockito.<CapabilityReference>any() );
        verifyNoMoreInteractions( listener1, listener2 );
    }

}
