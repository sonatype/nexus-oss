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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;

/**
 * {@link AbstractCondition} UTs.
 *
 * @since 1.10.0
 */
public class AbstractConditionTest
    extends NexusEventBusTestSupport
{

    private TestCondition underTest;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        underTest = new TestCondition( eventBus );
        underTest.bind();
    }

    /**
     * On creation, condition is not satisfied.
     */
    @Test
    public void notSatisfiedInitially()
    {
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * When satisfied set to true, condition is satisfied and notification is sent.
     */
    @Test
    public void satisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * When satisfied set to true after condition is already satisfied, condition is satisfied and notification is sent
     * only once.
     */
    @Test
    public void satisfiedOnAlreadySatisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * When satisfied set to false after condition is already unsatisfied, condition is unsatisfied and notification
     * is sent only once.
     */
    @Test
    public void unsatisfiedOnAlreadyUnsatisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * When satisfied set to true after condition is unsatisfied, condition is satisfied and notifications are sent.
     */
    @Test
    public void satisfiedOnUnsatisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ), satisfied( underTest ) );
    }

    /**
     * Calling setSatisfied() after release does not send events but sets status.
     */
    @Test
    public void setSatisfiedAfterRelease()
    {
        underTest.release();
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyNoEventBusEvents();
    }

    /**
     * Activation context getter returns the passed in value.
     */
    @Test
    public void getActivationContext()
    {
        assertThat( underTest.getEventBus(), is( equalTo( eventBus ) ) );
    }

    private static class TestCondition
        extends AbstractCondition
    {

        public TestCondition( final NexusEventBus eventBus )
        {
            super( eventBus );
        }

        @Override
        protected void doBind()
        {
            // do nothing
        }

        @Override
        protected void doRelease()
        {
            // do nothing
        }
    }

}
