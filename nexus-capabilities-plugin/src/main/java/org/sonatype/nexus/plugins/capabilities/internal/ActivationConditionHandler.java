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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;
import org.sonatype.nexus.plugins.capabilities.internal.activation.SatisfiedCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.UnsatisfiedCondition;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;

/**
 * Handles capability activation by reacting capability activation condition being satisfied/unsatisfied.
 *
 * @since 1.10.0
 */
public class ActivationConditionHandler
    extends AbstractLoggingComponent
{

    private final NexusEventBus eventBus;

    private final CapabilityReference reference;

    private final Conditions conditions;

    private Condition activationCondition;

    private Condition nexusActiveCondition;

    @Inject
    ActivationConditionHandler( final NexusEventBus eventBus,
                                final Conditions conditions,
                                final @Assisted CapabilityReference reference )
    {
        this.eventBus = checkNotNull( eventBus );
        this.conditions = checkNotNull( conditions );
        this.reference = checkNotNull( reference );
    }

    boolean isConditionSatisfied()
    {
        return activationCondition != null && activationCondition.isSatisfied();
    }

    @Subscribe
    public void handle( final ConditionEvent.Satisfied event )
    {
        if ( event.getCondition() == activationCondition )
        {
            reference.activate();
        }
    }

    @Subscribe
    public void handle( final ConditionEvent.Unsatisfied event )
    {
        if ( event.getCondition() == activationCondition || event.getCondition() == nexusActiveCondition )
        {
            reference.passivate();
        }
    }

    ActivationConditionHandler bind()
    {
        if ( activationCondition == null )
        {
            nexusActiveCondition = conditions.nexus().active();
            try
            {
                activationCondition = reference.capability().activationCondition();
            }
            catch ( Exception e )
            {
                activationCondition = new UnsatisfiedCondition( "Failed to determine activation condition" );
                getLogger().error(
                    "Could not get activation condition from capability {} ({}). Considering it as non activatable",
                    new Object[]{ reference.capability(), reference.capability().id(), e }
                );
            }
            if ( activationCondition == null )
            {
                activationCondition = new SatisfiedCondition( "Capability has no activation condition" );
            }
            nexusActiveCondition.bind();
            activationCondition.bind();
            eventBus.register( this );
        }
        return this;
    }

    ActivationConditionHandler release()
    {
        if ( activationCondition != null )
        {
            eventBus.unregister( this );
            nexusActiveCondition.release();
            activationCondition.release();
            activationCondition = null;
        }
        return this;
    }

    @Override
    public String toString()
    {
        return String.format(
            "Watching '%s' condition to activate/passivate capability '%s (id=%s)'",
            activationCondition, reference.capability(), reference.capability().id()
        );
    }

    public String explainWhyNotSatisfied()
    {
        return isConditionSatisfied() ? null : activationCondition.explainUnsatisfied();
    }

}
