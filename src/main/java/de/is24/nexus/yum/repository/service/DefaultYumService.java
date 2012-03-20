package de.is24.nexus.yum.repository.service;

import static de.is24.nexus.yum.repository.RepositoryUtils.getBaseDir;
import static de.is24.nexus.yum.repository.task.YumMetadataGenerationTask.ID;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

import de.is24.nexus.yum.config.YumConfiguration;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.task.YumGroupRepositoryGenerationTask;
import de.is24.nexus.yum.repository.task.YumMetadataGenerationTask;


@Component(role = YumService.class)
public class DefaultYumService implements YumService {
  private static final String CACHE_DIR_PREFIX = ".cache-";

  @Inject
  private GlobalRestApiSettings restApiSettings;

	@Requirement
	private NexusScheduler nexusScheduler;

  @Requirement
  private YumConfiguration yumConfig;

  private final YumRepositoryCache cache = new YumRepositoryCache();

  private String getBaseUrl(Repository repository) {
    return String.format("%s/content/repositories/%s", restApiSettings.getBaseUrl(), repository.getId());
  }

  @Override
  public void deactivate() {
		YumMetadataGenerationTask.deactivate();
  }

  @Override
  public ScheduledTask<YumRepository> createYumRepository(File rpmBaseDir, String rpmBaseUrl, File yumRepoBaseDir,
    URL yumRepoUrl, String id,
    boolean singleRpmPerDirectory) {
    try {
			if (YumMetadataGenerationTask.isActive()) {
        YumMetadataGenerationTask task = createTask();
        task.setRpmDir(rpmBaseDir.getAbsolutePath());
        task.setRpmUrl(rpmBaseUrl);
        task.setRepositoryId(id);
        task.setRepoDir(yumRepoBaseDir);
        task.setRepoUrl(yumRepoUrl.toString());
        task.setCacheDir(createCacheDir("nexus-yum-repo"));
        task.setSingleRpmPerDirectory(singleRpmPerDirectory);
				return submitTask(task);
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
			if (YumMetadataGenerationTask.isActive()) {
        YumMetadataGenerationTask task = createTask();
        task.setRpmDir(rpmBaseDir.getAbsolutePath());
        task.setRpmUrl(getBaseUrl(repository));
        task.setRepoDir(yumRepoBaseDir);
        task.setRepoUrl(yumRepoUrl.toString());
        task.setRepositoryId(repository.getId());
        task.setVersion(version);
        task.setCacheDir(createCacheDir(repository.getId()));
				return submitTask(task);
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create repository", e);
    }

    return null;
  }

  private ScheduledTask<YumRepository> submitTask(YumMetadataGenerationTask task) {
    return nexusScheduler.submit(ID, task);
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

  @Override
  public void recreateRepository(Repository repository) {
    createYumRepository(repository);
  }

  @Override
  public void markDirty(Repository repository, String itemVersion) {
    cache.markDirty(repository.getId(), itemVersion);
  }

  @Override
  public void activate() {
		YumMetadataGenerationTask.activate();
  }

  @Override
  public ScheduledTask<YumRepository> addToYumRepository(Repository repository, String filePath) {
    try {
      File rpmBaseDir = getBaseDir(repository);
			if (YumMetadataGenerationTask.isActive()) {
        YumMetadataGenerationTask task = createTask();
        task.setRpmDir(rpmBaseDir.getAbsolutePath());
        task.setRpmUrl(getBaseUrl(repository));
        task.setRepositoryId(repository.getId());
        task.setCacheDir(createCacheDir(repository.getId()));
        task.setAddedFiles(filePath);
				return submitTask(task);
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create repository", e);
    }

    return null;
  }

  @Override
  public boolean isActive() {
		return YumMetadataGenerationTask.isActive();
  }

  @Override
  public ScheduledTask<YumRepository> createGroupRepository(GroupRepository groupRepository) {
    YumGroupRepositoryGenerationTask task = nexusScheduler.createTaskInstance(YumGroupRepositoryGenerationTask.class);
    task.setGroupRepository(groupRepository);
    return nexusScheduler.submit(YumGroupRepositoryGenerationTask.ID, task);
  }

  private YumMetadataGenerationTask createTask() {
    return nexusScheduler.createTaskInstance(YumMetadataGenerationTask.class);
  }

  private File createRepositoryTempDir(Repository repository, String version) {
    return new File(yumConfig.getBaseTempDir(), repository.getId() + File.separator + version);
  }

  private String createCacheDir(String id) {
    return new File(yumConfig.getBaseTempDir(), CACHE_DIR_PREFIX + id).getAbsolutePath();
  }
}
