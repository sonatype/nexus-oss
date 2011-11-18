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
package org.sonatype.nexus.plugins.capabilities.internal.config.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfigurationEventInspector;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationUpdateEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;

/**
 * {@link CapabilityConfigurationEventInspector} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityConfigurationEventInspectorTest
{

    /**
     * If previous configuration was enabled, passivate is called and no activate is called because new configuration is
     * not enabled.
     */
    @Test
    public void capabilityUpdated01()
    {
        TestCapability capability = prepareForUpdate( true, false );

        verify( capability ).passivate();
        verify( capability ).update( Matchers.<Map<String, String>>any() );
        verifyNoMoreInteractions( capability );
    }

    /**
     * If previous configuration was not enabled, passivate is not called and no activate is called because new
     * configuration is not enabled.
     */
    @Test
    public void capabilityUpdated02()
    {
        TestCapability capability = prepareForUpdate( false, false );

        verify( capability ).update( Matchers.<Map<String, String>>any() );
        verifyNoMoreInteractions( capability );
    }

    /**
     * If previous configuration was enabled, passivate is called and activate is called because new configuration is
     * enabled.
     */
    @Test
    public void capabilityUpdated03()
    {
        TestCapability capability = prepareForUpdate( true, true );

        verify( capability ).passivate();
        verify( capability ).update( Matchers.<Map<String, String>>any() );
        verify( capability ).activate();
        verifyNoMoreInteractions( capability );
    }

    /**
     * If previous configuration was not enabled, passivate is not called and activate is called because new
     * configuration is enabled.
     */
    @Test
    public void capabilityUpdated04()
    {
        TestCapability capability = prepareForUpdate( false, true );

        verify( capability ).update( Matchers.<Map<String, String>>any() );
        verify( capability ).activate();
        verifyNoMoreInteractions( capability );
    }

    private TestCapability prepareForUpdate( final boolean oldEnabled, final boolean newEnabled )
    {
        final TestCapability capability = mock( TestCapability.class );
        when( capability.id() ).thenReturn( "test" );

        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.get( "test" ) ).thenReturn( capability );

        final CCapability oldCapability = new CCapability();
        oldCapability.setId( "test" );
        oldCapability.setEnabled( oldEnabled );
        final CCapability newCapability = new CCapability();
        newCapability.setId( "test" );
        newCapability.setEnabled( newEnabled );

        new CapabilityConfigurationEventInspector( capabilityRegistry ).inspect(
            new CapabilityConfigurationUpdateEvent( newCapability, oldCapability )
        );

        return capability;
    }

    private static interface TestCapability
        extends Capability, Capability.LifeCycle
    {

    }

}
