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

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistryEvent;
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

    private NexusEventBus eventBus;

    private DefaultCapabilityRegistry underTest;

    private ArgumentCaptor<CapabilityRegistryEvent> rec;

    @Before
    public void setUp()
    {
        final CapabilityFactory factory = mock( CapabilityFactory.class );
        final HashMap<String, CapabilityFactory> factoryMap = new HashMap<String, CapabilityFactory>();

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
        eventBus = mock( NexusEventBus.class );

        final CapabilityConfiguration configuration = mock( CapabilityConfiguration.class );
        final Conditions conditions = mock( Conditions.class );

        underTest = new DefaultCapabilityRegistry( factoryMap, eventBus, configuration, conditions )
        {
            @Override
            CapabilityReference createReference( final Capability capability )
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
        final CapabilityReference reference = underTest.create( "capability-1", CAPABILITY_TYPE );
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
        final CapabilityReference reference = underTest.create( "capability-1", CAPABILITY_TYPE );
        final CapabilityReference reference1 = underTest.remove( "capability-1" );

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

}
