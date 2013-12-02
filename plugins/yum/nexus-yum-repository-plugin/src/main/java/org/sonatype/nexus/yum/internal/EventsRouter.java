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

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumGroup;
import org.sonatype.nexus.yum.YumHosted;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.eclipse.sisu.EagerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since yum 3.0
 */
@Named
@EagerSingleton
public class EventsRouter
{

  private static final Logger log = LoggerFactory.getLogger(EventsRouter.class);

  private final Provider<RepositoryRegistry> repositoryRegistry;

  private final Provider<YumRegistry> yumRegistryProvider;

  @Inject
  public EventsRouter(final Provider<RepositoryRegistry> repositoryRegistry,
                      final Provider<YumRegistry> yumRegistryProvider,
                      final EventBus eventBus)
  {
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    this.yumRegistryProvider = checkNotNull(yumRegistryProvider);
    checkNotNull(eventBus).register(this);
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final RepositoryGroupMembersChangedEvent event) {
    if (yumRegistryProvider.get().isRegistered(event.getGroupRepository().getId())
        && (anyOfRepositoriesHasYumRepository(event.getAddedRepositoryIds())
        || anyOfRepositoriesHasYumRepository(event.getRemovedRepositoryIds())
        || anyOfRepositoriesHasYumRepository(event.getReorderedRepositoryIds()))) {

      Yum yum = yumRegistryProvider.get().get(event.getGroupRepository().getId());
      if (yum instanceof YumGroup) {
        ((YumGroup) yum).markDirty();
      }
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final RepositoryItemEventStore eventStore) {
    if (isRpmItemEvent(eventStore)) {
      final Yum yum = yumRegistryProvider.get().get(eventStore.getRepository().getId());
      if (yum != null && yum instanceof YumHosted) {
        ((YumHosted) yum).markDirty(getItemVersion(eventStore.getItem()));
        ((YumHosted) yum).addRpmAndRegenerate(eventStore.getItem().getPath());
      }
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(RepositoryItemEventDelete itemEvent) {
    final Yum yum = yumRegistryProvider.get().get(itemEvent.getRepository().getId());
    if (yum != null && yum instanceof YumHosted) {
      if (isRpmItemEvent(itemEvent)) {
        ((YumHosted) yum).regenerateWhenPathIsRemoved(itemEvent.getItem().getPath());
      }
      else if (isCollectionItem(itemEvent)) {
        ((YumHosted) yum).regenerateWhenDirectoryIsRemoved(itemEvent.getItem().getPath());
      }
    }
  }

  /**
   * Automatically merge group level metadata when "/repodata/repomd.xml" is (re)retrieved from proxy.
   *
   * @since 2.7
   */
  @AllowConcurrentEvents
  @Subscribe
  public void on(RepositoryItemEventCache itemEvent) {
    ProxyRepository repository = itemEvent.getRepository().adaptToFacet(ProxyRepository.class);
    if (repository != null && itemEvent.getItem().getPath().toLowerCase().equals("/" + Yum.PATH_OF_REPOMD_XML)) {
      log.debug("Yum metadata changed for {}. Looking if we should merge it...", repository.getId());
      List<GroupRepository> groups = repositoryRegistry.get().getGroupsOfRepository(repository);
      for (GroupRepository group : groups) {
        Yum yum = yumRegistryProvider.get().get(group.getId());
        if (yum != null && yum instanceof YumGroup) {
          ((YumGroup) yum).markDirty();
        }
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
