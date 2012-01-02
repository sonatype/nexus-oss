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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import javax.inject.Inject;

import org.sonatype.nexus.plugins.capabilities.api.ValidationResult;
import org.sonatype.nexus.plugins.capabilities.api.Validator;
import org.sonatype.nexus.plugins.capabilities.support.validator.DefaultValidationResult;

/**
 * Logical OR between {@link Validator}s.
 *
 * @since 2.0
 */
public class DisjunctionValidator
    implements Validator
{

    private final Validator[] validators;

    @Inject
    public DisjunctionValidator( final Validator... validators )
    {
        this.validators = checkNotNull( validators );
        checkArgument( validators.length > 0, "There must be at least one validator" );
        for ( final Validator validator : validators )
        {
            checkNotNull( validator );
        }
    }

    @Override
    public ValidationResult validate( final Map<String, String> properties )
    {
        final DefaultValidationResult failed = new DefaultValidationResult();
        for ( final Validator validator : validators )
        {
            final ValidationResult validationResult = validator.validate( properties );
            if ( validationResult.isValid() )
            {
                return ValidationResult.VALID;
            }
            failed.add( validationResult.violations() );
        }
        return failed;
    }

    @Override
    public String explainValid()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "One of following is valid: " );
        for ( final Validator validator : validators )
        {
            if ( sb.length() > 0 )
            {
                sb.append( " OR " );
            }
            sb.append( validator.explainValid() );
        }
        return sb.toString();
    }

    @Override
    public String explainInvalid()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "All of following is invalid: " );
        for ( final Validator validator : validators )
        {
            if ( sb.length() > 0 )
            {
                sb.append( " AND " );
            }
            sb.append( validator.explainValid() );
        }
        return sb.toString();
    }

}
