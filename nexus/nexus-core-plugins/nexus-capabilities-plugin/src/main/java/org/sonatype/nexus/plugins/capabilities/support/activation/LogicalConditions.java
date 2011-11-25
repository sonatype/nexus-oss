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

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * Factory of logical {@link Condition}s.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class LogicalConditions
{

    private final ActivationContext activationContext;

    @Inject
    public LogicalConditions( final ActivationContext activationContext )
    {
        this.activationContext = checkNotNull( activationContext );
    }

    /**
     * Creates a new condition that is satisfied when both conditions are satisfied (logical AND).
     *
     * @param left  left operand condition
     * @param right right operand condition
     * @return created condition
     */
    public Condition and( final Condition left, final Condition right )
    {
        return new ConjunctionCondition( activationContext, left, right );
    }

    /**
     * Creates a new condition that is satisfied when at least one condition is satisfied (logical OR).
     *
     * @param left  left operand condition
     * @param right right operand condition
     * @return created condition
     */
    public Condition or( final Condition left, final Condition right )
    {
        return new DisjunctionCondition( activationContext, left, right );
    }

    /**
     * A condition that applies a logical AND between conditions.
     *
     * @since 1.10.0
     */
    private static class ConjunctionCondition
        extends AbstractCompositeCondition
        implements Condition
    {

        public ConjunctionCondition( final ActivationContext activationContext,
                                     final Condition left,
                                     final Condition right )
        {
            super( activationContext, left, right );
        }

        @Override
        protected boolean check( final Condition... conditions )
        {
            for ( final Condition condition : conditions )
            {
                if ( !condition.isSatisfied() )
                {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * A condition that applies a logical OR between conditions.
     *
     * @since 1.10.0
     */
    private static class DisjunctionCondition
        extends AbstractCompositeCondition
        implements Condition
    {

        public DisjunctionCondition( final ActivationContext activationContext,
                                     final Condition left,
                                     final Condition right )
        {
            super( activationContext, left, right );
        }

        @Override
        protected boolean check( final Condition... conditions )
        {
            for ( final Condition condition : conditions )
            {
                if ( condition.isSatisfied() )
                {
                    return true;
                }
            }
            return false;
        }

    }

}
