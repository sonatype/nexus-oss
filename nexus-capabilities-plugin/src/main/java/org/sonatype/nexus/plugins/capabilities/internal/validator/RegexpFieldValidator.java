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
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.ValidationResult;
import org.sonatype.nexus.plugins.capabilities.api.Validator;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.support.validator.DefaultValidationResult;
import com.google.inject.assistedinject.Assisted;

/**
 * A {@link Validator} that ensures that a required field corresponding property matches a specified regex pattern.
 *
 * @since 2.0
 */
@Named
public class RegexpFieldValidator
    extends ValidatorSupport
    implements Validator
{

    private final String key;

    private final Pattern pattern;

    private final String label;

    @Inject
    RegexpFieldValidator( final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                          final @Assisted CapabilityType type,
                          final @Assisted( "key" ) String key,
                          final @Assisted( "regexp" ) String regexp )
    {
        super( capabilityDescriptorRegistry, type );
        this.key = checkNotNull( key );
        this.pattern = Pattern.compile( checkNotNull( regexp ) );
        label = propertyName( key );
    }

    @Override
    public ValidationResult validate( final Map<String, String> properties )
    {
        if ( properties != null )
        {
            final String value = properties.get( key );
            if ( value != null && pattern.matcher( value ).matches() )
            {
                return new DefaultValidationResult().add( label + " does not match '" + pattern.pattern() + "'" );
            }
        }
        return ValidationResult.VALID;
    }

    @Override
    public String explainValid()
    {
        return label + " matches '" + pattern.pattern() + "'";
    }

    @Override
    public String explainInvalid()
    {
        return label + " does not match '" + pattern.pattern() + "'";
    }

}
