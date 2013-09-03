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

package org.sonatype.nexus.client.rest.jersey;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.client.core.Condition;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.core.spi.SubsystemProvider;

import com.google.common.collect.Lists;

/**
 * @since 2.1
 */
@Named
@Singleton
public class JerseyNexusClientFactory
    extends NexusClientFactoryImpl
{

  public JerseyNexusClientFactory(final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories) {
    super(adapt(subsystemFactories));
  }

  public JerseyNexusClientFactory(final Condition connectionCondition,
                                  final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories)
  {
    super(connectionCondition, adapt(subsystemFactories));
  }


  public JerseyNexusClientFactory(final Set<SubsystemFactory<?, JerseyNexusClient>> subsystemFactories) {
    super(adapt(subsystemFactories.toArray(new SubsystemFactory[subsystemFactories.size()])));
  }

  @Inject
  public JerseyNexusClientFactory(final Set<SubsystemFactory<?, JerseyNexusClient>> subsystemFactories,
                                  final List<SubsystemProvider> subsystemProviders)
  {
    super(combine(subsystemFactories, subsystemProviders));
  }

  /**
   * Adapts a set of {@link SubsystemFactory}s to a list of {@link SubsystemProvider}s.
   *
   * @param subsystemFactories to adapt (can be null)
   * @return adapted (never null, can be empty if provided subsystem factories is empty)
   */
  private static List<SubsystemProvider> adapt(
      @Nullable final SubsystemFactory<?, JerseyNexusClient>[] subsystemFactories)
  {
    final List<SubsystemProvider> providers = Lists.newArrayList();
    if (subsystemFactories != null) {
      for (final SubsystemFactory<?, JerseyNexusClient> subsystemFactory : subsystemFactories) {
        providers.add(
            new SubsystemProvider()
            {
              @Override
              public Object get(final Class type, final Map<Object, Object> context) {
                if (type.equals(subsystemFactory.getType())) {
                  return subsystemFactory.create((JerseyNexusClient) context.get(NexusClient.class));
                }
                return null;
              }
            }
        );
      }
    }
    return providers;
  }

  /**
   * Combines a set of {@link SubsystemFactory}s and a list of {@link SubsystemProvider}s in a list of
   * {@link SubsystemProvider}s, adapting the {@link SubsystemFactory}s as {@link SubsystemProvider}s.
   *
   * @param subsystemFactories to combine (can be null)
   * @param subsystemProviders to combine (can be null)
   * @return combined list (never null)
   */
  private static List<SubsystemProvider> combine(
      @Nullable final Set<SubsystemFactory<?, JerseyNexusClient>> subsystemFactories,
      @Nullable final List<SubsystemProvider> subsystemProviders)
  {

    final List<SubsystemProvider> providers = Lists.newArrayList();
    if (subsystemFactories != null) {
      providers.addAll(adapt(subsystemFactories.toArray(new SubsystemFactory[subsystemFactories.size()])));
    }
    if (subsystemProviders != null) {
      providers.addAll(subsystemProviders);
    }
    return providers;
  }

}
