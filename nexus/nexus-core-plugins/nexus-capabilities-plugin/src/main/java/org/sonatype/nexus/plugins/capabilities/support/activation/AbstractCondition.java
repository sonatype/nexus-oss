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
import static com.google.common.base.Preconditions.checkState;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;

/**
 * {@link Condition} implementation support.
 *
 * @since 1.10.0
 */
public abstract class AbstractCondition
    extends AbstractLoggingComponent
    implements Condition
{

    private final NexusEventBus eventBus;

    private boolean satisfied;

    private boolean active;

    protected AbstractCondition( final NexusEventBus eventBus )
    {
        this( eventBus, false );
    }

    protected AbstractCondition( final NexusEventBus eventBus, final boolean satisfied )
    {
        this.eventBus = checkNotNull( eventBus );
        this.satisfied = satisfied;
        active = false;
    }

    public NexusEventBus getEventBus()
    {
        return eventBus;
    }

    @Override
    public boolean isSatisfied()
    {
        checkState( active, "Condition has already been released or was not bounded" );
        return satisfied;
    }

    @Override
    public final Condition bind()
    {
        if ( !active )
        {
            active = true;
            doBind();
        }
        return this;
    }

    @Override
    public final Condition release()
    {
        if ( active )
        {
            doRelease();
            active = false;
        }
        return this;
    }

    /**
     * Template method to be implemented by subclasses for doing specific binding.
     */
    protected abstract void doBind();

    /**
     * Template method to be implemented by subclasses for doing specific releasing.
     */
    protected abstract void doRelease();

    /**
     * Sets teh satisfied status and notify activation context about thsi condition being satisfied/unsatisfied.
     *
     * @param satisfied true, if condition is satisfied
     */
    protected void setSatisfied( final boolean satisfied )
    {
        checkState( active, "Condition has already been released or was not bounded" );
        if ( this.satisfied != satisfied )
        {
            this.satisfied = satisfied;
            if ( this.satisfied )
            {
                eventBus.post( new ConditionEvent.Satisfied( this ) );
            }
            else
            {
                eventBus.post( new ConditionEvent.Unsatisfied( this ) );
            }
        }
    }

}
