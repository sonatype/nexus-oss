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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.NexusStoppedEventInspector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;

public class NexusStoppedEventInspectorTest
{

    @Test
    public void capabilitiesArePassivated()
        throws Exception
    {
        final Capability capability1 = mock( Capability.class );
        final TestCapability capability2 = mock( TestCapability.class );
        when( capability2.id() ).thenReturn( "capability-2" );
        doThrow( new RuntimeException( "something went wrong" ) ).when( capability2 ).passivate();
        final TestCapability capability3 = mock( TestCapability.class );
        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( capability1, capability2, capability3 ) );

        new NexusStoppedEventInspector( capabilityRegistry ).inspect( new NexusStoppedEvent( this ) );

        verify( capability2 ).passivate();
        verify( capability3 ).passivate();
    }

    private static interface TestCapability
        extends Capability, Capability.LifeCycle
    {

    }

}
