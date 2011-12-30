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
import static org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder.capabilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.Validator;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;

/**
 * A {@link org.sonatype.nexus.plugins.capabilities.api.Validator} that ensures that only one capability of specified type and set of properties can be
 * created.
 *
 * @since 1.10.0
 */
@Named
public class PrimaryKeyValidator
    implements Validator
{

    private final CapabilityRegistry capabilityRegistry;

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    private final CapabilityType type;

    private final CapabilityIdentity excludeId;

    private final String[] propertyKeys;

    @Inject
    PrimaryKeyValidator( final CapabilityRegistry capabilityRegistry,
                         final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                         final @Assisted CapabilityType type,
                         final @Assisted String... propertyKeys )
    {
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.capabilityDescriptorRegistry = checkNotNull( capabilityDescriptorRegistry );
        this.type = checkNotNull( type );
        this.excludeId = null;
        this.propertyKeys = propertyKeys;
    }

    PrimaryKeyValidator( final CapabilityRegistry capabilityRegistry,
                         final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                         final CapabilityType type,
                         final CapabilityIdentity excludeId,
                         final String... propertyKeys )
    {
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.capabilityDescriptorRegistry = checkNotNull( capabilityDescriptorRegistry );
        this.type = checkNotNull( type );
        this.excludeId = checkNotNull( excludeId );
        this.propertyKeys = propertyKeys;
    }

    @Override
    public Set<Violation> validate( final Map<String, String> properties )
    {
        final Collection<CapabilityReference> references = capabilityRegistry.get( buildFilter( properties ) );
        if ( references == null
            || references.isEmpty()
            || ( references.size() == 1 && references.iterator().next().capability().id().equals( excludeId ) ) )
        {
            return null;
        }
        return Sets.<Violation>newHashSet( new DefaultViolation( type, buildMessage( properties ) ) );
    }

    private String buildMessage( final Map<String, String> properties )
    {
        final CapabilityDescriptor descriptor = capabilityDescriptorRegistry.get( type );
        final StringBuilder message = new StringBuilder()
            .append( "Only one capability of type '" ).append( descriptor.name() ).append( "'" );

        if ( properties != null )
        {
            final Map<String, String> keyToName = extractNames( descriptor );
            for ( final String key : propertyKeys )
            {
                String name = keyToName.get( key );
                if ( name == null )
                {
                    name = key;
                }
                String value = properties.get( key );
                if ( value.startsWith( "repo_" ) )
                {
                    value = value.replaceFirst( "repo_", "" );
                }
                else if ( value.startsWith( "group_" ) )
                {
                    value = value.replaceFirst( "group_", "" );
                }
                message.append( ", " ).append( name.toLowerCase() ).append( " '" ).append( value ).append( "'" );
            }
        }
        message.append( " can be created" );

        return message.toString();
    }

    private Predicate<CapabilityReference> buildFilter( final Map<String, String> properties )
    {
        final CapabilityReferenceFilterBuilder.CapabilityReferenceFilter filter = capabilities().withType(
            type
        );
        if ( propertyKeys != null )
        {
            for ( final String key : propertyKeys )
            {
                filter.withProperty( key, properties.get( key ) );
            }
        }
        return filter;
    }

    private Map<String, String> extractNames( final CapabilityDescriptor descriptor )
    {
        final Map<String, String> keyToName = Maps.newHashMap();
        final List<FormField> formFields = descriptor.formFields();
        if ( formFields != null )
        {
            for ( final FormField formField : formFields )
            {
                keyToName.put( formField.getId(), formField.getLabel() );
            }
        }
        return keyToName;
    }

}
