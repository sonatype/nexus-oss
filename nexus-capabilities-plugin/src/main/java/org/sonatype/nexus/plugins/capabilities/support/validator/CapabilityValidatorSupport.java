/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.capabilities.support.validator;

import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityValidator;
import org.sonatype.nexus.plugins.capabilities.Validator;

/**
 * {@link CapabilityValidator} support.
 *
 * @since 1.0
 */
public class CapabilityValidatorSupport
    implements CapabilityValidator
{

    static final Validator NO_VALIDATOR = null;

    @Override
    public Validator validator()
    {
        return NO_VALIDATOR;
    }

    @Override
    public Validator validator( final CapabilityIdentity id )
    {
        return NO_VALIDATOR;
    }

}
