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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;

/**
 * {@link AbstractCondition} UTs.
 *
 * @since 1.10.0
 */
public class AbstractConditionTest
{

    /**
     * On creation, condition is not satisfied.
     */
    @Test
    public void notSatisfiedInitially()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * When satisfied set to true, condition is satisfied and notification is sent.
     */
    @Test
    public void satisfied()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );

        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        verify( activationContext ).notifySatisfied( underTest );
    }

    /**
     * When satisfied set to true after condition is already satisfied, condition is satisfied and notification is sent
     * only once.
     */
    @Test
    public void satisfiedOnAlreadySatisfied()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );

        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
    }

    /**
     * When satisfied set to false after condition is already unsatisfied, condition is unsatisfied and notification
     * is sent only once.
     */
    @Test
    public void unsatisfiedOnAlreadyUnsatisfied()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );

        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        verify( activationContext, times( 1 ) ).notifySatisfied( underTest );
        verify( activationContext, times( 1 ) ).notifyUnsatisfied( underTest );
    }

    /**
     * When satisfied set to true after condition is unsatisfied, condition is satisfied and notifications are sent.
     */
    @Test
    public void satisfiedOnUnsatisfied()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );

        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        verify( activationContext, times( 2 ) ).notifySatisfied( underTest );
        verify( activationContext, times( 1 ) ).notifyUnsatisfied( underTest );
    }

    /**
     * Calling isSatisfied() after release throws exception.
     */
    @Test( expected = IllegalStateException.class )
    public void isSatisfiedThrowsExceptionAfterRelease()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );

        underTest.release();
        underTest.isSatisfied();
    }

    /**
     * Calling setSatisfied() after release throws exception.
     */
    @Test( expected = IllegalStateException.class )
    public void setSatisfiedThrowsExceptionAfterRelease()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );

        underTest.release();
        underTest.setSatisfied( true );
    }

    /**
     * Activation context getter returns the passed in value.
     */
    @Test
    public void getActivationContext()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final AbstractCondition underTest = new TestCondition( activationContext );
        assertThat( underTest.getActivationContext(), is( equalTo( activationContext ) ) );
    }

    private static class TestCondition
        extends AbstractCondition
    {

        public TestCondition( final ActivationContext activationContext )
        {
            super( activationContext );
        }
    }

}
