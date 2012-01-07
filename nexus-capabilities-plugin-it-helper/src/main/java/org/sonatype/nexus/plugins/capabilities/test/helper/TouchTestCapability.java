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
package org.sonatype.nexus.plugins.capabilities.test.helper;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;

public class TouchTestCapability
    extends CapabilitySupport
    implements Capability
{

    public static final String TYPE = "TouchTest";

    protected TouchTestCapability( final CapabilityContext context )
    {
        super( context );
    }

    @Override
    public void onCreate( Map<String, String> properties )
    {
        getLogger().info( "Create capability with id {} and properties {}", context().id(), properties );
    }

    @Override
    public void onUpdate( Map<String, String> properties )
    {
        getLogger().info( "Update capability with id {} and properties {}", context().id(), properties );
    }

    @Override
    public void onLoad( Map<String, String> properties )
    {
        getLogger().info( "Load capability with id {} and properties {}", context().id(), properties );
    }

    @Override
    public void onRemove()
    {
        getLogger().info( "Remove capability with id {}", context().id() );
    }

    @Override
    public String status()
    {
        return "<h3>I'm well. Thanx!</h3>";
    }

}
