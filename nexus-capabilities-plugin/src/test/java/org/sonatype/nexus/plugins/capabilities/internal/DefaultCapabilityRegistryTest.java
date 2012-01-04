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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistryEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;

/**
 * {@link DefaultCapabilityRegistry} UTs.
 *
 * @since 2.0
 */
public class DefaultCapabilityRegistryTest
{

    static final CapabilityType CAPABILITY_TYPE = capabilityType( "test" );

    static final CapabilityIdentity CAPABILITY_1 = capabilityIdentity( "capability-1" );

    static final CapabilityIdentity CAPABILITY_2 = capabilityIdentity( "capability-2" );

    private NexusEventBus eventBus;

    private DefaultCapabilityRegistry underTest;

    private ArgumentCaptor<CapabilityRegistryEvent> rec;

    @Before
    public void setUp()
    {
        final CapabilityFactory factory = mock( CapabilityFactory.class );
        when( factory.create( Matchers.<CapabilityIdentity>any() ) ).thenAnswer( new Answer<Capability>()
        {
            @Override
            public Capability answer( final InvocationOnMock invocation )
                throws Throwable
            {
                final Capability capability = mock( Capability.class );
                when( capability.id() ).thenReturn( (CapabilityIdentity) invocation.getArguments()[0] );
                return capability;
            }

        } );

        final CapabilityFactoryRegistry capabilityFactoryRegistry = mock( CapabilityFactoryRegistry.class );
        when( capabilityFactoryRegistry.get( CAPABILITY_TYPE ) ).thenReturn( factory );

        eventBus = mock( NexusEventBus.class );

        final ActivationConditionHandlerFactory achf = mock( ActivationConditionHandlerFactory.class );
        final ValidityConditionHandlerFactory vchf = mock( ValidityConditionHandlerFactory.class );

        underTest = new DefaultCapabilityRegistry( capabilityFactoryRegistry, eventBus, achf, vchf )
        {
            @Override
            CapabilityReference createReference( final CapabilityType type, final Capability capability )
            {
                return mock( CapabilityReference.class );
            }
        };

        rec = ArgumentCaptor.forClass( CapabilityRegistryEvent.class );
    }

    /**
     * Create capability creates a non null reference and posts create event.
     */
    @Test
    public void create()
    {
        final CapabilityReference reference = underTest.create( CAPABILITY_1, CAPABILITY_TYPE );
        assertThat( reference, is( not( nullValue() ) ) );

        verify( eventBus ).post( rec.capture() );
        assertThat( rec.getValue(), is( instanceOf( CapabilityRegistryEvent.Created.class ) ) );
        assertThat( rec.getValue().getReference(), is( equalTo( reference ) ) );
    }

    /**
     * Remove an existent capability posts remove event.
     */
    @Test
    public void remove()
    {
        final CapabilityReference reference = underTest.create( CAPABILITY_1, CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.remove( CAPABILITY_1 );

        assertThat( reference1, is( equalTo( reference ) ) );

        verify( eventBus, times( 2 ) ).post( rec.capture() );
        assertThat( rec.getAllValues().get( 0 ), is( instanceOf( CapabilityRegistryEvent.Created.class ) ) );
        assertThat( rec.getAllValues().get( 0 ).getReference(), is( equalTo( reference1 ) ) );
        assertThat( rec.getAllValues().get( 1 ), is( instanceOf( CapabilityRegistryEvent.Removed.class ) ) );
        assertThat( rec.getAllValues().get( 1 ).getReference(), is( equalTo( reference1 ) ) );
    }

    /**
     * Remove an inexistent capability does nothing and does not post remove event.
     */
    @Test
    public void removeInexistent()
    {
        underTest.create( CAPABILITY_1, CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.remove( CAPABILITY_2 );

        assertThat( reference1, is( nullValue() ) );
    }

    /**
     * Get a created capability.
     */
    @Test
    public void get()
    {
        underTest.create( CAPABILITY_1, CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.get( CAPABILITY_1 );

        assertThat( reference1, is( not( nullValue() ) ) );
    }

    /**
     * Get an inexistent capability.
     */
    @Test
    public void getInexistent()
    {
        underTest.create( CAPABILITY_1, CAPABILITY_TYPE );
        final CapabilityReference reference = underTest.get( CAPABILITY_2 );

        assertThat( reference, is( nullValue() ) );
    }

    /**
     * Get all created capabilities.
     */
    @Test
    public void getAll()
    {
        underTest.create( CAPABILITY_1, CAPABILITY_TYPE );
        underTest.create( CAPABILITY_2, CAPABILITY_TYPE );
        final Collection<CapabilityReference> references = underTest.getAll();

        assertThat( references, hasSize( 2 ) );
    }

}
