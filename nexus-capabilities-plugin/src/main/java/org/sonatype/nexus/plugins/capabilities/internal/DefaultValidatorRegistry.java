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
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.plugins.capabilities.ValidatorRegistry;
import org.sonatype.nexus.plugins.capabilities.support.validator.Validators;
import com.google.common.collect.Sets;

/**
 * Default {@link ValidatorRegistry} implementation.
 */
@Named
@Singleton
class DefaultValidatorRegistry
    implements ValidatorRegistry
{

    private final DefaultCapabilityRegistry capabilityRegistry;

    private final CapabilityFactoryRegistry capabilityFactoryRegistry;

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    private final Validators validators;

    @Inject
    DefaultValidatorRegistry( final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                              final CapabilityFactoryRegistry capabilityFactoryRegistry,
                              final DefaultCapabilityRegistry capabilityRegistry,
                              final Validators validators )
    {
        this.capabilityDescriptorRegistry = checkNotNull( capabilityDescriptorRegistry );
        this.capabilityFactoryRegistry = checkNotNull( capabilityFactoryRegistry );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.validators = checkNotNull( validators );
    }

    @Override
    public Collection<Validator> get( final CapabilityType type )
    {
        final Set<Validator> typeValidators = Sets.newHashSet();

        final CapabilityDescriptor descriptor = capabilityDescriptorRegistry.get( type );
        if ( descriptor != null )
        {
            typeValidators.add( validators.capability().constraintsOf( type ) );

            final Validator validator = descriptor.validator();
            if ( validator != null )
            {
                typeValidators.add( validator );
            }

            final CapabilityFactory factory = capabilityFactoryRegistry.get( type );
            if ( factory != null )
            {
                if ( factory instanceof Validator )
                {
                    typeValidators.add( (Validator) factory );
                }
            }

        }

        return typeValidators;
    }

    @Override
    public Collection<Validator> get( final CapabilityIdentity id )
    {
        final Set<Validator> instanceValidators = Sets.newHashSet();

        final DefaultCapabilityReference reference = capabilityRegistry.get( id );
        if ( reference != null )
        {
            instanceValidators.add( validators.capability().constraintsOf( reference.context().type() ) );

            final CapabilityDescriptor descriptor = capabilityDescriptorRegistry.get( reference.context().type() );
            if ( descriptor != null )
            {
                final Validator validator = descriptor.validator( id );
                if ( validator != null )
                {
                    instanceValidators.add( validator );
                }
            }

            if ( reference.capability() instanceof Validator )
            {
                instanceValidators.add( (Validator) reference.capability() );
            }
        }

        return instanceValidators;
    }

}
