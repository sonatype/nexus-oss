/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.capabilities.client.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.capabilities.client.spi.JerseyCapabilityFactory;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.capabilities.client.CapabilityA;

/**
 * @since 2.2
 */
@Named
@Singleton
public class JerseyCapabilityAFactory
    implements JerseyCapabilityFactory<CapabilityA>
{

    public CapabilityA create( final JerseyNexusClient nexusClient )
    {
        return new JerseyCapabilityA( nexusClient );
    }

    @Override
    public CapabilityA create( final JerseyNexusClient nexusClient, final CapabilityListItemResource resource )
    {
        return new JerseyCapabilityA( nexusClient, resource );
    }

    public boolean canCreate( final String type )
    {
        return "[a]".equals( type );
    }

    public boolean canCreate( final Class<Capability> type )
    {
        return CapabilityA.class.equals( type );
    }

}
