package de.is24.nexus.yum.plugin.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;
import com.google.inject.Singleton;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.repository.RepositoryScanningJob;
import de.is24.nexus.yum.service.RepositoryRpmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;


@Named(RepositoryRegistry.DEFAULT_BEAN_NAME)
@Singleton
public class DefaultRepositoryRegistry implements RepositoryRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRepositoryRegistry.class);

  private static final int REPOSITORY_SCANNING_THREAD_POOL_SIZE = 3;

  private final Map<String, MavenRepositoryInfo> repositories = new ConcurrentHashMap<String, MavenRepositoryInfo>();

  private final ExecutorService repositoryScanningExecutor = Executors.newFixedThreadPool(
    REPOSITORY_SCANNING_THREAD_POOL_SIZE);

  @Inject
  @Named(RepositoryRpmManager.DEFAULT_BEAN_NAME)
  private RepositoryRpmManager repositoryRpmManager;

  public void registerRepository(MavenRepository repository) {
    if (!repositories.containsKey(repository.getId())) {
      MavenRepositoryInfo repositoryInfo = new MavenRepositoryInfo(repository);
      repositories.put(repository.getId(), repositoryInfo);
      LOG.info("Marked repository as RPM-repository : {}", repository.getId());

      repositoryScanningExecutor.submit(new RepositoryScanningJob(repositoryRpmManager, repositoryInfo));
    }
  }

  public boolean isRegistered(Repository repository) {
    return repositories.containsKey(repository.getId());
  }

  public MavenRepository findRepositoryForId(final String repositoryId) {
    final MavenRepositoryInfo repositoryInfo = findRepositoryInfoForId(repositoryId);
    if (repositoryInfo == null) {
      return null;
    }
    return repositoryInfo.getRepository();
  }

  public MavenRepositoryInfo findRepositoryInfoForId(final String repositoryId) {
    return repositories.get(repositoryId);
  }

  public void unregisterRepository(Repository repository) {
    repositories.remove(repository.getId());
  }
}
