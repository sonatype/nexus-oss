package de.is24.nexus.yum.plugin.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.repository.RepositoryScanningJob;
import de.is24.nexus.yum.service.RepositoryRpmManager;


@Component(role = RepositoryRegistry.class)
public class DefaultRepositoryRegistry implements RepositoryRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRepositoryRegistry.class);

  private static final int REPOSITORY_SCANNING_THREAD_POOL_SIZE = 3;

  private final Map<String, MavenRepositoryInfo> repositories = new ConcurrentHashMap<String, MavenRepositoryInfo>();

  private final ThreadPoolExecutor repositoryScanningExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
    REPOSITORY_SCANNING_THREAD_POOL_SIZE);

  @Inject
  private RepositoryRpmManager repositoryRpmManager;

  @Override
  public void registerRepository(MavenRepository repository) {
    if (!repositories.containsKey(repository.getId())) {
      MavenRepositoryInfo repositoryInfo = new MavenRepositoryInfo(repository);
      repositories.put(repository.getId(), repositoryInfo);
      LOG.info("Marked repository as RPM-repository : {}", repository.getId());

      repositoryScanningExecutor.submit(new RepositoryScanningJob(repositoryRpmManager, repositoryInfo));
    }
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
