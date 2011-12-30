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
package org.sonatype.nexus.plugins.capabilities.internal.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import com.google.common.collect.Maps;

/**
 * Validators support.
 *
 * @since 1.10.0
 */
class ValidatorSupport
{

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    private final CapabilityType type;

    private final CapabilityDescriptor descriptor;

    private final Map<String, String> keyToName;

    ValidatorSupport( final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                      final CapabilityType type )
    {
        this.capabilityDescriptorRegistry = checkNotNull( capabilityDescriptorRegistry );
        this.type = checkNotNull( type );
        this.descriptor = capabilityDescriptorRegistry.get( capabilityType() );
        this.keyToName = extractNames( descriptor );
    }

    CapabilityDescriptorRegistry capabilityDescriptorRegistry()
    {
        return capabilityDescriptorRegistry;
    }

    CapabilityType capabilityType()
    {
        return type;
    }

    CapabilityDescriptor capabilityDescriptor()
    {
        return descriptor;
    }

    String typeName()
    {
        final CapabilityDescriptor descriptor = capabilityDescriptor();
        if ( descriptor != null )
        {
            return descriptor.name();
        }
        return capabilityType().toString();
    }

    String propertyName( final String propertyKey )
    {
        String name = keyToName.get( propertyKey );
        if ( name == null )
        {
            name = propertyKey;
        }
        return name;
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

    String fixRepositoryValue( final String repositoryId )
    {
        if ( repositoryId == null )
        {
            return null;
        }
        String value = repositoryId;
        if ( value.startsWith( "repo_" ) )
        {
            value = value.replaceFirst( "repo_", "" );
        }
        else if ( value.startsWith( "group_" ) )
        {
            value = value.replaceFirst( "group_", "" );
        }
        return value;
    }


}
