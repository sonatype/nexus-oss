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

import java.util.Set;

import org.sonatype.nexus.plugins.capabilities.api.ValidationResult;
import com.google.common.collect.Sets;

/**
 * Default {@link ValidationResult} implementation.
 *
 * @since 2.0
 */
public class DefaultValidationResult
    implements ValidationResult
{

    private Set<Violation> violations;

    public DefaultValidationResult()
    {
        violations = Sets.newHashSet();
    }

    @Override
    public boolean isValid()
    {
        return violations.isEmpty();
    }

    @Override
    public Set<Violation> violations()
    {
        return violations;
    }

    public DefaultValidationResult add( final Violation violation )
    {
        violations().add( violation );

        return this;
    }

    public void add( final Set<Violation> violations )
    {
        violations().addAll( violations );
    }

    public DefaultValidationResult add( final String message )
    {
        return add( new DefaultViolation( message ) );
    }

}
