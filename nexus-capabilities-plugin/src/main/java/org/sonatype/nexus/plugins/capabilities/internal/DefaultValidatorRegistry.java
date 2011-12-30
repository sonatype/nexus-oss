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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityValidator;
import org.sonatype.nexus.plugins.capabilities.api.Validator;
import org.sonatype.nexus.plugins.capabilities.api.ValidatorRegistry;
import com.google.common.collect.Sets;

/**
 * Default {@link ValidatorRegistry} implementation.
 */
@Named
@Singleton
class DefaultValidatorRegistry
    implements ValidatorRegistry
{

    private final CapabilityRegistry capabilityRegistry;

    private final CapabilityFactoryRegistry capabilityFactoryRegistry;

    private final Map<String, CapabilityValidator> capabilityValidators;

    @Inject
    DefaultValidatorRegistry( final CapabilityFactoryRegistry capabilityFactoryRegistry,
                              final CapabilityRegistry capabilityRegistry,
                              final Map<String, CapabilityValidator> capabilityValidators )
    {
        this.capabilityFactoryRegistry = checkNotNull( capabilityFactoryRegistry );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.capabilityValidators = checkNotNull( capabilityValidators );
    }

    @Override
    public Collection<Validator> get( final CapabilityType type )
    {
        final Set<Validator> validators = Sets.newHashSet();

        final CapabilityFactory factory = capabilityFactoryRegistry.get( type );
        if ( factory != null && factory instanceof Validator )
        {
            validators.add( (Validator) factory );
        }

        final CapabilityValidator capabilityValidator = capabilityValidators.get( type.toString() );
        if ( capabilityValidator != null )
        {
            final Validator validator = capabilityValidator.validator();
            if ( validator != null )
            {
                validators.add( validator );
            }
        }

        return validators;
    }

    @Override
    public Collection<Validator> get( final CapabilityIdentity id )
    {
        final Set<Validator> validators = Sets.newHashSet();

        final CapabilityReference reference = capabilityRegistry.get( id );
        if ( reference != null && reference.capability() instanceof Validator )
        {
            validators.add( (Validator) reference.capability() );
        }

        if ( reference != null )
        {
            final CapabilityValidator capabilityValidator =
                capabilityValidators.get( reference.capabilityType().toString() );
            if ( capabilityValidator != null )
            {
                final Validator validator = capabilityValidator.validator( id );
                if ( validator != null )
                {
                    validators.add( validator );
                }
            }
        }

        return validators;
    }

}
