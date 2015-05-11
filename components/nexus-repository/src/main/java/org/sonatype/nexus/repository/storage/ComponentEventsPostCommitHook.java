/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import java.util.List;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Component eventing related {@link StorageTxPostCommitHook}.
 *
 * @since 3.0
 */
public class ComponentEventsPostCommitHook
    extends StorageTxPostCommitHook
{
  private final EventBus eventBus;

  private final Repository repository;

  final List<ComponentEvent> events;

  public ComponentEventsPostCommitHook(final EventBus eventBus, final Repository repository) {
    this.eventBus = checkNotNull(eventBus);
    this.repository = checkNotNull(repository);
    this.events = Lists.newArrayList();
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
  public void execute() {
    for (ComponentEvent event : events) {
      eventBus.post(event);
    }
  }
}
