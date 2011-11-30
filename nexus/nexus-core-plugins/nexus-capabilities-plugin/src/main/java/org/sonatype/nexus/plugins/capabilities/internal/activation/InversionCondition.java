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
package org.sonatype.nexus.plugins.capabilities.internal.activation;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCompositeCondition;

/**
 * A condition that applies a logical NOT on another condition.
 *
 * @since 1.10.0
 */
public class InversionCondition
    extends AbstractCompositeCondition
    implements Condition
{

    private final Condition condition;

    public InversionCondition( final NexusEventBus eventBus,
                               final Condition condition )
    {
        super( eventBus, condition );
        this.condition = condition;
    }

    @Override
    protected boolean reevaluate( final Condition... conditions )
    {
        return !conditions[0].isSatisfied();
    }

    @Override
    public String toString()
    {
        return "NOT " + condition;
    }

}
