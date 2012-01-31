package de.is24.nexus.yum.plugin.impl;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

import de.is24.nexus.yum.plugin.AbstractEventListener;
import de.is24.nexus.yum.plugin.ItemEventListener;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.service.RepositoryRpmManager;
import de.is24.nexus.yum.service.YumService;


@Component(role = ItemEventListener.class, instantiationStrategy = Strategies.LOAD_ON_START)
public class RpmDeployedOrRemovedListener extends AbstractEventListener {
  private static final Logger LOG = LoggerFactory.getLogger(ItemEventListener.class);

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private YumService yumService;

  @Inject
  private RepositoryRpmManager repositoryRpmManager;

  @Override
  public void onEvent(Event<?> evt) {
    if (evt instanceof RepositoryItemEventStore) {
      processRepositoryItemAdd((RepositoryItemEventStore) evt);
    } else if (evt instanceof RepositoryRegistryEventAdd) {
      processRepository(((RepositoryRegistryEventAdd) evt).getRepository());
    } else if (evt instanceof RepositoryItemEventDelete) {
      processRepositoryItemDelete((RepositoryItemEventDelete) evt);
    }
  }

  private void processRepositoryItemDelete(RepositoryItemEventDelete itemEvent) {
    if (isRpmItemEvent(itemEvent)) {
      LOG.info("ItemDeleteEvent : {}", itemEvent.getItem().getPath());
      yumService.removeFromRepository(itemEvent.getRepository(), itemEvent.getItem().getPath());
    }
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
      repositoryRpmManager.updateRepository(itemEvent.getRepository().getId(), getItemVersion(itemEvent.getItem()));
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
