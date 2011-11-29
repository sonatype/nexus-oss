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

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * Composite {@link Condition} implementation support.
 */
public abstract class AbstractCompositeCondition
    extends AbstractCondition
{

    private final Condition[] conditions;

    private ActivationContext.Listener listener;

    private final ActivationContext activationContext;

    public AbstractCompositeCondition( final ActivationContext activationContext,
                                       final Condition... conditions )
    {
        super( activationContext, false );
        this.activationContext = checkNotNull( activationContext );
        this.conditions = checkNotNull( conditions );
        checkArgument( conditions.length > 1, "A composite mush have at least 2 conditions" );
    }

    public AbstractCompositeCondition( final ActivationContext activationContext,
                                       final Condition condition )
    {
        super( activationContext, false );
        this.activationContext = checkNotNull( activationContext );
        this.conditions = new Condition[]{ checkNotNull( condition ) };
    }

    @Override
    protected void doBind()
    {
        for ( final Condition condition : conditions )
        {
            condition.bind();
        }
        activationContext.addListener(
            listener = new ActivationContext.Listener()
            {
                @Override
                public void onSatisfied( final Condition condition )
                {
                    setSatisfied( check( conditions ) );
                }

                @Override
                public void onUnsatisfied( final Condition condition )
                {
                    setSatisfied( check( conditions ) );
                }

                @Override
                public String toString()
                {
                    return "Re-evaluate " + AbstractCompositeCondition.this;
                }
            },
            conditions
        );
        setSatisfied( check( conditions ) );
    }

    @Override
    public void doRelease()
    {
        getActivationContext().removeListener( listener, conditions );
        for ( final Condition condition : conditions )
        {
            condition.release();
        }
    }

    /**
     * Whether or not the composite conditions are satisfied as a unit.
     *
     * @param conditions to be checked (there are at least 2 conditions passed in)
     * @return true, if conditions are satisfied as a unit
     */
    protected abstract boolean check( final Condition... conditions );

    protected Condition[] getConditions()
    {
        return conditions;
    }

}
