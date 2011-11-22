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
package org.sonatype.nexus.plugins.capabilities.support.activation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * {@link CapabilityConditionsFactory} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityConditionsFactoryTest
{

    @Test
    public void capabilityOfTypeExists()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        final CapabilityConditionsFactory underTest = new CapabilityConditionsFactory(
            capabilityRegistry, activationContext
        );
        final Condition condition = underTest.capabilityOfTypeExists( TestCapability.class );

        ArgumentCaptor<CapabilityRegistry.Listener> listenerCaptor = ArgumentCaptor.forClass(
            CapabilityRegistry.Listener.class
        );

        verify( capabilityRegistry ).addListener( listenerCaptor.capture() );

        assertThat( condition.isSatisfied(), is( false ) );

        // now add the test capability that our condition is waiting for
        final CapabilityReference ref1 = mock( CapabilityReference.class );
        final TestCapability testCapability = new TestCapability();
        when( ref1.capability() ).thenReturn( testCapability );

        // now add the test capability that our condition is waiting for
        final CapabilityReference ref2 = mock( CapabilityReference.class );
        when( ref2.capability() ).thenReturn( testCapability );

        // it should become satisfied and notify the context
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        listenerCaptor.getValue().onAdd( ref1 );
        assertThat( condition.isSatisfied(), is( true ) );
        verify( activationContext ).notifySatisfied( condition );

        // if a new capability is added, notification will not happen again as it is already satisfied
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1, ref2 ) );
        listenerCaptor.getValue().onAdd( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );
        verify( activationContext ).notifySatisfied( condition );

        // remove one capabilit. conditions should remain satisfied as there is still another one of that type
        when( capabilityRegistry.getAll() ).thenReturn( Arrays.asList( ref1 ) );
        listenerCaptor.getValue().onRemove( ref2 );
        assertThat( condition.isSatisfied(), is( true ) );
        verify( activationContext ).notifySatisfied( condition );

        // now remove the capability to check if condition becomes unsatisfied
        when( capabilityRegistry.getAll() ).thenReturn( Collections.<CapabilityReference>emptyList() );
        listenerCaptor.getValue().onRemove( ref1 );
        assertThat( condition.isSatisfied(), is( false ) );
        verify( activationContext ).notifyUnsatisfied( condition );
    }

    private static class TestCapability
        extends AbstractCapability
    {

        protected TestCapability()
        {
            super( "test-capability" );
        }

    }

}
