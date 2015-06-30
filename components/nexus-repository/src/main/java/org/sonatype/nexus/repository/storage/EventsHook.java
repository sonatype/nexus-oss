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
package org.sonatype.nexus.repository.storage;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link StorageTxHook} that fires component events.
 *
 * @since 3.0
 */
public class EventsHook
    extends StorageTxHook
{
  private final EventBus eventBus;

  private final Repository repository;

  private final List<Object> events;

  public EventsHook(final EventBus eventBus, final Repository repository) {
    this.eventBus = checkNotNull(eventBus);
    this.repository = checkNotNull(repository);
    this.events = new ArrayList<>();
  }

  @Override
  public void createComponent(final Component... components) {
    for (Component component : components) {
      events.add(new ComponentCreatedEvent(component, repository));
    }
  }

  @Override
  public void updateComponent(final Component... components) {
    for (Component component : components) {
      events.add(new ComponentUpdatedEvent(component, repository));
    }
  }

  @Override
  public void deleteComponent(final Component... components) {
    for (Component component : components) {
      events.add(new ComponentDeletedEvent(component, repository));
    }
  }

  @Override
  public void createAsset(final Asset... assets) {
    for (Asset asset : assets) {
      events.add(new AssetCreatedEvent(asset, repository));
    }
  }

  @Override
  public void updateAsset(final Asset... assets) {
    for (Asset asset : assets) {
      events.add(new AssetUpdatedEvent(asset, repository));
    }
  }

  @Override
  public void deleteAsset(final Asset... assets) {
    for (Asset asset : assets) {
      events.add(new AssetDeletedEvent(asset, repository));
    }
  }

  @Override
  public void postCommit() {
    for (Object event : events) {
      eventBus.post(event);
    }
  }
}
