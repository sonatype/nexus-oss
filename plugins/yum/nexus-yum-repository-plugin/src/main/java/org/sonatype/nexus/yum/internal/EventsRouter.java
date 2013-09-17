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

package org.sonatype.nexus.yum.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.internal.task.MergeMetadataTask;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since yum 3.0
 */
@Named
@Singleton
@EventBus.Managed
public class EventsRouter
{

  private final Provider<RepositoryRegistry> repositoryRegistry;

  private final Provider<YumRegistry> yumRegistryProvider;

  private final Provider<NexusScheduler> nexusScheduler;

  private final Provider<SteadyLinksRequestStrategy> steadyLinksStrategy;

  @Inject
  public EventsRouter(final Provider<RepositoryRegistry> repositoryRegistry,
                      final Provider<YumRegistry> yumRegistryProvider,
                      final Provider<NexusScheduler> nexusScheduler,
                      final Provider<SteadyLinksRequestStrategy> steadyLinksStrategy)
  {
    this.steadyLinksStrategy = checkNotNull(steadyLinksStrategy);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    this.yumRegistryProvider = checkNotNull(yumRegistryProvider);
    this.nexusScheduler = checkNotNull(nexusScheduler);
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final RepositoryRegistryEventAdd event) {
    event.getRepository().registerRequestStrategy(
        SteadyLinksRequestStrategy.class.getName(), steadyLinksStrategy.get()
    );
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final RepositoryRegistryEventRemove event) {
    event.getRepository().unregisterRequestStrategy(
        SteadyLinksRequestStrategy.class.getName()
    );
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final RepositoryGroupMembersChangedEvent event) {
    if (yumRegistryProvider.get().isRegistered(event.getGroupRepository().getId())
        && (anyOfRepositoriesHasYumRepository(event.getAddedRepositoryIds())
        || anyOfRepositoriesHasYumRepository(event.getRemovedRepositoryIds())
        || anyOfRepositoriesHasYumRepository(event.getReorderedRepositoryIds()))) {
      MergeMetadataTask.createTaskFor(nexusScheduler.get(), event.getGroupRepository());
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final RepositoryItemEventStore eventStore) {
    if (isRpmItemEvent(eventStore)) {
      final Yum yum = yumRegistryProvider.get().get(eventStore.getRepository().getId());
      if (yum != null) {
        yum.markDirty(getItemVersion(eventStore.getItem()));
        yum.addRpmAndRegenerate(eventStore.getItem().getPath());
      }
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(RepositoryItemEventDelete itemEvent) {
    final Yum yum = yumRegistryProvider.get().get(itemEvent.getRepository().getId());
    if (yum != null) {
      if (isRpmItemEvent(itemEvent)) {
        yum.regenerateWhenPathIsRemoved(itemEvent.getItem().getPath());
      }
      else if (isCollectionItem(itemEvent)) {
        yum.regenerateWhenDirectoryIsRemoved(itemEvent.getItem().getPath());
      }
    }
  }

  private boolean isCollectionItem(RepositoryItemEvent itemEvent) {
    return StorageCollectionItem.class.isAssignableFrom(itemEvent.getItem().getClass());
  }

  private boolean isRpmItemEvent(RepositoryItemEvent itemEvent) {
    return yumRegistryProvider.get().isRegistered(itemEvent.getRepository().getId())
        && !itemEvent.getItem().getRepositoryItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)
        && itemEvent.getItem().getPath().toLowerCase().endsWith(".rpm");
  }

  private String getItemVersion(StorageItem item) {
    String[] parts = item.getParentPath().split("/");
    return parts[parts.length - 1];
  }

  private boolean anyOfRepositoriesHasYumRepository(final List<String> repositoryIds) {
    if (repositoryIds != null) {
      for (final String repositoryId : repositoryIds) {
        try {
          repositoryRegistry.get().getRepository(repositoryId).retrieveItem(
              new ResourceStoreRequest(Yum.PATH_OF_REPOMD_XML)
          );
          return true;
        }
        catch (final Exception ignore) {
          // we could not get the repository or repomd.xml so looks like we do not have an yum repository
        }
      }
    }
    return false;
  }

}
