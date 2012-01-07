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
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;

/**
 * {@link PassivateCapabilityDuringUpdateCondition} UTs.
 *
 * @since 2.0
 */
public class PassivateCapabilityDuringUpdateConditionTest
    extends NexusEventBusTestSupport
{

    private CapabilityReference reference;

    private PassivateCapabilityDuringUpdateCondition underTest;

    private CapabilityRegistry capabilityRegistry;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        final CapabilityIdentity id = capabilityIdentity( "test" );

        capabilityRegistry = mock( CapabilityRegistry.class );
        reference = mock( CapabilityReference.class );

        final CapabilityContext context = mock( CapabilityContext.class );
        when( context.id() ).thenReturn( id );

        when( reference.context() ).thenReturn( context );

        underTest = new PassivateCapabilityDuringUpdateCondition( eventBus, id );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Condition should become unsatisfied before update and satisfied after update.
     */
    @Test
    public void passivateDuringUpdate()
    {
        underTest.handle( new CapabilityEvent.BeforeUpdate( capabilityRegistry, reference ) );
        underTest.handle( new CapabilityEvent.AfterUpdate( capabilityRegistry, reference ) );

        verifyEventBusEvents( unsatisfied( underTest ), satisfied( underTest ) );
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

}
