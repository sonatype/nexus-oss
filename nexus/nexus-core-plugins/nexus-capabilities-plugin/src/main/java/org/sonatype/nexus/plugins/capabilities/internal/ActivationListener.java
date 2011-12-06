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
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;

/**
 * Handles capability activation by reacting capability activation condition being satisfied/unsatisfied.
 *
 * @since 1.10.0
 */
public class ActivationListener
    extends AbstractLoggingComponent
{

    private final NexusEventBus eventBus;

    private final CapabilityReference reference;

    private Condition activationCondition;

    @Inject
    ActivationListener( final NexusEventBus eventBus, final @Assisted CapabilityReference reference )
    {
        this.eventBus = checkNotNull( eventBus );
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
        if ( event.getCondition() == activationCondition )
        {
            reference.passivate();
        }
    }

    ActivationListener bind()
    {
        if ( activationCondition == null )
        {
            activationCondition = reference.capability().activationCondition();
            if ( activationCondition == null )
            {
                activationCondition = new SatisfiedCondition( "Capability has no activation condition" );
            }
            activationCondition.bind();
            eventBus.register( this );
            if ( activationCondition.isSatisfied() )
            {
                handle( new ConditionEvent.Satisfied( activationCondition ) );
            }
        }
        return this;
    }

    ActivationListener release()
    {
        if ( activationCondition != null )
        {
            eventBus.unregister( this );
            activationCondition.release();
            handle( new ConditionEvent.Unsatisfied( activationCondition ) );
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

}
