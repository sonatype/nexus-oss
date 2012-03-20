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

import de.is24.nexus.yum.config.YumConfiguration;
import de.is24.nexus.yum.execution.CommandLineExecutor;
import de.is24.nexus.yum.plugin.event.YumRepositoryGenerateEvent;
import de.is24.nexus.yum.repository.ListFileFactory;
import de.is24.nexus.yum.repository.RpmListWriter;
import de.is24.nexus.yum.repository.YumRepository;

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
  private static final String CACHE_DIR_PREFIX = ".cache-";
  private static final Logger LOG = LoggerFactory.getLogger(YumMetadataGenerationTask.class);
  public static final int MAXIMAL_PARALLEL_RUNS = 10;
  public static final String PARAM_REPO_ID = "yumMetadataGenerationRepoId";
  public static final String PARAM_RPM_DIR = "yumMetadataGenerationRpmDir";
  public static final String PARAM_REPO_DIR = "yumMetadataGenerationRepoDir";
  public static final String PARAM_VERSION = "yumMetadataGenerationVersion";
  public static final String PARAM_CACHE_DIR = "yumMetadataGenerationCacheDir";
  public static final String PARAM_RPM_URL = "yumMetadataGenerationRpmUrl";
  public static final String PARAM_REPO_URL = "yumMetadataGenerationRepoUrl";
  public static final String PARAM_ADDED_FILES = "yumMetadataGenerationAddedFiles";
  public static final String PARAM_SINGLE_RPM_PER_DIR = "yumMetadataGenerationSingleRpmPerDir";
  private static boolean activated = true;

  public YumMetadataGenerationTask() {
    this(null);
  }

  public YumMetadataGenerationTask(String name) {
    super(name);
    getParameters().put(PARAM_SINGLE_RPM_PER_DIR, Boolean.toString(true));
  }

  @Requirement
  private ApplicationEventMulticaster eventMulticaster;

  @Requirement
  private RepositoryRegistry repositoryRegistry;

  @Requirement
  private YumConfiguration yumConfig;

  @Override
  protected YumRepository doRun() throws Exception {
    setDefaults();
    if (activated) {
      LOG.info("Generating Yum-Repository for '{}' ...", getRpmDir());
      try {
        getRepoDir().mkdirs();

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
      return new YumRepository(getRepoDir(), getRepositoryId(), getVersion());
    }

    return null;
  }

  protected void setDefaults() {
    if (getParameter(PARAM_REPO_DIR) == null) {
      setRepoDir(new File(getRpmDir()));
    }
    if (getRepoUrl() == null) {
      setRepoUrl(getRpmUrl());
    }
  }

  @Override
  protected String getAction() {
    return "Generation YUM repository metadata";
  }

  @Override
  protected String getMessage() {
    return "Generation YUM repository metadata";
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
    return new RpmListWriter(getRepositoryId(), getRpmDir(), getAddedFiles(), getVersion(), isSingleRpmPerDirectory(), this)
        .writeList();
  }

  private File createCacheDir() {
    File cacheDir = new File(getCacheDir(), getRepositoryIdVersion());
    cacheDir.mkdirs();
    return cacheDir;
  }

  private String getRepositoryIdVersion() {
    return getRepositoryId() + (isNotBlank(getVersion()) ? ("-version-" + getVersion()) : "");
  }

  private void replaceUrl() throws IOException {
    File repomd = new File(getRepoDir(), YUM_REPOSITORY_DIR_NAME + File.separator + REPOMD_XML);
    if (activated && repomd.exists() && getRepoUrl() != null) {
      String repomdStr = FileUtils.readFileToString(repomd);
      repomdStr = repomdStr.replace(getRpmUrl(), getRepoUrl());
      writeStringToFile(repomd, repomdStr);
    }
  }

  private String buildCreateRepositoryCommand(File packageList) {
    String packageFile = packageList.getAbsolutePath();
    String cacheDir = createCacheDir().getAbsolutePath();
    return format("createrepo --update -o %s -u %s  -v -d -i %s -c %s %s", getRepoDir().getAbsolutePath(), getRpmUrl(), packageFile,
        cacheDir, getRpmDir());
  }

  public static void deactivate() {
    activated = false;
  }

  public static void activate() {
    activated = true;
  }

  @Override
  public File getRpmListFile(String repositoryId) {
    return new File(createPackageDir(), getRepositoryId() + ".txt");
  }

  private File createPackageDir() {
    File PackageDir = new File(getCacheDir(), PACKAGE_FILE_DIR_NAME);
    PackageDir.mkdirs();
    return PackageDir;
  }

  @Override
  public File getRpmListFile(String repositoryId, String version) {
    return new File(createPackageDir(), getRepositoryId() + "-" + version + ".txt");
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
    setRepositoryId(repository.getId());
  }

  public File getCacheDir() {
    return new File(yumConfig.getBaseTempDir(), CACHE_DIR_PREFIX + getRepositoryId());
  }

  public void setCacheDir(String CacheDir) {
  }

  public String getAddedFiles() {
    return getParameter(PARAM_ADDED_FILES);
  }

  public void setAddedFiles(String addedFiles) {
    getParameters().put(PARAM_ADDED_FILES, addedFiles);
  }

  public File getRepoDir() {
    return new File(getParameter(PARAM_REPO_DIR));
  }

  public void setRepoDir(File RepoDir) {
    getParameters().put(PARAM_REPO_DIR, RepoDir.getAbsolutePath());
  }

  public String getRepoUrl() {
    return getParameter(PARAM_REPO_URL);
  }

  public void setRepoUrl(String RepoUrl) {
    getParameters().put(PARAM_REPO_URL, RepoUrl);
  }

  public String getRpmDir() {
    return getParameter(PARAM_RPM_DIR);
  }

  public void setRpmDir(String RpmDir) {
    getParameters().put(PARAM_RPM_DIR, RpmDir);
  }

  public String getRpmUrl() {
    return getParameter(PARAM_RPM_URL);
  }

  public void setRpmUrl(String RpmUrl) {
    getParameters().put(PARAM_RPM_URL, RpmUrl);
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
}
