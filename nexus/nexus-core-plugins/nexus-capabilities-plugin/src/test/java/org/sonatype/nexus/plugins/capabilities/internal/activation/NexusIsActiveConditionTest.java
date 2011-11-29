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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;

/**
 * {@link NexusIsActiveCondition} UTs.
 *
 * @since 1.10.0
 */
public class NexusIsActiveConditionTest
{

    private ActivationContext activationContext;

    private NexusIsActiveCondition underTest;

    @Before
    public void setUp()
    {
        activationContext = mock( ActivationContext.class );
        underTest = new NexusIsActiveCondition( activationContext);
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
        underTest.acknowledgeNexusStarted();
        assertThat( underTest.isSatisfied(), is( true ) );
        verify( activationContext ).notifySatisfied( underTest );
    }

    /**
     * Condition is satisfied when negated is not satisfied.
     */
    @Test
    public void unsatisfiedWhenNexusStopped()
    {
        underTest.acknowledgeNexusStarted();
        underTest.acknowledgeNexusStopped();
        assertThat( underTest.isSatisfied(), is( false ) );
        verify( activationContext ).notifyUnsatisfied( underTest );
    }

}
