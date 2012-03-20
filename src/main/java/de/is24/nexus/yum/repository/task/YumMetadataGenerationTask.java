package de.is24.nexus.yum.repository.task;

import static de.is24.nexus.yum.repository.YumRepository.REPOMD_XML;
import static de.is24.nexus.yum.repository.YumRepository.YUM_REPOSITORY_DIR_NAME;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

import de.is24.nexus.yum.execution.CommandLineExecutor;
import de.is24.nexus.yum.plugin.event.YumRepositoryGenerateEvent;
import de.is24.nexus.yum.repository.ListFileFactory;
import de.is24.nexus.yum.repository.RpmListWriter;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.config.YumGeneratorConfiguration;

/**
 * Create a yum-repository directory via 'createrepo' command line tool.
 * 
 * @author sherold
 * 
 */
@Component(role = SchedulerTask.class, hint = YumMetadataGenerationTask.ID, instantiationStrategy = "per-lookup")
public class YumMetadataGenerationTask extends AbstractNexusTask<YumRepository> implements ListFileFactory {
  public static final String ID = "YumMetadataGenerationTask";
  private static final String PACKAGE_FILE_DIR_NAME = ".packageFiles";
  private static final Logger LOG = LoggerFactory.getLogger(YumMetadataGenerationTask.class);
  public static final int MAXIMAL_PARALLEL_RUNS = 10;
  public static final String PARAM_REPO_ID = "yumMetadataGenerationRepoId";
  public static final String PARAM_BASE_RPM_DIR = "yumMetadataGenerationBaseRpmDir";
  public static final String PARAM_BASE_REPO_DIR = "yumMetadataGenerationBaseRepoDir";
  public static final String PARAM_VERSION = "yumMetadataGenerationVersion";
  public static final String PARAM_BASE_CACHE_DIR = "yumMetadataGenerationBaseCacheDir";
  public static final String PARAM_BASE_RPM_URL = "yumMetadataGenerationBaseRpmUrl";
  public static final String PARAM_BASE_REPO_URL = "yumMetadataGenerationBaseRepoUrl";
  public static final String PARAM_ADDED_FILES = "yumMetadataGenerationAddedFiles";
  public static final String PARAM_SINGLE_RPM_PER_DIR = "yumMetadataGenerationSingleRpmPerDir";
  private static boolean activated = true;

  @Requirement
  private ApplicationEventMulticaster eventMulticaster;

  @Requirement
  private RepositoryRegistry repositoryRegistry;

  @Override
  protected YumRepository doRun() throws Exception {
    if (activated) {
      LOG.info("Generating Yum-Repository for '{}' ...", getBaseRpmDir());
      try {
        getBaseRepoDir().mkdirs();

        File rpmListFile = createRpmListFile();
        new CommandLineExecutor().exec(buildCreateRepositoryCommand(rpmListFile));

        replaceUrl();
      } catch (IOException e) {
        LOG.warn("Generating Yum-Repo failed", e);
        throw new IOException("Generating Yum-Repo failed", e);
      }
      Thread.sleep(100);
      LOG.info("Generation complete.");

      sendNotificationEvent();
      return new YumRepository(getBaseRepoDir(), getRepositoryId(), getVersion());
    }

    return null;
  }

  @Override
  protected String getAction() {
    return "Generation YUM repository metadata";
  }

  @Override
  protected String getMessage() {
    return "Generation YUM repository metadata";
  }

  public void setConfiguration(YumGeneratorConfiguration config) {
    getParameters().put(PARAM_REPO_ID, config.getId());
    getParameters().put(PARAM_ADDED_FILES, config.getAddedFile());
    getParameters().put(PARAM_BASE_CACHE_DIR, pathOrNull(config.getBaseCacheDir()));
    getParameters().put(PARAM_BASE_REPO_DIR, pathOrNull(config.getBaseRepoDir()));
    getParameters().put(PARAM_BASE_REPO_URL, config.getBaseRepoUrl());
    getParameters().put(PARAM_BASE_RPM_DIR, pathOrNull(config.getBaseRpmDir()));
    getParameters().put(PARAM_BASE_RPM_URL, config.getBaseRpmUrl());
    getParameters().put(PARAM_VERSION, config.getVersion());
    getParameters().put(PARAM_SINGLE_RPM_PER_DIR, Boolean.toString(config.isSingleRpmPerDirectory()));
  }

  @Override
  public boolean allowConcurrentExecution(Map<String, List<ScheduledTask<?>>> activeTasks) {

    if (activeTasks.containsKey(ID)) {
      int activeRunningTasks = 0;
      for (ScheduledTask<?> scheduledTask : activeTasks.get(ID)) {
        if (RUNNING.equals(scheduledTask.getTaskState())) {
          if (conflictsWith((YumMetadataGenerationTask) scheduledTask.getTask())) {
            return false;
          }
          activeRunningTasks++;
        }
      }
      return activeRunningTasks < MAXIMAL_PARALLEL_RUNS;
    } else {
      return true;
    }
  }

  private void sendNotificationEvent() {
    if (StringUtils.isBlank(getVersion())) {
      try {
        final Repository repository = repositoryRegistry.getRepository(getRepositoryId());
        eventMulticaster.notifyEventListeners(new YumRepositoryGenerateEvent(repository));
      } catch (NoSuchRepositoryException e) {
      }
    }
  }

  private boolean conflictsWith(YumMetadataGenerationTask task) {
    if (StringUtils.equals(getRepositoryId(), task.getRepositoryId())) {
      return StringUtils.equals(getVersion(), task.getVersion());
    }
    return false;
  }

  private File createRpmListFile() throws IOException {
    return new RpmListWriter(getRepositoryId(), getBaseRpmDir(), getAddedFiles(), getVersion(), isSingleRpmPerDirectory(), this)
        .writeList();
  }

  private File createCacheDir() {
    File cacheDir = new File(getBaseCacheDir(), getRepositoryIdVersion());
    cacheDir.mkdirs();
    return cacheDir;
  }

  private String getRepositoryIdVersion() {
    return getRepositoryId() + (isNotBlank(getVersion()) ? ("-version-" + getVersion()) : "");
  }

  private void replaceUrl() throws IOException {
    File repomd = new File(getBaseRepoDir(), YUM_REPOSITORY_DIR_NAME + File.separator + REPOMD_XML);
    if (activated && repomd.exists() && getBaseRepoUrl() != null) {
      String repomdStr = FileUtils.readFileToString(repomd);
      repomdStr = repomdStr.replace(getBaseRpmUrl(), getBaseRepoUrl());
      writeStringToFile(repomd, repomdStr);
    }
  }

  private String buildCreateRepositoryCommand(File packageList) {
    String packageFile = packageList.getAbsolutePath();
    String cacheDir = createCacheDir().getAbsolutePath();
    return format("createrepo --update -o %s -u %s  -v -d -i %s -c %s %s", getBaseRepoDir().getAbsolutePath(), getBaseRpmUrl(),
        packageFile, cacheDir, getBaseRpmDir());
  }

  public static void deactivate() {
    activated = false;
  }

  public static void activate() {
    activated = true;
  }

  @Override
  public File getRpmListFile(String repositoryId) {
    return new File(createBasePackageDir(), getRepositoryId() + ".txt");
  }

  private File createBasePackageDir() {
    File basePackageDir = new File(getBaseCacheDir(), PACKAGE_FILE_DIR_NAME);
    basePackageDir.mkdirs();
    return basePackageDir;
  }

  @Override
  public File getRpmListFile(String repositoryId, String version) {
    return new File(createBasePackageDir(), getRepositoryId() + "-" + version + ".txt");
  }

  public static boolean isActive() {
    return activated;
  }

  public String getRepositoryId() {
    return getParameter(PARAM_REPO_ID);
  }

  public void setRepositoryId(String repositoryId) {
    getParameters().put(PARAM_REPO_ID, repositoryId);
  }

  public void setRepository(Repository repository) {
    getParameters().put(PARAM_REPO_ID, repository.getId());
  }

  public String getBaseCacheDir() {
    return getParameter(PARAM_BASE_CACHE_DIR);
  }

  public void setBaseCacheDir(String baseCacheDir) {
    getParameters().put(PARAM_BASE_CACHE_DIR, baseCacheDir);
  }

  public String getAddedFiles() {
    return getParameter(PARAM_ADDED_FILES);
  }

  public void setAddedFiles(String addedFiles) {
    getParameters().put(PARAM_ADDED_FILES, addedFiles);
  }

  public File getBaseRepoDir() {
    return new File(getParameter(PARAM_BASE_REPO_DIR));
  }

  public void setBaseRepoDir(File baseRepoDir) {
    getParameters().put(PARAM_BASE_REPO_DIR, baseRepoDir.getAbsolutePath());
  }

  public String getBaseRepoUrl() {
    return getParameter(PARAM_BASE_REPO_URL);
  }

  public void setBaseRepoUrl(String baseRepoUrl) {
    getParameters().put(PARAM_BASE_REPO_URL, baseRepoUrl);
  }

  public String getBaseRpmDir() {
    return getParameter(PARAM_BASE_RPM_DIR);
  }

  public void setBaseRpmDir(String baseRpmDir) {
    getParameters().put(PARAM_BASE_RPM_DIR, baseRpmDir);
  }

  public String getBaseRpmUrl() {
    return getParameter(PARAM_BASE_RPM_URL);
  }

  public void setBaseRpmUrl(String baseRpmUrl) {
    getParameters().put(PARAM_BASE_RPM_URL, baseRpmUrl);
  }

  public String getVersion() {
    return getParameter(PARAM_VERSION);
  }

  public void setVersion(String version) {
    getParameters().put(PARAM_VERSION, version);
  }

  public boolean isSingleRpmPerDirectory() {
    return Boolean.valueOf(getParameter(PARAM_SINGLE_RPM_PER_DIR));
  }

  public void setSingleRpmPerDirectory(boolean singleRpmPerDirectory) {
    getParameters().put(PARAM_SINGLE_RPM_PER_DIR, Boolean.toString(singleRpmPerDirectory));
  }

  private static String pathOrNull(File file) {
    return file != null ? file.getAbsolutePath() : null;
  }
}
