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

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is becoming unsatisfied before an capability is updated and becomes satisfied after capability was
 * updated.
 *
 * @since 1.10.0
 */
public class PassivateCapabilityDuringUpdateCondition
    extends AbstractCondition
{

    private final Capability capability;

    public PassivateCapabilityDuringUpdateCondition( final NexusEventBus eventBus,
                                                     final Capability capability )
    {
        super( eventBus, true );
        this.capability = checkNotNull( capability );
    }

    @Override
    protected void doBind()
    {
        getEventBus().register( this );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    @Subscribe
    public void handle( final CapabilityEvent.BeforeUpdate event )
    {
        if ( event.getReference().capability() == capability )
        {
            setSatisfied( false );
        }
    }

    @Subscribe
    public void handle( final CapabilityEvent.AfterUpdate event )
    {
        if ( event.getReference().capability() == capability )
        {
            setSatisfied( true );
        }
    }

    @Override
    public String toString()
    {
        return "Passivate during update of " + capability;
    }

}
