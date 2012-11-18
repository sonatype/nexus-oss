/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.capabilities;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityBooterSupport;

/**
 * Automatically create Yum capability.
 *
 * @since 3.0
 */
@Named
@Singleton
public class YumCapabilitiesBooter
    extends CapabilityBooterSupport
{

    @Override
    protected void boot( final CapabilityRegistry registry )
        throws Exception
    {
        maybeAddCapability( registry, YumCapabilityDescriptor.TYPE, true, null, null );
    }

}
