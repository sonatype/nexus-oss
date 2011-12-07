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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.CapabilityOfTypeActiveCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.CapabilityOfTypeExistsCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.PassivateCapabilityDuringUpdateCondition;

/**
 * {@link CapabilityConditions} UTs.
 *
 * @since 1.10.0
 */
public class CapabilityConditionsTest
{

    private CapabilityConditions underTest;

    @Before
    public void setUp()
    {
        final NexusEventBus eventBus = mock( NexusEventBus.class );
        final CapabilityRegistry capabilityRegistry = mock( CapabilityRegistry.class );
        underTest = new CapabilityConditions( eventBus, capabilityRegistry );
    }

    /**
     * capabilityOfTypeExists() factory method returns expected condition.
     */
    @Test
    public void capabilityOfTypeExists()
    {
        assertThat(
            underTest.capabilityOfTypeExists( Capability.class ),
            is( Matchers.<Condition>instanceOf( CapabilityOfTypeExistsCondition.class ) )
        );
    }

    /**
     * capabilityOfTypeActive() factory method returns expected condition.
     */
    @Test
    public void capabilityOfTypeActive()
    {
        assertThat(
            underTest.capabilityOfTypeActive( Capability.class ),
            is( Matchers.<Condition>instanceOf( CapabilityOfTypeActiveCondition.class ) )
        );
    }

    /**
     * reactivateCapabilityOnUpdate() factory method returns expected condition.
     */
    @Test
    public void reactivateCapabilityOnUpdate()
    {
        final Capability capability = mock( Capability.class );

        assertThat(
            underTest.passivateCapabilityDuringUpdate( capability ),
            is( Matchers.<Condition>instanceOf( PassivateCapabilityDuringUpdateCondition.class ) )
        );
    }

}
