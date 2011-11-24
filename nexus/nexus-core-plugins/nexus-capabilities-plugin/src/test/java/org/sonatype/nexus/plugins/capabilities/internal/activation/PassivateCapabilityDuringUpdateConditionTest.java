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
import org.mockito.ArgumentCaptor;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;

/**
 * {@link PassivateCapabilityDuringUpdateCondition} UTs.
 *
 * @since 1.10.0
 */
public class PassivateCapabilityDuringUpdateConditionTest
{

    private ActivationContext activationContext;

    private CapabilityReference reference;

    private CapabilityRegistry.Listener listener;

    private PassivateCapabilityDuringUpdateCondition underTest;

    private CapabilityRegistry capabilityRegistry;

    @Before
    public void setUp()
    {
        activationContext = mock( ActivationContext.class );
        capabilityRegistry = mock( CapabilityRegistry.class );

        final Capability capability = mock( Capability.class );
        this.reference = mock( CapabilityReference.class );
        when( this.reference.capability() ).thenReturn( capability );

        underTest = new PassivateCapabilityDuringUpdateCondition(
            activationContext, capabilityRegistry, capability
        );

        ArgumentCaptor<CapabilityRegistry.Listener> listenerCaptor = ArgumentCaptor.forClass(
            CapabilityRegistry.Listener.class
        );

        verify( capabilityRegistry ).addListener( listenerCaptor.capture() );
        listener = listenerCaptor.getValue();
    }

    /**
     * Condition should become unsatisfied before update and satisfied after update.
     */
    @Test
    public void capabilityOfTypeExists01()
    {
        listener.beforeUpdate( reference );
        listener.afterUpdate( reference );

        verify( activationContext ).notifyUnsatisfied( underTest );
        verify( activationContext ).notifySatisfied( underTest );
    }

    /**
     * Capability registry listener is removed when releasing.
     */
    @Test
    public void releaseRemovesItselfAsListener()
    {
        underTest.release();

        verify( capabilityRegistry ).removeListener( underTest );
    }

}
