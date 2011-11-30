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

import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;

/**
 * {@link CapabilityConfigurationEventsHandler} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityConfigurationEventsHandlerTest
{

    private static final boolean ENABLED = true;

    private static final boolean DISABLED = false;

    /**
     * When an enabled capability configuration is created, create a reference, call create and enable it.
     */
    @Test
    public void onAddEnabled()
    {
        CapabilityReference reference = prepareForCreate( ENABLED );

        verify( reference ).create( Matchers.<Map<String, String>>any() );
        verify( reference ).enable();
    }

    /**
     * When an disabled capability configuration is created, create a reference, call create on it, but not enable.
     */
    @Test
    public void onAddDisabled()
    {
        CapabilityReference reference = prepareForCreate( DISABLED );

        verify( reference ).create( Matchers.<Map<String, String>>any() );
    }

    /**
     * When an enabled capability configuration is loaded, create a reference, call create and enable it.
     */
    @Test
    public void onLoadEnabled()
    {
        CapabilityReference reference = prepareForLoad( ENABLED );

        verify( reference ).load( Matchers.<Map<String, String>>any() );
        verify( reference ).enable();
    }

    /**
     * When an disabled capability configuration is loaded, create a reference, call create on it, but not enable.
     */
    @Test
    public void onLoadDisabled()
    {
        CapabilityReference reference = prepareForLoad( DISABLED );

        verify( reference ).load( Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was enabled and new configuration is not enabled, disable() is called.
     */
    @Test
    public void onUpdate01()
    {
        CapabilityReference reference = prepareForUpdate( ENABLED, DISABLED );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();

        verify( reference ).disable();
        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was not enabled and new configuration is not enabled, enable() is not called.
     */
    @Test
    public void onUpdate02()
    {
        CapabilityReference reference = prepareForUpdate( DISABLED, DISABLED );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was enabled and new configuration is enabled, enable() is not called.
     */
    @Test
    public void onUpdate03()
    {
        CapabilityReference reference = prepareForUpdate( ENABLED, DISABLED );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was not enabled and new configuration is enabled, enable() is called.
     */
    @Test
    public void onUpdate04()
    {
        CapabilityReference reference = prepareForUpdate( DISABLED, ENABLED );
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference ).enable();
        verify( reference ).update( Matchers.<Map<String, String>>any(), Matchers.<Map<String, String>>any() );
    }

    private CapabilityReference prepareForCreate( final boolean enabled )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test-cc" );

        final CapabilityReference reference = mock( CapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );

        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.create( "test-cc", "test" ) ).thenReturn( reference );

        final CCapability cc = new CCapability();
        cc.setId( "test-cc" );
        cc.setTypeId( "test" );
        cc.setEnabled( enabled );

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Added( cc )
        );

        return reference;
    }

    /**
     * When an capability configuration is removed, call remove on reference.
     */
    @Test
    public void onRemove()
    {
        CapabilityReference reference = prepareForRemove();

        verify( reference ).remove();
    }

    private CapabilityReference prepareForLoad( final boolean enabled )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test-cc" );

        final CapabilityReference reference = mock( CapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );

        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.create( "test-cc", "test" ) ).thenReturn( reference );

        final CCapability cc = new CCapability();
        cc.setId( "test-cc" );
        cc.setTypeId( "test" );
        cc.setEnabled( enabled );

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Loaded( cc )
        );

        return reference;
    }

    private CapabilityReference prepareForUpdate( final boolean oldEnabled, final boolean newEnabled )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test-cc" );

        final CapabilityReference reference = mock( CapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );
        when( reference.isActive() ).thenReturn( oldEnabled );

        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.get( "test-cc" ) ).thenReturn( reference );

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

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Updated( newCapability, oldCapability )
        );

        return reference;
    }

    private CapabilityReference prepareForRemove()
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test-cc" );

        final CapabilityReference reference = mock( CapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );

        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.get( "test-cc" ) ).thenReturn( reference );

        final CCapability cc = new CCapability();
        cc.setId( "test-cc" );

        new CapabilityConfigurationEventsHandler( capabilityRegistry ).handle(
            new CapabilityConfigurationEvent.Removed( cc )
        );

        return reference;
    }

}
