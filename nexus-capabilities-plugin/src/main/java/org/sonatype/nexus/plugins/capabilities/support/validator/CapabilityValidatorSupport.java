/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.capabilities.support.validator;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.api.Validator.Violation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityValidator;

/**
 * {@link CapabilityValidator} support.
 *
 * @since 1.0
 */
public class CapabilityValidatorSupport
    implements CapabilityValidator
{
    
    private static final Set<Violation> ALWAYS_VALID = Collections.emptySet();

    @Override
    public Set<Violation> validate( final Map<String, String> properties )
    {
        return ALWAYS_VALID;
    }

    @Override
    public Set<Violation> validate( final CapabilityIdentity id, final Map<String, String> properties )
    {
        return ALWAYS_VALID;
    }

}
