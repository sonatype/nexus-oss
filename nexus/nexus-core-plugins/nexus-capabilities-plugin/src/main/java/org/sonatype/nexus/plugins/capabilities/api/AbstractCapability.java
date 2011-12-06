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
package org.sonatype.nexus.plugins.capabilities.api;

import java.util.Map;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

public abstract class AbstractCapability
    extends AbstractLoggingComponent
    implements Capability
{

    private final String id;

    protected AbstractCapability( final String id )
    {
        assert id != null : "Capability id cannot be null";

        this.id = id;
    }

    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void create( final Map<String, String> properties )
        throws Exception
    {
        // do nothing
    }

    @Override
    public void load( final Map<String, String> properties )
        throws Exception
    {
        // do nothing
    }

    @Override
    public void update( final Map<String, String> properties )
        throws Exception
    {
        // do nothing
    }

    @Override
    public void remove()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void activate()
        throws Exception
    {
        // do nothing
    }

    @Override
    public void passivate()
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
