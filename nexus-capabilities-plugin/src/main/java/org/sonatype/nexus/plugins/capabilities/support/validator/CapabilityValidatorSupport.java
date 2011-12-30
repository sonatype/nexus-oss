/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.capabilities.support.validator;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityValidator;
import org.sonatype.nexus.plugins.capabilities.api.ValidationResult;

/**
 * {@link CapabilityValidator} support.
 *
 * @since 1.0
 */
public class CapabilityValidatorSupport
    implements CapabilityValidator
{

    @Override
    public ValidationResult validate( final Map<String, String> properties )
    {
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate( final CapabilityIdentity id, final Map<String, String> properties )
    {
        return ValidationResult.VALID;
    }

}
