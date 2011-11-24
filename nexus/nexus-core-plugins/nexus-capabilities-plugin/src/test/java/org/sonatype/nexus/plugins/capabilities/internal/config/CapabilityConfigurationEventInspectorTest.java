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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfigurationEventInspector.sameProperties;

import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationUpdateEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;

/**
 * {@link CapabilityConfigurationEventInspector} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityConfigurationEventInspectorTest
{

    private static final boolean ENABLED = true;

    private static final boolean DISABLED = false;

    private static final boolean SAME_PROPERTIES = true;

    private static final boolean DIFFERENT_PROPERTIES = false;

    /**
     * If previous configuration was enabled and new configuration is not enabled, disable() is called.
     */
    @Test
    public void capabilityUpdated01()
    {
        CapabilityReference reference = prepareForUpdate( ENABLED, DISABLED, DIFFERENT_PROPERTIES );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();

        verify( reference ).disable();
        verify( reference.capability() ).update( Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was enabled and new configuration is not enabled, disable() is called.
     * Update not called as they have same properties.
     */
    @Test
    public void capabilityUpdated01SameProperties()
    {
        CapabilityReference reference = prepareForUpdate( ENABLED, DISABLED, SAME_PROPERTIES );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();

        verify( reference ).disable();
    }

    /**
     * If previous configuration was not enabled and new configuration is not enabled, enable() is not called.
     */
    @Test
    public void capabilityUpdated02()
    {
        CapabilityReference reference = prepareForUpdate( DISABLED, DISABLED, DIFFERENT_PROPERTIES );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference.capability() ).update( Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was not enabled and new configuration is not enabled, enable() is not called.
     * Update not called as they have same properties.
     */
    @Test
    public void capabilityUpdated02SameProperties()
    {
        CapabilityReference reference = prepareForUpdate( DISABLED, DISABLED, SAME_PROPERTIES );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();
    }

    /**
     * If previous configuration was enabled and new configuration is enabled, enable() is not called.
     */
    @Test
    public void capabilityUpdated03()
    {
        CapabilityReference reference = prepareForUpdate( ENABLED, DISABLED, DIFFERENT_PROPERTIES );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference.capability() ).update( Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was enabled and new configuration is enabled, enable() is not called.
     * Update not called as they have same properties.
     */
    @Test
    public void capabilityUpdated03SameProperties()
    {
        CapabilityReference reference = prepareForUpdate( ENABLED, DISABLED, DIFFERENT_PROPERTIES );
        doThrow( new AssertionError( "Enable not expected to be called" ) ).when( reference ).enable();
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();
    }

    /**
     * If previous configuration was not enabled and new configuration is enabled, enable() is called.
     */
    @Test
    public void capabilityUpdated04()
    {
        CapabilityReference reference = prepareForUpdate( DISABLED, ENABLED, DIFFERENT_PROPERTIES );
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference ).enable();
        verify( reference.capability() ).update( Matchers.<Map<String, String>>any() );
    }

    /**
     * If previous configuration was not enabled and new configuration is enabled, enable() is called.
     * Update not called as they have same properties.
     */
    @Test
    public void capabilityUpdated04SameProperties()
    {
        CapabilityReference reference = prepareForUpdate( DISABLED, ENABLED, DIFFERENT_PROPERTIES );
        doThrow( new AssertionError( "Disable not expected to be called" ) ).when( reference ).disable();

        verify( reference ).enable();
    }

    private CapabilityReference prepareForUpdate( final boolean oldEnabled,
                                                  final boolean newEnabled,
                                                  final boolean sameProperties )
    {
        final Capability capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test" );
        final CapabilityReference reference = mock( CapabilityReference.class );
        when( reference.capability() ).thenReturn( capability );
        when( capability.id() ).thenReturn( "test" );
        when( reference.isActive() ).thenReturn( oldEnabled );

        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.get( "test" ) ).thenReturn( reference );

        final CCapability oldCapability = new CCapability();
        oldCapability.setId( "test" );
        oldCapability.setEnabled( oldEnabled );
        final CCapability newCapability = new CCapability();
        newCapability.setId( "test" );
        newCapability.setEnabled( newEnabled );

        if ( sameProperties )
        {
            doThrow( new AssertionError( "Capability update not expected to be called" ) ).when( capability ).update(
                Matchers.<Map<String, String>>any() );
        }
        else
        {
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
        }

        new CapabilityConfigurationEventInspector( capabilityRegistry ).inspect(
            new CapabilityConfigurationUpdateEvent( newCapability, oldCapability )
        );

        return reference;
    }

    @Test
    public void samePropertiesWhenBothNull()
    {
        final CCapability cc1 = new CCapability();
        cc1.setProperties( null );
        final CCapability cc2 = new CCapability();
        cc2.setProperties( null );
        assertThat( sameProperties( cc1, cc2 ), is( true ) );
    }

    @Test
    public void samePropertiesWhenOldAreNull()
    {
        final CCapability cc1 = new CCapability();
        cc1.setProperties( null );
        final CCapability cc2 = new CCapability();
        final CCapabilityProperty ccp2 = new CCapabilityProperty();
        ccp2.setKey( "ccp2" );
        ccp2.setValue( "ccp2" );
        cc2.getProperties().add( ccp2 );
        assertThat( sameProperties( cc1, cc2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenNewAreNull()
    {
        final CCapability cc1 = new CCapability();
        final CCapabilityProperty ccp1 = new CCapabilityProperty();
        ccp1.setKey( "ccp1" );
        ccp1.setValue( "ccp1" );
        cc1.getProperties().add( ccp1 );
        final CCapability cc2 = new CCapability();
        cc2.setProperties( null );
        assertThat( sameProperties( cc1, cc2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenBothAreSame()
    {
        final CCapability cc1 = new CCapability();
        final CCapabilityProperty ccp1 = new CCapabilityProperty();
        ccp1.setKey( "ccp" );
        ccp1.setValue( "ccp" );
        cc1.getProperties().add( ccp1 );
        final CCapability cc2 = new CCapability();
        final CCapabilityProperty ccp2 = new CCapabilityProperty();
        ccp2.setKey( "ccp" );
        ccp2.setValue( "ccp" );
        cc2.getProperties().add( ccp2 );
        assertThat( sameProperties( cc1, cc2 ), is( true ) );
    }

    @Test
    public void samePropertiesWhenDifferentValueSameKey()
    {
        final CCapability cc1 = new CCapability();
        final CCapabilityProperty ccp1 = new CCapabilityProperty();
        ccp1.setKey( "ccp" );
        ccp1.setValue( "ccp1" );
        cc1.getProperties().add( ccp1 );
        final CCapability cc2 = new CCapability();
        final CCapabilityProperty ccp2 = new CCapabilityProperty();
        ccp2.setKey( "ccp" );
        ccp2.setValue( "ccp2" );
        cc2.getProperties().add( ccp2 );
        assertThat( sameProperties( cc1, cc2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenDifferentSize()
    {
        final CCapability cc1 = new CCapability();
        final CCapabilityProperty ccp1 = new CCapabilityProperty();
        ccp1.setKey( "ccp" );
        ccp1.setValue( "ccp1" );
        final CCapabilityProperty ccp1_1 = new CCapabilityProperty();
        ccp1_1.setKey( "ccp1.1" );
        ccp1_1.setValue( "ccp1.1" );
        cc1.getProperties().add( ccp1 );
        final CCapability cc2 = new CCapability();
        final CCapabilityProperty ccp2 = new CCapabilityProperty();
        ccp2.setKey( "ccp" );
        ccp2.setValue( "ccp2" );
        cc2.getProperties().add( ccp2 );
        assertThat( sameProperties( cc1, cc2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenDifferentKeys()
    {
        final CCapability cc1 = new CCapability();
        final CCapabilityProperty ccp1 = new CCapabilityProperty();
        ccp1.setKey( "ccp1" );
        ccp1.setValue( "ccp" );
        cc1.getProperties().add( ccp1 );
        final CCapability cc2 = new CCapability();
        final CCapabilityProperty ccp2 = new CCapabilityProperty();
        ccp2.setKey( "ccp2" );
        ccp2.setValue( "ccp" );
        cc2.getProperties().add( ccp2 );
        assertThat( sameProperties( cc1, cc2 ), is( false ) );
    }

}
