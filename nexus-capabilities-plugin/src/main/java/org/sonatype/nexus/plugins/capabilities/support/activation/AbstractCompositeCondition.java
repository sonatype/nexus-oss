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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Composite {@link Condition} implementation support.
 *
 * @since 1.10.0
 */
public abstract class AbstractCompositeCondition
    extends AbstractCondition
{

    private final Condition[] conditions;

    public AbstractCompositeCondition( final NexusEventBus eventBus,
                                       final Condition... conditions )
    {
        super( eventBus, false );
        this.conditions = checkNotNull( conditions );
        checkArgument( conditions.length > 1, "A composite mush have at least 2 conditions" );
    }

    public AbstractCompositeCondition( final NexusEventBus eventBus,
                                       final Condition condition )
    {
        super( eventBus, false );
        this.conditions = new Condition[]{ checkNotNull( condition ) };
    }

    @Override
    protected void doBind()
    {
        for ( final Condition condition : conditions )
        {
            condition.bind();
        }
        getEventBus().register( this );
        setSatisfied( reevaluate( conditions ) );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
        for ( final Condition condition : conditions )
        {
            condition.release();
        }
    }

    @Subscribe
    public void handle( final ConditionEvent.Satisfied event )
    {
        if ( shouldReevaluateFor( event.getCondition() ) )
        {
            setSatisfied( reevaluate( conditions ) );
        }
    }

    @Subscribe
    public void handle( final ConditionEvent.Unsatisfied event )
    {
        if ( shouldReevaluateFor( event.getCondition() ) )
        {
            setSatisfied( reevaluate( conditions ) );
        }
    }

    @Override
    public String toString()
    {
        return "Re-evaluate " + AbstractCompositeCondition.this;
    }

    /**
     * Whether or not the composite conditions are satisfied as a unit.
     *
     * @param conditions to be checked (there are at least 2 conditions passed in)
     * @return true, if conditions are satisfied as a unit
     */
    protected abstract boolean reevaluate( final Condition... conditions );

    protected Condition[] getConditions()
    {
        return conditions;
    }

    private boolean shouldReevaluateFor( final Condition condition )
    {
        for ( final Condition watched : conditions )
        {
            if ( watched == condition )
            {
                return true;
            }
        }
        return false;
    }

}
