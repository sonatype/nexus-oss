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
package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;

/**
 * {@link PassivateCapabilityDuringUpdateCondition} UTs.
 *
 * @since 1.10.0
 */
public class PassivateCapabilityDuringUpdateConditionTest
    extends NexusEventBusTestSupport
{

    private CapabilityReference reference;

    private PassivateCapabilityDuringUpdateCondition underTest;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        final Capability capability = mock( Capability.class );
        this.reference = mock( CapabilityReference.class );
        when( this.reference.capability() ).thenReturn( capability );

        underTest = new PassivateCapabilityDuringUpdateCondition( eventBus, capability );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Condition should become unsatisfied before update and satisfied after update.
     */
    @Test
    public void passivateDuringUpdate()
    {
        underTest.handle( new CapabilityEvent.BeforeUpdate( reference ) );
        underTest.handle( new CapabilityEvent.AfterUpdate( reference ) );

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
