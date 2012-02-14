package de.is24.nexus.yum.plugin.impl;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

import de.is24.nexus.yum.plugin.YumRepositoryGenerateEventListener;
import de.is24.nexus.yum.plugin.event.YumRepositoryGenerateEvent;
import de.is24.nexus.yum.plugin.m2yum.M2YumGroupRepository;
import de.is24.nexus.yum.service.YumService;

@Component(role = YumRepositoryGenerateEventListener.class, instantiationStrategy = Strategies.LOAD_ON_START)
public class DefaultYumRepositoryGenerateEventListener implements YumRepositoryGenerateEventListener {

  @Requirement
  private RepositoryRegistry repositoryRegistry;

  @Requirement
  private YumService yumService;

  @Override
  public void onEvent(Event<?> evt) {
    if (evt instanceof YumRepositoryGenerateEvent) {
      final Repository repository = ((YumRepositoryGenerateEvent) evt).getRepository();
      for (GroupRepository groupRepository : repositoryRegistry.getGroupsOfRepository(repository)) {
        if (groupRepository.getRepositoryKind().isFacetAvailable(M2YumGroupRepository.class)) {
          yumService.createGroupRepository(groupRepository);
        }
      }
    }
  }

}
