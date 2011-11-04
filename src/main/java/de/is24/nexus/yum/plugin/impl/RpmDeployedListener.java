package de.is24.nexus.yum.plugin.impl;

import javax.inject.Inject;
import javax.inject.Named;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.plexus.config.Strategies;
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
public class RpmDeployedListener extends AbstractEventListener {
  private static final Logger LOG = LoggerFactory.getLogger(ItemEventListener.class);

  @Inject
  @Named(RepositoryRegistry.DEFAULT_BEAN_NAME)
  private RepositoryRegistry repositoryRegistry;

  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
  private YumService yumService;

  @Inject
  @Named(RepositoryRpmManager.DEFAULT_BEAN_NAME)
  private RepositoryRpmManager repositoryRpmManager;

  public void onEvent(Event<?> evt) {
    if (evt instanceof RepositoryItemEventStore) {
      processRepositoryItem((RepositoryItemEventStore) evt);
    } else if (evt instanceof RepositoryRegistryEventAdd) {
      processRepository(((RepositoryRegistryEventAdd) evt).getRepository());
    }
  }

  private void processRepository(Repository repository) {
    if (repository.getRepositoryKind().isFacetAvailable(MavenHostedRepository.class)) {
      repositoryRegistry.registerRepository(MavenRepository.class.cast(repository));
    }
  }

  private void processRepositoryItem(RepositoryItemEventStore itemEvent) {
    if (repositoryRegistry.isRegistered(itemEvent.getRepository()) && itemEvent.getItem().getPath().endsWith(".rpm")) {
      LOG.info("ItemStoreEvent : {}", itemEvent.getItem().getPath());
      yumService.markDirty(itemEvent.getRepository(), getItemVersion(itemEvent.getItem()));
      yumService.addToYumRepository(itemEvent.getRepository(), itemEvent.getItem().getPath());
      repositoryRpmManager.updateRepository(itemEvent.getRepository().getId(), getItemVersion(itemEvent.getItem()));
    }
  }

  private String getItemVersion(StorageItem item) {
    String[] parts = item.getParentPath().split("/");
    return parts[parts.length - 1];
  }
}
