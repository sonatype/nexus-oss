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

package org.sonatype.nexus.proxy.repository.threads;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.Event;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maintains the ThreadPoolManager based on Nexus events.
 *
 * @author cstamas
 */
@Singleton
@Named
public class ThreadPoolManagerEventInspector
    extends AbstractEventInspector
{
  private final ThreadPoolManager poolManager;

  @Inject
  public ThreadPoolManagerEventInspector(final ThreadPoolManager poolManager) {
    this.poolManager = checkNotNull(poolManager);
  }

  @Override
  public boolean accepts(Event<?> evt) {
    return evt != null && evt instanceof RepositoryRegistryRepositoryEvent;
    // return evt != null && ( evt instanceof RepositoryRegistryRepositoryEvent || evt instanceof NexusStoppedEvent );
  }

  @Override
  public void inspect(Event<?> evt) {
    if (!accepts(evt)) {
      return;
    }

    if (evt instanceof RepositoryRegistryEventAdd) {
      poolManager.createPool(((RepositoryRegistryEventAdd) evt).getRepository());

    }
    else if (evt instanceof RepositoryRegistryEventRemove) {
      poolManager.removePool(((RepositoryRegistryEventRemove) evt).getRepository());
    }
    // else if ( evt instanceof NexusStoppedEvent )
    // {
    // poolManager.shutdown();
    // }
  }
}
