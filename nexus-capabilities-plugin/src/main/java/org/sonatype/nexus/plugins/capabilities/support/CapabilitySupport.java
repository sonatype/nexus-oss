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
package org.sonatype.nexus.plugins.capabilities.support;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.Condition;

public abstract class CapabilitySupport
    extends AbstractLoggingComponent
    implements Capability
{

    private CapabilityContext context;

    /**
     * Returns capability context.
     *
     * @return capability context
     */
    protected CapabilityContext context()
    {
        checkState( context != null, "Capability was not yet initialized" );
        return context;
    }

    @Override
    public void init( final CapabilityContext context )
    {
        this.context = checkNotNull( context );
    }

    @Override
    public String description()
    {
        return null;
    }

    @Override
    public String status()
    {
        return null;
    }

    @Override
    public void onCreate()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void onLoad()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void onUpdate()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void onRemove()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void onActivate()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void onPassivate()
        throws Exception
    {
        // do nothing
    }

    /**
     * Returns null, meaning that this capability is always active.
     *
     * @return null
     */
    @Override
    public Condition activationCondition()
    {
        return null;
    }

    /**
     * Returns null, meaning that this capability is always valid.
     *
     * @return null
     */
    @Override
    public Condition validityCondition()
    {
        return null;
    }

}
