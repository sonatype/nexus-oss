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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.configuration.DefaultConfigurationIdGenerator;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.ValidatorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.storage.CapabilityStorage;
import org.sonatype.nexus.plugins.capabilities.internal.storage.CapabilityStorageItem;
import com.google.common.collect.Maps;

/**
 * {@link DefaultCapabilityRegistry} UTs.
 *
 * @since 2.0
 */
public class DefaultCapabilityRegistryTest
{

    static final CapabilityType CAPABILITY_TYPE = capabilityType( "test" );

    private NexusEventBus eventBus;

    private DefaultCapabilityRegistry underTest;

    private ArgumentCaptor<CapabilityEvent> rec;

    private CapabilityStorage capabilityStorage;

    private CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    @Before
    public void setUp()
    {
        capabilityStorage = mock( CapabilityStorage.class );

        final ValidatorRegistryProvider validatorRegistryProvider = mock( ValidatorRegistryProvider.class );
        final ValidatorRegistry validatorRegistry = mock( ValidatorRegistry.class );
        when( validatorRegistryProvider.get() ).thenReturn( validatorRegistry );

        final CapabilityFactory factory = mock( CapabilityFactory.class );
        when( factory.create() ).thenAnswer( new Answer<Capability>()
        {
            @Override
            public Capability answer( final InvocationOnMock invocation )
                throws Throwable
            {
                return mock( Capability.class );
            }

        } );

        final CapabilityFactoryRegistry capabilityFactoryRegistry = mock( CapabilityFactoryRegistry.class );
        when( capabilityFactoryRegistry.get( CAPABILITY_TYPE ) ).thenReturn( factory );

        capabilityDescriptorRegistry = mock( CapabilityDescriptorRegistry.class );
        when( capabilityDescriptorRegistry.get( CAPABILITY_TYPE ) ).thenReturn( mock( CapabilityDescriptor.class ) );

        eventBus = mock( NexusEventBus.class );

        final ActivationConditionHandlerFactory achf = mock( ActivationConditionHandlerFactory.class );
        when( achf.create( Mockito.<DefaultCapabilityReference>any() ) ).thenReturn(
            mock( ActivationConditionHandler.class )
        );
        final ValidityConditionHandlerFactory vchf = mock( ValidityConditionHandlerFactory.class );
        when( vchf.create( Mockito.<DefaultCapabilityReference>any() ) ).thenReturn(
            mock( ValidityConditionHandler.class )
        );

        underTest = new DefaultCapabilityRegistry(
            capabilityStorage,
            new DefaultConfigurationIdGenerator(),
            validatorRegistryProvider,
            capabilityFactoryRegistry,
            capabilityDescriptorRegistry,
            eventBus,
            achf,
            vchf
        );

        rec = ArgumentCaptor.forClass( CapabilityEvent.class );
    }

    /**
     * Create capability creates a non null reference and posts create event.
     *
     * @throws Exception unexpected
     */
    @Test
    public void create()
        throws Exception
    {
        final CapabilityReference reference = underTest.add( CAPABILITY_TYPE, true, null, null );
        assertThat( reference, is( not( nullValue() ) ) );

        verify( eventBus ).post( rec.capture() );
        assertThat( rec.getValue(), is( instanceOf( CapabilityEvent.Created.class ) ) );
        assertThat( rec.getValue().getReference(), is( equalTo( reference ) ) );
    }

    /**
     * Remove an existent capability posts remove event.
     *
     * @throws Exception unexpected
     */
    @Test
    public void remove()
        throws Exception
    {
        final CapabilityReference reference = underTest.add( CAPABILITY_TYPE, true, null, null );
        final CapabilityReference reference1 = underTest.remove( reference.context().id() );

        assertThat( reference1, is( equalTo( reference ) ) );

        verify( eventBus, times( 2 ) ).post( rec.capture() );
        assertThat( rec.getAllValues().get( 0 ), is( instanceOf( CapabilityEvent.Created.class ) ) );
        assertThat( rec.getAllValues().get( 0 ).getReference(), is( equalTo( reference1 ) ) );
        assertThat( rec.getAllValues().get( 1 ), is( instanceOf( CapabilityEvent.AfterRemove.class ) ) );
        assertThat( rec.getAllValues().get( 1 ).getReference(), is( equalTo( reference1 ) ) );
    }

    /**
     * Remove an inexistent capability does nothing and does not post remove event.
     *
     * @throws Exception unexpected
     */
    @Test
    public void removeInexistent()
        throws Exception
    {
        underTest.add( CAPABILITY_TYPE, true, null, null );
        final CapabilityReference reference1 = underTest.remove( capabilityIdentity( "foo" ) );

        assertThat( reference1, is( nullValue() ) );
    }

    /**
     * Get a created capability.
     *
     * @throws Exception unexpected
     */
    @Test
    public void get()
        throws Exception
    {
        final CapabilityReference reference = underTest.add( CAPABILITY_TYPE, true, null, null );
        final CapabilityReference reference1 = underTest.get( reference.context().id() );

        assertThat( reference1, is( not( nullValue() ) ) );
    }

    /**
     * Get an inexistent capability.
     *
     * @throws Exception unexpected
     */
    @Test
    public void getInexistent()
        throws Exception
    {
        underTest.add( CAPABILITY_TYPE, true, null, null );
        final CapabilityReference reference = underTest.get( capabilityIdentity( "foo" ) );

        assertThat( reference, is( nullValue() ) );
    }

    /**
     * Get all created capabilities.
     *
     * @throws Exception unexpected
     */
    @Test
    public void getAll()
        throws Exception
    {
        underTest.add( CAPABILITY_TYPE, true, null, null );
        underTest.add( CAPABILITY_TYPE, true, null, null );
        final Collection<? extends CapabilityReference> references = underTest.getAll();

        assertThat( references, hasSize( 2 ) );
    }

    private interface ValidatorRegistryProvider
        extends Provider<ValidatorRegistry>
    {

    }

    /**
     * On load if version did not change conversion should not be performed.
     *
     * @throws Exception unexpected
     */
    @Test
    public void load()
        throws Exception
    {
        final Map<String, String> oldProps = Maps.newHashMap();
        oldProps.put( "p1", "v1" );
        oldProps.put( "p2", "v2" );

        final CapabilityStorageItem item = new CapabilityStorageItem(
            0, capabilityIdentity( "foo" ), CAPABILITY_TYPE, true, null, oldProps
        );
        when( capabilityStorage.getAll() ).thenReturn( Arrays.asList( item ) );

        final CapabilityDescriptor descriptor = mock( CapabilityDescriptor.class );
        when( capabilityDescriptorRegistry.get( CAPABILITY_TYPE ) ).thenReturn( descriptor );
        when( descriptor.version() ).thenReturn( 0 );

        underTest.load();

        verify( capabilityStorage ).getAll();
        verify( descriptor ).version();
        verifyNoMoreInteractions( descriptor, capabilityStorage );
    }

    /**
     * On load if version changed conversion should be performed and new properties stored.
     *
     * @throws Exception unexpected
     */
    @Test
    public void loadWhenVersionChanged()
        throws Exception
    {
        final Map<String, String> oldProps = Maps.newHashMap();
        oldProps.put( "p1", "v1" );
        oldProps.put( "p2", "v2" );

        final CapabilityStorageItem item = new CapabilityStorageItem(
            0, capabilityIdentity( "foo" ), CAPABILITY_TYPE, true, null, oldProps
        );
        when( capabilityStorage.getAll() ).thenReturn( Arrays.asList( item ) );

        final CapabilityDescriptor descriptor = mock( CapabilityDescriptor.class );
        when( capabilityDescriptorRegistry.get( CAPABILITY_TYPE ) ).thenReturn( descriptor );
        when( descriptor.version() ).thenReturn( 1 );

        final Map<String, String> newProps = Maps.newHashMap();
        oldProps.put( "p1", "v1-converted" );
        oldProps.put( "p3", "v3" );

        when( descriptor.convert( oldProps, 0 ) ).thenReturn( newProps );

        underTest.load();

        verify( capabilityStorage ).getAll();
        verify( descriptor, atLeastOnce() ).version();
        verify( descriptor ).convert( oldProps, 0 );
        final ArgumentCaptor<CapabilityStorageItem> captor = ArgumentCaptor.forClass( CapabilityStorageItem.class );
        verify( capabilityStorage ).update( captor.capture() );
        assertThat( captor.getValue(), is( notNullValue() ) );

        final Map<String, String> actualNewProps = captor.getValue().properties();
        assertThat( newProps, is( equalTo( actualNewProps ) ) );
    }

    /**
     * On load if version changed conversion should be performed if conversion fails load is skipped.
     *
     * @throws Exception unexpected
     */
    @Test
    public void loadWhenVersionChangedAndConversionFails()
        throws Exception
    {
        final Map<String, String> oldProps = Maps.newHashMap();
        oldProps.put( "p1", "v1" );
        oldProps.put( "p2", "v2" );

        final CapabilityStorageItem item = new CapabilityStorageItem(
            0, capabilityIdentity( "foo" ), CAPABILITY_TYPE, true, null, oldProps
        );
        when( capabilityStorage.getAll() ).thenReturn( Arrays.asList( item ) );

        final CapabilityDescriptor descriptor = mock( CapabilityDescriptor.class );
        when( capabilityDescriptorRegistry.get( CAPABILITY_TYPE ) ).thenReturn( descriptor );
        when( descriptor.version() ).thenReturn( 1 );

        when( descriptor.convert( oldProps, 0 ) ).thenThrow( new RuntimeException( "expected" ) );

        underTest.load();

        verify( capabilityStorage ).getAll();
        verify( descriptor, atLeastOnce() ).version();
        verify( descriptor ).convert( oldProps, 0 );

        verifyNoMoreInteractions( descriptor, capabilityStorage );
    }

}
