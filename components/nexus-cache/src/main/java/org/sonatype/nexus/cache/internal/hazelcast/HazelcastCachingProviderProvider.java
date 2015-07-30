/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.cache.internal.hazelcast;

import javax.cache.spi.CachingProvider;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.core.HazelcastInstance;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hazelcast JCache {@link CachingProvider} provider.
 *
 * @since 3.0
 */
@Named("hazelcast")
@Singleton
public class HazelcastCachingProviderProvider
    extends ComponentSupport
    implements Provider<CachingProvider>
{
  private final CachingProvider provider;

  @Inject
  public HazelcastCachingProviderProvider(final HazelcastInstance hazelcastInstance) {
    checkNotNull(hazelcastInstance);
    this.provider = HazelcastServerCachingProvider.createCachingProvider(hazelcastInstance);
  }

  @Override
  public CachingProvider get() {
    return provider;
  }
}
