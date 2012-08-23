package org.sonatype.nexus.plugins.yum.plugin.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.plugins.yum.repository.task.RepositoryScanningTask;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;


@Component(role = RepositoryRegistry.class)
public class DefaultRepositoryRegistry implements RepositoryRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRepositoryRegistry.class);

  private final Map<String, MavenRepositoryInfo> repositories = new ConcurrentHashMap<String, MavenRepositoryInfo>();

  @Requirement
  private NexusScheduler nexusScheduler;

  @Override
  public void registerRepository(MavenRepository repository) {
    if (!repositories.containsKey(repository.getId())) {
      MavenRepositoryInfo repositoryInfo = new MavenRepositoryInfo(repository);
      repositories.put(repository.getId(), repositoryInfo);
      LOG.info("Marked repository as RPM-repository : {}", repository.getId());
      runScanningTask(repositoryInfo);
    }
  }

  private void runScanningTask(MavenRepositoryInfo repositoryInfo) {
    RepositoryScanningTask task = nexusScheduler.createTaskInstance(RepositoryScanningTask.class);
    task.setMavenRepositoryInfo(repositoryInfo);
    nexusScheduler.submit(RepositoryScanningTask.ID, task);
  }

  @Override
  public boolean isRegistered(Repository repository) {
    return repositories.containsKey(repository.getId());
  }

  @Override
  public MavenRepository findRepositoryForId(final String repositoryId) {
    final MavenRepositoryInfo repositoryInfo = findRepositoryInfoForId(repositoryId);
    if (repositoryInfo == null) {
      return null;
    }
    return repositoryInfo.getRepository();
  }

  @Override
  public MavenRepositoryInfo findRepositoryInfoForId(final String repositoryId) {
    return repositories.get(repositoryId);
  }

  @Override
  public void unregisterRepository(Repository repository) {
    repositories.remove(repository.getId());
  }

}
