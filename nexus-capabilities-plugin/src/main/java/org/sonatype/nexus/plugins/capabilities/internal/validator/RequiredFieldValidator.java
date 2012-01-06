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

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.ValidationResult;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.plugins.capabilities.support.validator.DefaultValidationResult;
import com.google.inject.assistedinject.Assisted;

/**
 * A {@link Validator} that ensures that a required field corresponding property is not null or empty.
 *
 * @since 2.0
 */
@Named
public class RequiredFieldValidator
    extends ValidatorSupport
    implements Validator
{

    private final String key;

    private final String label;

    @Inject
    RequiredFieldValidator( final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                            final @Assisted CapabilityType type,
                            final @Assisted String key )
    {
        super( capabilityDescriptorRegistry, type );
        this.key = checkNotNull( key );
        label = propertyName( key );
    }

    @Override
    public ValidationResult validate( final Map<String, String> properties )
    {
        if ( properties != null )
        {
            final String value = properties.get( key );
            if ( value == null || value.trim().length() == 0 )
            {
                return new DefaultValidationResult().add( key, label + " is required" );
            }
        }
        return ValidationResult.VALID;
    }

    @Override
    public String explainValid()
    {
        return label + " is not null or empty";
    }

    @Override
    public String explainInvalid()
    {
        return label + " is null or empty";
    }

}
