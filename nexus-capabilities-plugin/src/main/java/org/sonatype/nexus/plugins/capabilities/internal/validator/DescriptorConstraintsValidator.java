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
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.ValidationResult;
import org.sonatype.nexus.plugins.capabilities.api.Validator;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.support.validator.Validators;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;

/**
 * A {@link Validator} that ensures that constraints expressed by descriptor fields are satisfied.
 *
 * @since 2.0
 */
@Named
public class DescriptorConstraintsValidator
    extends ValidatorSupport
    implements Validator
{

    private final Validator validator;

    @Inject
    DescriptorConstraintsValidator( final Validators validators,
                                    final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                                    final @Assisted CapabilityType type )
    {
        super( capabilityDescriptorRegistry, type );
        checkNotNull( validators );
        Validator descriptorValidator = validators.capability().alwaysValid();
        final List<FormField> formFields = capabilityDescriptor().formFields();
        if ( formFields != null )
        {
            final List<Validator> fieldValidators = Lists.newArrayList();
            for ( final FormField formField : formFields )
            {
                if ( formField.isRequired() )
                {
                    fieldValidators.add( validators.capability().required( type, formField.getId() ) );
                }
                final String regexp = formField.getRegexValidation();
                if ( regexp != null && regexp.trim().length() > 0 )
                {
                    fieldValidators.add( validators.capability().matches( type, formField.getId(), regexp ) );
                }
            }
            if ( fieldValidators != null )
            {
                descriptorValidator = validators.logical().and(
                    fieldValidators.toArray( new Validator[fieldValidators.size()] )
                );
            }
        }
        validator = descriptorValidator;
    }

    @Override
    public ValidationResult validate( final Map<String, String> properties )
    {
        return validator.validate( properties );
    }

    @Override
    public String explainValid()
    {
        return validator.explainValid();
    }

    @Override
    public String explainInvalid()
    {
        return validator.explainInvalid();
    }

}
