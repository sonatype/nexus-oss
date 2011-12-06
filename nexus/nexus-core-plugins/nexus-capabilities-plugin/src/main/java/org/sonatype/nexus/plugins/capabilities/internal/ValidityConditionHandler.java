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
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;

/**
 * Handles capability automatic removing by reacting to capability validity condition being satisfied/unsatisfied.
 *
 * @since 1.10.0
 */
public class ValidityConditionHandler
    extends AbstractLoggingComponent
{

    private final NexusEventBus eventBus;

    private final CapabilityReference reference;

    private final CapabilityConfiguration configurations;

    private final Conditions conditions;

    private Condition nexusActiveCondition;

    private Condition validityCondition;

    @Inject
    ValidityConditionHandler( final NexusEventBus eventBus,
                              final CapabilityConfiguration configurations,
                              final Conditions conditions,
                              final @Assisted CapabilityReference reference )
    {
        this.eventBus = checkNotNull( eventBus );
        this.configurations = checkNotNull( configurations );
        this.conditions = checkNotNull( conditions );
        this.reference = checkNotNull( reference );
    }

    boolean isConditionSatisfied()
    {
        return validityCondition != null && validityCondition.isSatisfied();
    }

    @Subscribe
    public void handle( final ConditionEvent.Satisfied event )
    {
        if ( event.getCondition() == nexusActiveCondition )
        {
            bindValidity();
        }
    }

    @Subscribe
    public void handle( final ConditionEvent.Unsatisfied event )
    {
        if ( event.getCondition() == nexusActiveCondition )
        {
            releaseValidity();
        }
        else if ( event.getCondition() == validityCondition )
        {
            reference.disable();
            try
            {
                configurations.remove( reference.capability().id() );
            }
            catch ( Exception e )
            {
                getLogger().error( "Failed to remove capability with id '{}'", reference.capability().id(), e );
            }
        }
    }

    ValidityConditionHandler bind()
    {
        if ( nexusActiveCondition == null )
        {
            nexusActiveCondition = conditions.nexus().active();
            nexusActiveCondition.bind();
            eventBus.register( this );
            if ( nexusActiveCondition.isSatisfied() )
            {
                handle( new ConditionEvent.Satisfied( nexusActiveCondition ) );
            }
        }
        return this;
    }

    ValidityConditionHandler release()
    {
        if ( nexusActiveCondition != null )
        {
            handle( new ConditionEvent.Unsatisfied( nexusActiveCondition ) );
            eventBus.unregister( this );
            nexusActiveCondition.release();
        }
        return this;
    }

    private ValidityConditionHandler bindValidity()
    {
        if ( validityCondition == null )
        {
            try
            {
                validityCondition = reference.capability().validityCondition();
            }
            catch ( Exception e )
            {
                validityCondition = new SatisfiedCondition(
                    "Always satisfied (failed to determine validity condition)"
                );
                getLogger().error(
                    "Could not get validation condition from capability {} ({}). Considering it as always valid",
                    new Object[]{ reference.capability(), reference.capability().id(), e }
                );
            }
            if ( validityCondition == null )
            {
                validityCondition = new SatisfiedCondition( "Always satisfied (capability has no validity condition)" );
            }
            validityCondition.bind();
        }
        return this;
    }

    private ValidityConditionHandler releaseValidity()
    {
        if ( validityCondition != null )
        {
            validityCondition.release();
            validityCondition = null;
        }
        return this;
    }

    @Override
    public String toString()
    {
        String condition = nexusActiveCondition.toString();
        if ( validityCondition != null )
        {
            condition = validityCondition + " WHEN " + condition;
        }
        return String.format(
            "Watching '%s' condition to validate/invalidate capability '%s (id=%s)'",
            condition, reference.capability(), reference.capability().id()
        );
    }

}
