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
package com.sonatype.nexus.repository.nuget.internal.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.sonatype.nexus.repository.nuget.security.NugetApiKeyStore;

import org.sonatype.nexus.common.app.NexusStartedEvent;
import org.sonatype.nexus.common.app.NexusStoppedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleManagerImpl;

import com.google.common.eventbus.Subscribe;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the lifecycle for the {@link NugetApiKeyStoreImpl}.
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class NugetApiKeyLifecycle
    extends LifecycleManagerImpl
{
  private final EventBus eventBus;

  private final Provider<NugetApiKeyStore> keyStore;

  @Inject
  public NugetApiKeyLifecycle(final EventBus eventBus, final Provider<NugetApiKeyStore> keyStore)
  {
    this.eventBus = checkNotNull(eventBus);
    this.keyStore = checkNotNull(keyStore);

    eventBus.register(this);
  }

  @Subscribe
  public void on(final NexusStartedEvent event) throws Exception {
    add(keyStore.get());

    start();
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) throws Exception {
    eventBus.unregister(this);

    stop();
    clear();
  }
}
