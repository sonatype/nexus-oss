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

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * {@link Condition} implementation support.
 */
public abstract class AbstractCondition
    implements Condition
{

    private final ActivationContext activationContext;

    private boolean satisfied;

    private boolean valid;

    protected AbstractCondition( final ActivationContext activationContext )
    {
        this( activationContext, false );
    }

    protected AbstractCondition( final ActivationContext activationContext, final boolean satisfied )
    {
        this.activationContext = checkNotNull( activationContext );
        this.satisfied = satisfied;
        valid = true;
    }

    protected ActivationContext getActivationContext()
    {
        return activationContext;
    }

    @Override
    public boolean isSatisfied()
    {
        checkState( valid, "Condition has already been released" );
        return satisfied;
    }

    @Override
    public Condition release()
    {
        valid = false;
        return this;
    }

    protected void setSatisfied( final boolean satisfied )
    {
        checkState( valid, "Condition has already been released" );
        if ( this.satisfied != satisfied )
        {
            this.satisfied = satisfied;
            if ( this.satisfied )
            {
                activationContext.notifySatisfied( this );
            }
            else
            {
                activationContext.notifyUnsatisfied( this );
            }
        }
    }

}
