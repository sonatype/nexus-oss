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

package org.sonatype.nexus.capabilities.client.support;

import java.lang.reflect.Proxy;

import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.capabilities.client.internal.JerseyReflectiveCapability;
import org.sonatype.nexus.capabilities.client.spi.CapabilityType;
import org.sonatype.nexus.capabilities.client.spi.JerseyCapabilityFactory;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link JerseyCapabilityFactory} that implements capabilities via reflection.
 *
 * @since 2.2
 */
public class JerseyReflectiveCapabilityFactory<C extends Capability>
    implements JerseyCapabilityFactory<C>
{

  private final Class<C> type;

  final CapabilityType capabilityType;

  public JerseyReflectiveCapabilityFactory(final Class<C> type) {
    this.type = checkNotNull(type);
    capabilityType = type.getAnnotation(CapabilityType.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public C create(final JerseyNexusClient nexusClient) {
    return (C) Proxy.newProxyInstance(
        type.getClassLoader(),
        new Class[]{type},
        new JerseyReflectiveCapability(type, nexusClient, capabilityType.value())
    );
  }

  @SuppressWarnings("unchecked")
  @Override
  public C create(final JerseyNexusClient nexusClient, final CapabilityListItemResource resource) {
    return (C) Proxy.newProxyInstance(
        type.getClassLoader(),
        new Class[]{type},
        new JerseyReflectiveCapability(type, nexusClient, resource)
    );
  }

  @Override
  public boolean canCreate(final String type) {
    return capabilityType.value().equals(type);
  }

  @Override
  public boolean canCreate(final Class<Capability> type) {
    return this.type.equals(type);
  }

}
