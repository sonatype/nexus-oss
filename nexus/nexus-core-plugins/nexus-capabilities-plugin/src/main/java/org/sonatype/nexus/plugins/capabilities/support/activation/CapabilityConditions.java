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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.CapabilityOfTypeActiveCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.CapabilityOfTypeExistsCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.PassivateCapabilityDuringUpdateCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.OnDemandCondition;

/**
 * Factory of {@link Condition}s related to Capabilities.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class CapabilityConditions
{

    private final CapabilityRegistry capabilityRegistry;

    private final ActivationContext activationContext;

    @Inject
    public CapabilityConditions( final ActivationContext activationContext,
                                 final CapabilityRegistry capabilityRegistry )
    {
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.activationContext = checkNotNull( activationContext );
    }

    /**
     * Creates a new condition that is satisfied when a capability of a specified type exists.
     *
     * @param type class of capability that should exist
     * @return created condition
     */
    public Condition capabilityOfTypeExists( final Class<? extends Capability> type )
    {
        return new CapabilityOfTypeExistsCondition( activationContext, capabilityRegistry, type );
    }

    /**
     * Creates a new condition that is satisfied when a capability of a specified type exists and is in an active state.
     *
     * @param type class of capability that should exist and be active
     * @return created condition
     */
    public Condition capabilityOfTypeActive( final Class<? extends Capability> type )
    {
        return new CapabilityOfTypeActiveCondition( activationContext, capabilityRegistry, type );
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
        return new PassivateCapabilityDuringUpdateCondition( activationContext, capabilityRegistry, capability );
    }

    /**
     * Creates a new condition that can be programatically satisfied/unsatisfied.
     *
     * @return created condition
     */
    public OnDemand onDemand()
    {
        return new OnDemandCondition( activationContext );
    }

    public static interface OnDemand
        extends Condition
    {

        /**
         * Reactivates the condition, fact that can trigger passivate / activate on capability using it.
         *
         * @return itself, for fluent api usage
         */
        OnDemand reactivate();

        /**
         * Marks condition as satisfied, fact that can trigger activation of capability using it.
         *
         * @return itself, for fluent api usage
         */
        OnDemand satisfy();

        /**
         * Marks condition as unsatisfied, fact that can trigger passivation of capability using it.
         *
         * @return itself, for fluent api usage
         */
        OnDemand unsatisfy();

    }

}
