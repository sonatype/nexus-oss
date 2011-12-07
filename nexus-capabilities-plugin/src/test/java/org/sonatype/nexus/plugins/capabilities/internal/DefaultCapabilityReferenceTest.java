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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.internal.DefaultCapabilityReference.sameProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;
import org.sonatype.nexus.plugins.capabilities.internal.activation.NexusIsActiveCondition;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;
import org.sonatype.nexus.plugins.capabilities.support.activation.NexusConditions;

/**
 * {@link DefaultCapabilityReference} UTs.
 *
 * @since 1.10.0
 */
public class DefaultCapabilityReferenceTest
{

    static final Map<String, String> NULL_PROPERTIES = null;

    private Capability capability;

    private DefaultCapabilityReference underTest;

    private NexusEventBus eventBus;

    private Condition activationCondition;

    private CapabilityConfiguration configurations;

    private Condition validityCondition;

    private ArgumentCaptor<CapabilityEvent> re;

    private ArgumentCaptor<Object> ebc;

    private ActivationConditionHandlerFactory achf;

    private ValidityConditionHandlerFactory vchf;

    private NexusIsActiveCondition nexusIsActiveCondition;

    @Before
    public void setUp()
    {
        eventBus = mock( NexusEventBus.class );
        configurations = mock( CapabilityConfiguration.class );

        final Conditions conditions = mock( Conditions.class );
        final NexusConditions nexusConditions = mock( NexusConditions.class );
        nexusIsActiveCondition = mock( NexusIsActiveCondition.class );

        when( nexusIsActiveCondition.isSatisfied() ).thenReturn( true );
        when( nexusConditions.active() ).thenReturn( nexusIsActiveCondition );
        when( conditions.nexus() ).thenReturn( nexusConditions );

        capability = mock( Capability.class );
        when( capability.id() ).thenReturn( "test-capability" );

        activationCondition = mock( Condition.class );
        when( activationCondition.isSatisfied() ).thenReturn( true );
        when( capability.activationCondition() ).thenReturn( activationCondition );

        validityCondition = mock( Condition.class );
        when( validityCondition.isSatisfied() ).thenReturn( true );
        when( capability.validityCondition() ).thenReturn( validityCondition );

        achf = mock( ActivationConditionHandlerFactory.class );
        when( achf.create( any( DefaultCapabilityReference.class ) ) ).thenAnswer(
            new Answer<ActivationConditionHandler>()
            {
                @Override
                public ActivationConditionHandler answer( final InvocationOnMock invocation )
                    throws Throwable
                {
                    return new ActivationConditionHandler(
                        eventBus, conditions, (CapabilityReference) invocation.getArguments()[0]
                    );
                }
            }
        );

        vchf = mock( ValidityConditionHandlerFactory.class );
        when( vchf.create( any( DefaultCapabilityReference.class ) ) ).thenAnswer(
            new Answer<ValidityConditionHandler>()
            {
                @Override
                public ValidityConditionHandler answer( final InvocationOnMock invocation )
                    throws Throwable
                {
                    return new ValidityConditionHandler(
                        eventBus, configurations, conditions, (CapabilityReference) invocation.getArguments()[0]
                    );
                }
            }
        );

        underTest = new DefaultCapabilityReference( eventBus, achf, vchf, capability );
        underTest.create( Collections.<String, String>emptyMap() );

        re = ArgumentCaptor.forClass( CapabilityEvent.class );
        ebc = ArgumentCaptor.forClass( Object.class );
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
        verify( activationCondition ).bind();
        verify( eventBus ).register( isA( ActivationConditionHandler.class ) );
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

        verify( activationCondition ).release();
        verify( eventBus ).unregister( isA( ActivationConditionHandler.class ) );
    }

    /**
     * Capability is activated and active flag is set on activate.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void activateWhenNotActive()
        throws Exception
    {
        underTest.enable();
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
        verify( capability ).activate();
        verify( eventBus ).post( re.capture() );
        assertThat( re.getValue(), is( instanceOf( CapabilityEvent.AfterActivated.class ) ) );
        assertThat( re.getValue().getReference(), is( equalTo( (CapabilityReference) underTest ) ) );
    }

    /**
     * Capability is not activated activated again once it has been activated.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void activateWhenActive()
        throws Exception
    {
        underTest.enable();
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
        verify( capability ).activate();

        doThrow( new AssertionError( "Activate not expected to be called" ) ).when( capability ).activate();
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );
    }

    /**
     * Capability is not passivated when is not active.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void passivateWhenNotActive()
        throws Exception
    {
        assertThat( underTest.isActive(), is( false ) );
        underTest.enable();
        underTest.passivate();
        assertThat( underTest.isActive(), is( false ) );

        doThrow( new AssertionError( "Passivate not expected to be called" ) ).when( capability ).passivate();
        underTest.passivate();
    }

    /**
     * Capability is passivated when is active.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void passivateWhenActive()
        throws Exception
    {
        underTest.enable();
        underTest.activate();
        assertThat( underTest.isActive(), is( true ) );

        underTest.passivate();
        assertThat( underTest.isActive(), is( false ) );
        verify( capability ).passivate();

        verify( eventBus, times( 2 ) ).post( re.capture() );
        assertThat( re.getAllValues().get( 0 ), is( instanceOf( CapabilityEvent.AfterActivated.class ) ) );
        assertThat( re.getAllValues().get( 0 ).getReference(), is( equalTo( (CapabilityReference) underTest ) ) );
        assertThat( re.getAllValues().get( 1 ), is( instanceOf( CapabilityEvent.BeforePassivated.class ) ) );
        assertThat( re.getAllValues().get( 1 ).getReference(), is( equalTo( (CapabilityReference) underTest ) ) );
    }

    /**
     * When activation fails, active state is false and capability remains enabled.
     * Calling passivate will do nothing.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void activateProblem()
        throws Exception
    {
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( capability ).activate();

        underTest.enable();
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( false ) );

        underTest.activate();
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( false ) );
        verify( capability ).activate();

        doThrow( new AssertionError( "Passivate not expected to be called" ) ).when( capability ).passivate();
        underTest.passivate();
    }

    /**
     * When passivation fails, active state is false and capability remains enabled.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void passivateProblem()
        throws Exception
    {
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( capability ).passivate();

        underTest.enable();
        underTest.activate();
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( true ) );

        underTest.passivate();
        verify( capability ).passivate();
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( false ) );
    }

    /**
     * When update fails and capability is not active, no exception is propagated and passivate is not called.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void updateProblemWhenNotActive()
        throws Exception

    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        properties.put( "p", "p" );
        final HashMap<String, String> previousProperties = new HashMap<String, String>();
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( capability ).update( properties );
        doThrow( new AssertionError( "Passivate not expected to be called" ) ).when( capability ).passivate();

        underTest.enable();
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( false ) );

        underTest.update( properties, previousProperties );
        verify( capability ).update( properties );
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( false ) );
    }

    /**
     * When update fails and capability is active, no exception is propagated and capability is passivated.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void updateProblemWhenActive()
        throws Exception
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        properties.put( "p", "p" );
        final HashMap<String, String> previousProperties = new HashMap<String, String>();
        doThrow( new UnsupportedOperationException( "Expected" ) ).when( capability ).update( properties );

        underTest.enable();
        underTest.activate();
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( true ) );

        underTest.update( properties, previousProperties );
        verify( capability ).update( properties );
        assertThat( underTest.isEnabled(), is( true ) );
        assertThat( underTest.isActive(), is( false ) );
        verify( capability ).passivate();
    }

    /**
     * Calling create forwards to capability (no need to call create as it is done in setup).
     *
     * @throws Exception re-thrown
     */
    @Test
    public void createIsForwardedToCapability()

        throws Exception
    {
        verify( capability ).create( Matchers.<Map<String, String>>any() );
    }

    /**
     * Calling load forwards to capability.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void loadIsForwardedToCapability()
        throws Exception
    {
        underTest = new DefaultCapabilityReference( eventBus, achf, vchf, capability );
        final HashMap<String, String> properties = new HashMap<String, String>();
        underTest.load( properties );

        verify( capability ).load( properties );
    }

    /**
     * Calling update forwards to capability if properties are different.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void updateIsForwardedToCapability()
        throws Exception
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        properties.put( "p", "p" );
        final HashMap<String, String> previousProperties = new HashMap<String, String>();
        underTest.update( properties, previousProperties );
        verify( capability ).update( properties );

        verify( eventBus, times( 2 ) ).post( re.capture() );
        assertThat( re.getAllValues().get( 0 ), is( instanceOf( CapabilityEvent.BeforeUpdate.class ) ) );
        assertThat( re.getAllValues().get( 0 ).getReference(), is( equalTo( (CapabilityReference) underTest ) ) );
        assertThat( re.getAllValues().get( 1 ), is( instanceOf( CapabilityEvent.AfterUpdate.class ) ) );
        assertThat( re.getAllValues().get( 1 ).getReference(), is( equalTo( (CapabilityReference) underTest ) ) );
    }

    /**
     * Calling update does not forwards to capability if properties are same.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void updateIsNotForwardedToCapabilityIfSameProperties()
        throws Exception
    {
        final HashMap<String, String> properties = new HashMap<String, String>();
        final HashMap<String, String> previousProperties = new HashMap<String, String>();
        doThrow( new AssertionError( "Update not expected to be called" ) ).when( capability ).update(
            Matchers.<Map<String, String>>any()
        );
        underTest.update( properties, previousProperties );
    }

    /**
     * Calling remove forwards to capability and handlers are removed.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void removeIsForwardedToCapability()
        throws Exception
    {
        underTest.enable();
        underTest.remove();
        verify( capability ).remove();
        verify( eventBus, times( 2 ) ).unregister( ebc.capture() );
    }

    @Test
    public void samePropertiesWhenBothNull()
    {
        assertThat( sameProperties( NULL_PROPERTIES, NULL_PROPERTIES ), is( true ) );
    }

    @Test
    public void samePropertiesWhenOldAreNull()
    {
        final HashMap<String, String> p2 = new HashMap<String, String>();
        p2.put( "p2", "p2" );
        assertThat( sameProperties( NULL_PROPERTIES, p2 ), is( false ) );
    }

    @Test
    public void samePropertiesWhenNewAreNull()
    {
        final HashMap<String, String> p1 = new HashMap<String, String>();
        p1.put( "p1", "p1" );
        assertThat( sameProperties( p1, NULL_PROPERTIES ), is( false ) );
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

    /**
     * When created it automatically listens to nexus becoming active (to be able to handle validity condition) and
     * for validity condition.
     */
    @Test
    public void listensToNexusIsActiveAndValidityConditions()
    {
        verify( eventBus ).register( ebc.capture() );
        assertThat( ebc.getValue(), is( instanceOf( ValidityConditionHandler.class ) ) );
    }

    /**
     * When validity condition becomes unsatisfied, capability is automatically removed.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void automaticallyRemoveWhenValidityConditionIsUnsatisfied()
        throws Exception
    {
        verify( eventBus ).register( ebc.capture() );
        assertThat( ebc.getValue(), is( instanceOf( ValidityConditionHandler.class ) ) );

        ( (ValidityConditionHandler) ebc.getValue() ).handle( new ConditionEvent.Unsatisfied( validityCondition ) );

        verify( configurations ).remove( capability.id() );
    }

    /**
     * When Nexus is shutdown capability is passivated.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void passivateWhenNexusIsShutdown()
        throws Exception
    {
        underTest.enable();
        underTest.activate();
        verify( eventBus, times( 2 ) ).register( ebc.capture() );
        assertThat( ebc.getAllValues().get( 0 ), is( instanceOf( ValidityConditionHandler.class ) ) );
        assertThat( ebc.getAllValues().get( 1 ), is( instanceOf( ActivationConditionHandler.class ) ) );

        ( (ActivationConditionHandler) ebc.getAllValues().get( 1 ) ).handle(
            new ConditionEvent.Unsatisfied( nexusIsActiveCondition )
        );

        verify( capability ).passivate();
    }

}
