package de.is24.nexus.yum.plugin.event;

import org.sonatype.nexus.proxy.events.RepositoryEvent;
import org.sonatype.nexus.proxy.repository.Repository;

public class YumRepositoryGenerateEvent extends RepositoryEvent {

  public YumRepositoryGenerateEvent(Repository repository) {
    super(repository);
  }

}
