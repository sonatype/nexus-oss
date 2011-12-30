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
package org.sonatype.nexus.plugins.capabilities.support.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.plugins.capabilities.api.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.Validator;

/**
 * Default {@link org.sonatype.nexus.plugins.capabilities.api.Validator.Violation} implementation.
 *
 * @since 1.10.0
 */
public class DefaultViolation
    implements Validator.Violation
{

    private final CapabilityType type;

    private final String message;

    private final CapabilityIdentity id;

    private final String property;

    public DefaultViolation( final CapabilityType type,
                             final String message )
    {
        this.type = checkNotNull( type );
        this.id = null;
        this.property = null;
        this.message = checkNotNull( message );
    }

    public DefaultViolation( final CapabilityType type,
                             final CapabilityIdentity id,
                             final String message )
    {
        this.type = checkNotNull( type );
        this.id = checkNotNull( id );
        this.property = null;
        this.message = checkNotNull( message );
    }

    public DefaultViolation( final CapabilityType type,
                             final CapabilityIdentity id,
                             final String property,
                             final String message )
    {
        this.type = checkNotNull( type );
        this.id = checkNotNull( id );
        this.property = checkNotNull( property );
        this.message = checkNotNull( message );
    }

    @Override
    public CapabilityType type()
    {
        return type;
    }

    @Override
    public CapabilityIdentity id()
    {
        return id;
    }

    @Override
    public String message()
    {
        return message;
    }

    @Override
    public String property()
    {
        return property;
    }

}
