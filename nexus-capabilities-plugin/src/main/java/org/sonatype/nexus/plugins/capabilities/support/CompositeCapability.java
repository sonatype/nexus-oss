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

import java.util.ArrayList;
import java.util.Collection;

import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;

public class CompositeCapability
    extends CapabilitySupport
{

    private final Collection<Capability> capabilities;

    public CompositeCapability()
    {
        capabilities = new ArrayList<Capability>();
    }

    public void add( final Capability capability )
    {
        capabilities.add( capability );
    }

    public void remove( final Capability capability )
    {
        capabilities.remove( capability );
    }

    @Override
    public void init( final CapabilityContext context )
    {
        super.init( context );
        for ( final Capability capability : capabilities )
        {
            capability.init( context );
        }
    }

    @Override
    public void onCreate()
        throws Exception
    {
        for ( final Capability capability : capabilities )
        {
            capability.onCreate();
        }
    }

    @Override
    public void onLoad()
        throws Exception
    {
        for ( final Capability capability : capabilities )
        {
            capability.onLoad();
        }
    }

    @Override
    public void onUpdate()
        throws Exception
    {
        for ( final Capability capability : capabilities )
        {
            capability.onUpdate();
        }
    }

    @Override
    public void onRemove()
        throws Exception
    {
        for ( final Capability capability : capabilities )
        {
            capability.onRemove();
        }
    }

    @Override
    public void onActivate()
        throws Exception
    {
        for ( final Capability capability : capabilities )
        {
            capability.onActivate();
        }
    }

    @Override
    public void onPassivate()
        throws Exception
    {
        for ( final Capability capability : capabilities )
        {
            capability.onPassivate();
        }
    }

}