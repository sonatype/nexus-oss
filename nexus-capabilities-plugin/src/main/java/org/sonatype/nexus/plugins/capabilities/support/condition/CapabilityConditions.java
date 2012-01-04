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
package org.sonatype.nexus.plugins.capabilities.support.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.CapabilityOfTypeActiveCondition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.CapabilityOfTypeExistsCondition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.PassivateCapabilityDuringUpdateCondition;

/**
 * Factory of {@link Condition}s related to capabilities.
 *
 * @since 2.0
 */
@Named
@Singleton
public class CapabilityConditions
{

    private final CapabilityRegistry capabilityRegistry;

    private final NexusEventBus eventBus;

    private final CapabilityDescriptorRegistry descriptorRegistry;

    @Inject
    public CapabilityConditions( final NexusEventBus eventBus,
                                 final CapabilityDescriptorRegistry descriptorRegistry,
                                 final CapabilityRegistry capabilityRegistry )
    {
        this.descriptorRegistry = checkNotNull( descriptorRegistry );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.eventBus = checkNotNull( eventBus );
    }

    /**
     * Creates a new condition that is satisfied when a capability of a specified type exists.
     *
     * @param type class of capability that should exist
     * @return created condition
     */
    public Condition capabilityOfTypeExists( final CapabilityType type )
    {
        return new CapabilityOfTypeExistsCondition( eventBus, descriptorRegistry, capabilityRegistry, type );
    }

    /**
     * Creates a new condition that is satisfied when a capability of a specified type exists and is in an active state.
     *
     * @param type class of capability that should exist and be active
     * @return created condition
     */
    public Condition capabilityOfTypeActive( final CapabilityType type )
    {
        return new CapabilityOfTypeActiveCondition( eventBus, descriptorRegistry, capabilityRegistry, type );
    }

    /**
     * Creates a new condition that is becoming unsatisfied before an capability is updated and becomes satisfied after
     * capability was updated.
     *
     * @param capability capability that should be passivated during update updated
     * @return created condition
     */
    public Condition passivateCapabilityDuringUpdate( final Capability capability )
    {
        return new PassivateCapabilityDuringUpdateCondition( eventBus, capability );
    }

}
