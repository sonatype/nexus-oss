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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.internal.DefaultCapabilityReference.sameProperties;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

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

    private ActivationContext activationContext;

    private Condition activationCondition;

    @Before
    public void setUp()
    {
        capabilityRegistry = mock( DefaultCapabilityRegistry.class );
        activationContext = mock( ActivationContext.class );
        capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test" );
        activationCondition = mock( Condition.class );
        when( activationCondition.isSatisfied() ).thenReturn( true );
        when( capability.activationCondition() ).thenReturn( activationCondition );
        underTest = new DefaultCapabilityReference( capabilityRegistry, activationContext, capability );

    }

    /**
     * Capability is enabled and enable flag is set.
     */
    @Test
    public void enableWhenNotEnabled()
    {
        assertThat( underTest.isEnabled(), is( false ) );
        underTest.enable();
        assertThat( underTest.isEnabled(), is( true ) );
        verify( activationContext ).addListener(
            Matchers.<ActivationContext.Listener>any(), eq( activationCondition )
        );
    }

    /**
     * Capability is disabled and enable flag is set.
     */
    @Test
    public void disableWhenEnabled()
    {
        assertThat( underTest.isEnabled(), is( false ) );
        underTest.enable();
        assertThat( underTest.isEnabled(), is( true ) );
        underTest.disable();
        assertThat( underTest.isEnabled(), is( false ) );
    }

    /**
     * Capability is activated and active flag is set on activate.
     */
    @Test
    public void activateWhenNotActive()
    {
        underTest.enable();
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
        underTest.enable();
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
        underTest.enable();
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

        underTest.enable();
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

        underTest.enable();
        assertThat( underTest.isActive(), is( true ) );
        underTest.passivate();
        verify( capability ).passivate();
        assertThat( underTest.isActive(), is( false ) );
    }

    /**
     * Calling create forwards to capability.
     */
    @Test
    public void createIsForwardedToCapability()
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        underTest.create( properties );
        verify( capability ).create( properties );
    }

    /**
     * Calling load forwards to capability.
     */
    @Test
    public void loadIsForwardedToCapability()
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        underTest.load( properties );
        verify( capability ).load( properties );
    }

    /**
     * Calling update forwards to capability if properties are different.
     */
    @Test
    public void updateIsForwardedToCapability()
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        properties.put( "p", "p" );
        final HashMap<String, String> previousProperties = new HashMap<String, String>();
        underTest.update( properties, previousProperties );
        verify( capability ).update( properties );
        verify( capabilityRegistry, times( 2 ) ).notify(
            notNull( CapabilityReference.class ), notNull( DefaultCapabilityRegistry.Notifier.class )
        );
    }

    /**
     * Calling update does not forwards to capability if properties are same.
     */
    @Test
    public void updateIsNotForwardedToCapabilityIfSameProperties()
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        final HashMap<String, String> previousProperties = new HashMap<String, String>();
        doThrow( new AssertionError( "Update not expected to be called" ) ).when( capability ).update(
            Matchers.<Map<String, String>>any()
        );
        underTest.update( properties, previousProperties );
    }

    /**
     * Calling remove forwards to capability.
     */
    @Test
    public void removeIsForwardedToCapability()
    {
        underTest.enable();
        underTest.remove();
        verify( capability ).remove();
        verify( activationContext ).removeListener(
            Matchers.<ActivationContext.Listener>any(), eq( activationCondition )
        );
    }

    @Test
    public void samePropertiesWhenBothNull()
    {
        assertThat( sameProperties( null, null ), is( true ) );
    }

    @Test
    public void samePropertiesWhenOldAreNull()
    {
        final HashMap<String, String> p2 = new HashMap<String, String>();
        p2.put( "p2", "p2" );
        assertThat( sameProperties( null, p2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenNewAreNull()
    {
        final HashMap<String, String> p1 = new HashMap<String, String>();
        p1.put( "p1", "p1" );
        assertThat( sameProperties( p1, null ), is( false ) );
    }

    @Test
    public void samePropertiesWhenBothAreSame()
    {
        final HashMap<String, String> p1 = new HashMap<String, String>();
        p1.put( "p", "p" );
        final HashMap<String, String> p2 = new HashMap<String, String>();
        p2.put( "p", "p" );
        assertThat( sameProperties( p1, p2 ), is( true ) );
    }

    @Test
    public void samePropertiesWhenDifferentValueSameKey()
    {
        final HashMap<String, String> p1 = new HashMap<String, String>();
        p1.put( "p", "p1" );
        final HashMap<String, String> p2 = new HashMap<String, String>();
        p2.put( "p", "p2" );
        assertThat( sameProperties( p1, p2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenDifferentSize()
    {
        final HashMap<String, String> p1 = new HashMap<String, String>();
        p1.put( "p1.1", "p1.1" );
        p1.put( "p1.2", "p1.2" );
        final HashMap<String, String> p2 = new HashMap<String, String>();
        p2.put( "p2", "p2" );
        assertThat( sameProperties( p1, p2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenDifferentKeys()
    {
        final HashMap<String, String> p1 = new HashMap<String, String>();
        p1.put( "p1", "p" );
        final HashMap<String, String> p2 = new HashMap<String, String>();
        p2.put( "p2", "p" );
        assertThat( sameProperties( p1, p2 ), is( false ) );
    }

}
