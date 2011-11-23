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
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;

/**
 * {@link DefaultCapabilityReference} UTs.
 *
 * @since 1.10.0
 */
public class DefaultCapabilityReferenceTest
{

    private Capability capability;

    private DefaultCapabilityReference underTest;

    private DefaultCapabilityRegistry capabilityRegistry;

    @Before
    public void setUp()
    {
        capabilityRegistry = mock( DefaultCapabilityRegistry.class );
        final ActivationContext activationContext = mock( ActivationContext.class );
        capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test" );
        underTest = new DefaultCapabilityReference( capabilityRegistry, activationContext, capability );
    }

    /**
     * Capability is activated and active flag is set on activate.
     */
    @Test
    public void activateWhenNotActive()
    {
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
        verify( capability ).activate();
        verify( capabilityRegistry ).notify(
            notNull( CapabilityReference.class ), notNull( DefaultCapabilityRegistry.Notifier.class )
        );
    }

    /**
     * Capability is not activated activated again once it has been activated.
     */
    @Test
    public void activateWhenActive()
    {
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
        verify( capability ).activate();

        doThrow( new AssertionError( "Activate not expected to be called" ) ).when( capability ).activate();
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
    }

    /**
     * Capability is not passivated when is not active.
     */
    @Test
    public void passivateWhenNotActive()
    {
        assertThat( underTest.isActive(), is( false ) );
        doThrow( new AssertionError( "Passivate not expected to be called" ) ).when( capability ).passivate();
        underTest.passivate();
        assertThat( underTest.isActive(), is( false ) );
    }

    /**
     * Capability is passivated when is active.
     */
    @Test
    public void passivateWhenActive()
    {
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );

        underTest.passivate();
        assertThat( underTest.isActive(), is( false ) );
        verify( capability ).passivate();
        verify( capabilityRegistry, times( 2 ) ).notify(
            notNull( CapabilityReference.class ), notNull( DefaultCapabilityRegistry.Notifier.class )
        );
    }

    /**
     * Capability is not passivated when activation fails.
     */
    @Test
    public void activateProblem()
    {
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( capability ).activate();

        underTest.activate();
        assertThat( underTest.isActive(), is( false ) );
        verify( capability ).activate();
        doThrow( new AssertionError( "Passivate not expected to be called" ) ).when( capability ).passivate();
        underTest.passivate();
    }

    /**
     * Active flag is set when passivation problem.
     */
    @Test
    public void passivateProblem()
    {
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( capability ).passivate();

        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
        underTest.passivate();
        verify( capability ).passivate();
        assertThat( underTest.isActive(), is( false ) );
    }

}
