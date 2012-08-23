package org.sonatype.nexus.plugins.yum.plugin.impl;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.nexus.plugins.yum.plugin.AbstractEventListener;
import org.sonatype.nexus.plugins.yum.plugin.DeletionService;
import org.sonatype.nexus.plugins.yum.plugin.ItemEventListener;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.plugins.yum.plugin.event.YumRepositoryGenerateEvent;
import org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumGroupRepository;
import org.sonatype.nexus.plugins.yum.repository.service.YumService;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;


@Component(role = ItemEventListener.class, instantiationStrategy = Strategies.LOAD_ON_START)
public class RpmRepositoryEventListener extends AbstractEventListener {
  private static final Logger LOG = LoggerFactory.getLogger(ItemEventListener.class);

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private org.sonatype.nexus.proxy.registry.RepositoryRegistry nexusRepositoryRegistry;

  @Inject
  private YumService yumService;

  @Inject
  private DeletionService deletionService;

  @Override
  public void onEvent(Event<?> evt) {
    if (evt instanceof RepositoryItemEventStore) {
      processRepositoryItemAdd((RepositoryItemEventStore) evt);
    } else if (evt instanceof RepositoryRegistryEventAdd) {
      processRepository(((RepositoryRegistryEventAdd) evt).getRepository());
    } else if (evt instanceof RepositoryItemEventDelete) {
      processRepositoryItemDelete((RepositoryItemEventDelete) evt);
    } else if (evt instanceof YumRepositoryGenerateEvent) {
      final Repository repository = ((YumRepositoryGenerateEvent) evt).getRepository();
      for (GroupRepository groupRepository : nexusRepositoryRegistry.getGroupsOfRepository(repository)) {
        if (groupRepository.getRepositoryKind().isFacetAvailable(M2YumGroupRepository.class)) {
          yumService.createGroupRepository(groupRepository);
        }
      }
    }
  }

  private void processRepositoryItemDelete(RepositoryItemEventDelete itemEvent) {
    if (isRpmItemEvent(itemEvent)) {
      deletionService.deleteRpm(itemEvent.getRepository(), itemEvent.getItem().getPath());
    } else if (isCollectionItem(itemEvent)) {
      deletionService.deleteDirectory(itemEvent.getRepository(), itemEvent.getItem().getPath());
    }
  }

  private boolean isCollectionItem(RepositoryItemEvent itemEvent) {
    return StorageCollectionItem.class.isAssignableFrom(itemEvent.getItem().getClass());
  }

  private void processRepository(Repository repository) {
    if (repository.getRepositoryKind().isFacetAvailable(MavenHostedRepository.class)) {
      repositoryRegistry.registerRepository(MavenRepository.class.cast(repository));
    }
  }

  private void processRepositoryItemAdd(RepositoryItemEventStore itemEvent) {
    if (isRpmItemEvent(itemEvent)) {
      LOG.info("ItemStoreEvent : {}", itemEvent.getItem().getPath());
      yumService.markDirty(itemEvent.getRepository(), getItemVersion(itemEvent.getItem()));
      yumService.addToYumRepository(itemEvent.getRepository(), itemEvent.getItem().getPath());
    }
  }

  private boolean isRpmItemEvent(RepositoryItemEvent itemEvent) {
    return repositoryRegistry.isRegistered(itemEvent.getRepository()) && itemEvent.getItem().getPath().endsWith(".rpm");
  }

  private String getItemVersion(StorageItem item) {
    String[] parts = item.getParentPath().split("/");
    return parts[parts.length - 1];
  }
}
