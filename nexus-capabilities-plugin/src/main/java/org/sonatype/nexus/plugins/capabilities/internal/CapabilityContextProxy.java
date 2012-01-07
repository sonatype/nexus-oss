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
package org.sonatype.nexus.plugins.capabilities.internal;

import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;

/**
 * A {@link CapabilityContext} that delegates to another mutable context.
 *
 * @since 2.0
 */
class CapabilityContextProxy
    implements CapabilityContext
{

    private CapabilityContext delegate;

    CapabilityContextProxy( final CapabilityContext delegate )
    {
        this.delegate = checkNotNull( delegate );
    }

    public void setCapabilityContext( final CapabilityContext delegate )
    {
        this.delegate = checkNotNull( delegate );
    }

    @Override
    public CapabilityIdentity id()
    {
        return delegate.id();
    }

    @Override
    public CapabilityType type()
    {
        return delegate.type();
    }

    @Override
    public CapabilityDescriptor descriptor()
    {
        return delegate.descriptor();
    }

    @Override
    public String notes()
    {
        return delegate.notes();
    }

    @Override
    public Map<String, String> properties()
    {
        return delegate.properties();
    }

    @Override
    public boolean isEnabled()
    {
        return delegate.isEnabled();
    }

    @Override
    public boolean isActive()
    {
        return delegate.isActive();
    }

    @Override
    public boolean hasFailure()
    {
        return delegate.hasFailure();
    }

    @Override
    public Exception failure()
    {
        return delegate.failure();
    }

    @Override
    public String stateDescription()
    {
        return delegate.stateDescription();
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

}
