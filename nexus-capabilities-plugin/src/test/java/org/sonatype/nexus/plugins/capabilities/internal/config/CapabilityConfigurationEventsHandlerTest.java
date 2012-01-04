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
package org.sonatype.nexus.plugins.capabilities.internal.config;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.internal.DefaultCapabilityReference;
import org.sonatype.nexus.plugins.capabilities.internal.DefaultCapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;

/**
 * {@link CapabilityConfigurationEventsHandler} UTs.
 *
 * @since 2.0
 */
public class CapabilityConfigurationEventsHandlerTest
{

    private static final boolean ENABLED = true;

    private static final boolean DISABLED = false;

    static final CapabilityIdentity TEST_IDENTITY = capabilityIdentity( "test-cc" );

    static final CapabilityType TEST_TYPE = capabilityType( "test" );

    /**
     * When an enabled capability configuration is created, create a reference, call create and enable it.
     */
    @Test
    public void onAddEnabled()
    {
        DefaultCapabilityReference reference = create( ENABLED );

        verify( reference ).create( Matchers.<Map<String, String>>any() );
        verify( reference ).enable();
        verify( reference ).activate();
    }

    /**
     * When an disabled capability configuration is created, create a reference, call create on it, but not enable.
     */
    @Test
    public void onAddDisabled()
    {
        DefaultCapabilityReference reference = create( DISABLED );

        verify( reference ).create( Matchers.<Map<String, String>>any() );
    }

    /**
     * When an enabled capability configuration is loaded, create a reference, call create and enable it.
     */
    @Test
    public void onLoadEnabled()
    {
        DefaultCapabilityReference reference = load( ENABLED );

        verify( reference ).load( Matchers.<Map<String, String>>any() );
        verify( reference ).enable();
        verify( reference ).activate();
    }

    /**
     * When an disabled capability configuration is loaded, create a reference, call create on it, but not enable.
     */
    @Test
    public void onLoadDisabled()
    {
        DefaultCapabilityReference reference = load( DISABLED );

        verify( reference ).load( Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was enabled and new configuration is not enabled, enabled() is not called, disable()
     * is called.
     */
    @Test
    public void onUpdateOldEnabledNewDisabled()
    {
        DefaultCapabilityReference reference = update( ENABLED, DISABLED );

        verify( reference ).disable();
        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was not enabled and new configuration is not enabled, enable() is not called, disable()
     * is not called.
     */
    @Test
    public void onUpdateOldDisabledNewDisabled()
    {
        DefaultCapabilityReference reference = update( DISABLED, DISABLED );

        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was enabled and new configuration is enabled, enable() is not called, disable() is not
     * called.
     */
    @Test
    public void onUpdateOldEnabledNewEnabled()
    {
        DefaultCapabilityReference reference = update( ENABLED, ENABLED );

        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was not enabled and new configuration is enabled, enable() is called, disable() is not
     * called.
     */
    @Test
    public void onUpdateOldDisabledNewEnabled()
    {
        DefaultCapabilityReference reference = update( DISABLED, ENABLED );

        verify( reference ).enable();
        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * When an capability configuration is removed, call remove on reference.
     */
    @Test
    public void onRemove()
    {
        DefaultCapabilityReference reference = remove();

        verify( reference ).remove();
    }

    private DefaultCapabilityReference create( final boolean enabled )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( TEST_IDENTITY );

        final DefaultCapabilityReference reference = mock( DefaultCapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );
        if ( !enabled )
        {
            doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
            doThrow( new AssertionError( "Activate not expected to be called" ) ).when( reference ).activate();
        }

        final DefaultCapabilityRegistry capabilityRegistry = mock( DefaultCapabilityRegistry.class );
        when( capabilityRegistry.create( TEST_IDENTITY, TEST_TYPE ) ).thenReturn( reference );

        final CCapability cc = new CCapability();
        cc.setId( "test-cc" );
        cc.setTypeId( "test" );
        cc.setEnabled( enabled );

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Added( cc )
        );

        return reference;
    }

    private DefaultCapabilityReference load( final boolean enabled )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( TEST_IDENTITY );

        final DefaultCapabilityReference reference = mock( DefaultCapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );
        if ( !enabled )
        {
            doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
            doThrow( new AssertionError( "Activate not expected to be called" ) ).when( reference ).activate();
        }

        final DefaultCapabilityRegistry capabilityRegistry = mock( DefaultCapabilityRegistry.class );
        when( capabilityRegistry.create( TEST_IDENTITY, TEST_TYPE ) ).thenReturn(
            reference );

        final CCapability cc = new CCapability();
        cc.setId( "test-cc" );
        cc.setTypeId( "test" );
        cc.setEnabled( enabled );

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Loaded( cc )
        );

        return reference;
    }

    private DefaultCapabilityReference update( final boolean oldEnabled, final boolean newEnabled )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( TEST_IDENTITY );

        final DefaultCapabilityReference reference = mock( DefaultCapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );
        when( reference.isActive() ).thenReturn( oldEnabled );

        final DefaultCapabilityRegistry capabilityRegistry = mock( DefaultCapabilityRegistry.class );
        when( capabilityRegistry.get( TEST_IDENTITY ) ).thenReturn( reference );

        final CCapability oldCapability = new CCapability();
        oldCapability.setId( "test-cc" );
        oldCapability.setEnabled( oldEnabled );

        final CCapability newCapability = new CCapability();
        newCapability.setId( "test-cc" );
        newCapability.setEnabled( newEnabled );

        {
            final CCapabilityProperty ccp = new CCapabilityProperty();
            ccp.setKey( "foo" );
            ccp.setKey( "bar" );
            oldCapability.getProperties().add( ccp );
        }
        {
            final CCapabilityProperty ccp = new CCapabilityProperty();
            ccp.setKey( "bar" );
            ccp.setKey( "foo" );
            newCapability.getProperties().add( ccp );
        }

        if ( !newEnabled )
        {
            doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        }
        if ( !oldEnabled )
        {
            doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();
        }

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Updated( newCapability, oldCapability )
        );

        return reference;
    }

    private DefaultCapabilityReference remove()
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( TEST_IDENTITY );

        final DefaultCapabilityReference reference = mock( DefaultCapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );

        final DefaultCapabilityRegistry capabilityRegistry = mock( DefaultCapabilityRegistry.class );
        when( capabilityRegistry.get( TEST_IDENTITY ) ).thenReturn( reference );

        final CCapability cc = new CCapability();
        cc.setId( "test-cc" );

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Removed( cc )
        );

        return reference;
    }

}
