package de.is24.nexus.yum.service.impl;

import static de.is24.nexus.yum.repository.RepositoryUtils.getBaseDir;
import static de.is24.nexus.yum.repository.YumGeneratorConfigurationBuilder.newConfigBuilder;
import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plugin.Managed;
import org.sonatype.scheduling.ScheduledTask;
import com.google.inject.Singleton;
import de.is24.nexus.yum.repository.YumGeneratorConfigurationBuilder;
import de.is24.nexus.yum.repository.YumMetadataGenerationTask;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.YumRepositoryGeneratorJob;
import de.is24.nexus.yum.service.YumService;


@Managed
@Named(YumService.DEFAULT_BEAN_NAME)
@Singleton
public class DefaultYumService implements YumService {
  private static final String CACHE_DIR_PREFIX = ".cache-";

  @Inject
  private YumRepositoryCreatorService executorService;

  @Inject
  private GlobalRestApiSettings restApiSettings;

  @Inject
  private NexusConfiguration nexusConfiguration;

  private File baseTempDir;

  private final YumRepositoryCache cache = new YumRepositoryCache();

  private String getBaseUrl(Repository repository) {
    return String.format("%s/content/repositories/%s", restApiSettings.getBaseUrl(), repository.getId());
  }

  @Override
  public void deactivate() {
    executorService.shutdown();
    YumRepositoryGeneratorJob.deactivate();
  }

  @Override
  public ScheduledTask<YumRepository> createYumRepository(File rpmBaseDir, String rpmBaseUrl, File yumRepoBaseDir,
    URL yumRepoUrl, String id,
    boolean singleRpmPerDirectory) {
    try {
      if (!executorService.isShutdown()) {
        YumGeneratorConfigurationBuilder config = newConfigBuilder();
        config.rpmDir(rpmBaseDir);
        config.rpmUrl(rpmBaseUrl);
        config.id(id);
        config.repoDir(yumRepoBaseDir);
        config.repoUrl(yumRepoUrl.toString());
        config.cacheDir(createCacheDir("nexus-yum-repo"));
        config.singleRpmPerDirectory(singleRpmPerDirectory);
        return executorService.submit(createTask(config));
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create repository", e);
    }

    return null;
  }

  @Override
  public ScheduledTask<YumRepository> createYumRepository(Repository repository, String version, File yumRepoBaseDir,
    URL yumRepoUrl) {
    try {
      File rpmBaseDir = getBaseDir(repository);
      if (!executorService.isShutdown()) {
        YumGeneratorConfigurationBuilder config = newConfigBuilder();
        config.rpmDir(rpmBaseDir);
        config.rpmUrl(getBaseUrl(repository));
        config.repoDir(yumRepoBaseDir);
        config.repoUrl(yumRepoUrl.toString());
        config.id(repository.getId());
        config.version(version);
        config.cacheDir(createCacheDir(repository.getId()));
        return executorService.submit(createTask(config));
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create repository", e);
    }

    return null;
  }

  @Override
  public ScheduledTask<YumRepository> createYumRepository(Repository repository) {
    return addToYumRepository(repository, null);
  }

  @Override
  public YumRepository getRepository(Repository repository, String version, URL baseRepoUrl) throws Exception {
    YumRepository yumRepository = cache.lookup(repository.getId(), version);
    if ((yumRepository == null) || yumRepository.isDirty()) {
      ScheduledTask<YumRepository> future = createYumRepository(repository, version,
        createRepositoryTempDir(repository, version), baseRepoUrl);
      yumRepository = future.get();
      cache.cache(yumRepository);
    }
    return yumRepository;
  }

  private YumMetadataGenerationTask createTask(YumGeneratorConfigurationBuilder config) {
    YumMetadataGenerationTask task = executorService.createTaskInstance(YumMetadataGenerationTask.class);
    task.setConfiguration(config.toConfig());
    return task;
  }

  private File createRepositoryTempDir(Repository repository, String version) {
    return new File(getBaseTempDir(), repository.getId() + File.separator + version);
  }

  private File createCacheDir(String id) {
    return new File(getBaseTempDir(), CACHE_DIR_PREFIX + id);
  }

  @Override
  public void markDirty(Repository repository, String itemVersion) {
    cache.markDirty(repository.getId(), itemVersion);
  }

  @Override
  public File getBaseTempDir() {
    if (baseTempDir == null) {
      baseTempDir = new File(nexusConfiguration.getTemporaryDirectory(), "yum");
    }

    return baseTempDir;
  }

  @Override
  public void activate() {
    YumRepositoryGeneratorJob.activate();
    if (executorService.isShutdown()) {
      executorService.activate();
    }
  }

  @Override
  public ScheduledTask<YumRepository> addToYumRepository(Repository repository, String filePath) {
    try {
      File rpmBaseDir = getBaseDir(repository);
      if (!executorService.isShutdown()) {
        YumGeneratorConfigurationBuilder config = newConfigBuilder();
        config.rpmDir(rpmBaseDir);
        config.rpmUrl(getBaseUrl(repository));
        config.id(repository.getId());
        config.cacheDir(createCacheDir(repository.getId()));
        config.addedFile(filePath);
        return executorService.submit(createTask(config));
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create repository", e);
    }

    return null;
  }

  @Override
  public boolean isActive() {
    return YumRepositoryGeneratorJob.isActive();
  }
}
