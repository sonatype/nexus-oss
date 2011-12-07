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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;

/**
 * {@link NexusIsActiveCondition} UTs.
 *
 * @since 1.10.0
 */
public class NexusIsActiveConditionTest
    extends NexusEventBusTestSupport
{

    private NexusIsActiveCondition underTest;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        underTest = new NexusIsActiveCondition( eventBus );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Condition is not satisfied initially.
     */
    @Test
    public void notSatisfiedInitially()
    {
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Condition is satisfied when Nexus is started.
     */
    @Test
    public void satisfiedWhenNexusStarted()
    {
        underTest.handle( new NexusStartedEvent( this ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition is satisfied when negated is not satisfied.
     */
    @Test
    public void unsatisfiedWhenNexusStopped()
    {
        underTest.handle( new NexusStartedEvent( this ) );
        underTest.handle( new NexusStoppedEvent( this ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
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
