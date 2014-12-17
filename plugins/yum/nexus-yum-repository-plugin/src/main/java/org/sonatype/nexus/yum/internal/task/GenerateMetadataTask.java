/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.task;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.routing.Manager;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;
import org.sonatype.nexus.scheduling.Cancelable;
import org.sonatype.nexus.scheduling.CancelableSupport;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumGroup;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.YumRepository;
import org.sonatype.nexus.yum.internal.ListFileFactory;
import org.sonatype.nexus.yum.internal.RepositoryUtils;
import org.sonatype.nexus.yum.internal.RpmListWriter;
import org.sonatype.nexus.yum.internal.RpmScanner;
import org.sonatype.nexus.yum.internal.YumRepositoryImpl;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.nexus.yum.Yum.PATH_OF_REPODATA;
import static org.sonatype.nexus.yum.Yum.PATH_OF_REPOMD_XML;

/**
 * Create a yum-repository directory via 'createrepo' command line tool.
 *
 * @since yum 3.0
 */
@Named
public class GenerateMetadataTask
    extends RepositoryTaskSupport<YumRepository>
    implements ListFileFactory, Cancelable
{
  // TODO: is defined in DefaultFSPeer. Do we want to expose it over there?
  private static final String REPO_TMP_FOLDER = ".nexus/tmp";

  private static final String PACKAGE_FILE_DIR_NAME = ".packageFiles";

  private static final String CACHE_DIR_PREFIX = ".cache-";

  private static final Logger LOG = LoggerFactory.getLogger(GenerateMetadataTask.class);

  public static final String PARAM_RPM_DIR = "rpmDir";

  public static final String PARAM_REPO_DIR = "repoDir";

  public static final String PARAM_VERSION = "version";

  public static final String PARAM_ADDED_FILES = "addedFiles";

  public static final String PARAM_SINGLE_RPM_PER_DIR = "singleRpmPerDir";

  public static final String PARAM_FORCE_FULL_SCAN = "forceFullScan";

  public static final String PARAM_YUM_GROUPS_DEFINITION_FILE = "yumGroupsDefinitionFile";

  private final RpmScanner scanner;

  private final YumRegistry yumRegistry;

  private final Manager routingManager;

  private final CommandLineExecutor commandLineExecutor;

  @Inject
  public GenerateMetadataTask(final YumRegistry yumRegistry,
                              final RpmScanner scanner,
                              final Manager routingManager,
                              final CommandLineExecutor commandLineExecutor)
  {
    this.yumRegistry = checkNotNull(yumRegistry);
    this.scanner = checkNotNull(scanner);
    this.routingManager = checkNotNull(routingManager);
    this.commandLineExecutor = checkNotNull(commandLineExecutor);

    getConfiguration().setString(PARAM_SINGLE_RPM_PER_DIR, Boolean.toString(true));
  }

  /**
   * Returns running tasks having same type as this task, that are running on set of repositories that would
   * overlap with this tasks' processed repositories, and have same version (including null!).
   */
  @Override
  public List<TaskInfo<?>> isBlockedBy(List<TaskInfo<?>> runningTasks) {
    final List<TaskInfo<?>> blockedBy = super.isBlockedBy(runningTasks);
    if (blockedBy.isEmpty()) {
      return blockedBy;
    }
    else {
      return Lists.newArrayList(Iterables.filter(blockedBy, new Predicate<TaskInfo<?>>()
      {
        @Override
        public boolean apply(final TaskInfo<?> taskInfo) {
          return Objects.equals(getConfiguration().getString(GenerateMetadataTask.PARAM_VERSION),
              taskInfo.getConfiguration().getString(GenerateMetadataTask.PARAM_VERSION));
        }
      }));
    }
  }

  @Override
  protected YumRepository execute()
      throws Exception
  {
    String repositoryId = getRepositoryId();

    if (!StringUtils.isEmpty(repositoryId)) {
      checkState(
          yumRegistry.isRegistered(repositoryId),
          "Metadata regeneration can only be run on repositories that have an enabled 'Yum: Generate Metadata' capability"
      );
      Yum yum = yumRegistry.get(repositoryId);
      checkState(
          yum.getNexusRepository().getRepositoryKind().isFacetAvailable(HostedRepository.class),
          "Metadata generation can only be run on hosted repositories"
      );
    }

    setDefaults();

    final Repository repository = findRepository();
    final RepositoryItemUid mdUid = repository.createUid("/" + PATH_OF_REPOMD_XML);
    try {
      mdUid.getLock().lock(Action.update);

      LOG.debug("Generating Yum-Repository for '{}' ...", getRpmDir());

      final File repoBaseDir = getRepoDir();
      final File repoRepodataDir = new File(repoBaseDir, PATH_OF_REPODATA);
      final File repoTmpDir = new File(repoBaseDir, REPO_TMP_FOLDER + File.separator + UUID.randomUUID().toString());
      DirSupport.mkdir(repoTmpDir);
      final File repoTmpRepodataDir = new File(repoTmpDir, PATH_OF_REPODATA);

      try {
        // NEXUS-6680: Nuke cache dir if force rebuild in effect
        if (shouldForceFullScan()) {
          DirSupport.deleteIfExists(getCacheDir().toPath());
        }

        // copy existing metadata to perform update
        if (repoRepodataDir.isDirectory()) {
          DirSupport.copy(repoRepodataDir.toPath(), repoTmpRepodataDir.toPath());
        }

        File rpmListFile = createRpmListFile();
        commandLineExecutor.exec(buildCreateRepositoryCommand(repoTmpDir, rpmListFile));

        // at the end check for cancellation
        CancelableSupport.checkCancellation();
        // got here, not canceled, move results to proper place
        DirSupport.deleteIfExists(repoRepodataDir.toPath());
        DirSupport.moveIfExists(repoTmpRepodataDir.toPath(), repoRepodataDir.toPath());
      }
      catch (IOException e) {
        LOG.warn("Yum metadata generation failed", e);
        throw new IOException("Yum metadata generation failed", e);
      }
      finally {
        deleteQuietly(repoTmpDir);
      }

      // TODO dubious
      Thread.sleep(100);

      if (repository != null) {
        final MavenRepository mavenRepository = repository.adaptToFacet(MavenRepository.class);
        if (mavenRepository != null) {
          try {
            routingManager.forceUpdatePrefixFile(mavenRepository);
          }
          catch (Exception e) {
            log.warn("Could not update Whitelist for repository '{}'", mavenRepository, e);
          }
        }
      }

      regenerateMetadataForGroups();
      return new YumRepositoryImpl(repoBaseDir, repositoryId, getVersion());
    }
    finally {
      mdUid.getLock().unlock();
    }
  }

  protected void setDefaults()
      throws MalformedURLException, URISyntaxException
  {
    final Repository repository = findRepository();
    if (isBlank(getRpmDir()) && repository != null) {
      setRpmDir(RepositoryUtils.getBaseDir(repository).getAbsolutePath());
    }
    if (isBlank(getConfiguration().getString(PARAM_REPO_DIR)) && isNotBlank(getRpmDir())) {
      setRepoDir(new File(getRpmDir()));
    }
  }

  private Repository findRepository() {
    try {
      return getRepositoryRegistry().getRepository(getRepositoryId());
    }
    catch (NoSuchRepositoryException e) {
      return null;
    }
  }

  @Override
  public String getMessage() {
    return format("Generate Yum metadata of repository '%s'", getRepositoryId());
  }

  private void regenerateMetadataForGroups() {
    if (StringUtils.isBlank(getVersion())) {
      try {
        final Repository repository = getRepositoryRegistry().getRepository(getRepositoryId());
        for (GroupRepository groupRepository : getRepositoryRegistry().getGroupsOfRepository(repository)) {
          Yum yum = yumRegistry.get(groupRepository.getId());
          if (yum != null && yum instanceof YumGroup) {
            ((YumGroup) yum).markDirty();
          }
        }
      }
      catch (NoSuchRepositoryException e) {
        log.warn(
            "Repository '{}' does not exist anymore. Backing out from triggering group merge for it.",
            getRepositoryId()
        );
      }
    }
  }

  private File createRpmListFile()
      throws IOException
  {
    return new RpmListWriter(
        new File(getRpmDir()),
        getAddedFiles(),
        getVersion(),
        isSingleRpmPerDirectory(),
        shouldForceFullScan(),
        this,
        scanner
    ).writeList();
  }

  private String getRepositoryIdVersion() {
    return getRepositoryId() + (isNotBlank(getVersion()) ? ("-version-" + getVersion()) : "");
  }

  private String buildCreateRepositoryCommand(File tmpDir, File packageList) {
    StringBuilder commandLine = new StringBuilder();
    commandLine.append(yumRegistry.getCreaterepoPath());
    if (!shouldForceFullScan()) {
      commandLine.append(" --update");
    }
    commandLine.append(" --verbose --no-database");
    commandLine.append(" --outputdir ").append(tmpDir.getAbsolutePath());
    commandLine.append(" --pkglist ").append(packageList.getAbsolutePath());
    commandLine.append(" --cachedir ").append(createCacheDir().getAbsolutePath());
    final String yumGroupsDefinitionFile = getYumGroupsDefinitionFile();
    if (yumGroupsDefinitionFile != null) {
      final File file = new File(getRepoDir().getAbsolutePath(), yumGroupsDefinitionFile);
      final String path = file.getAbsolutePath();
      if (file.exists()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          commandLine.append(" --groupfile ").append(path);
        }
        else {
          LOG.warn("Yum groups definition file '{}' must have an '.xml' extension, ignoring", path);
        }
      }
      else {
        LOG.warn("Yum groups definition file '{}' doesn't exist, ignoring", path);
      }
    }
    commandLine.append(" ").append(getRpmDir());

    return commandLine.toString();
  }

  @Override
  public File getRpmListFile() {
    return new File(createPackageDir(), getRepositoryId() + ".txt");
  }

  private File createCacheDir() {
    return getCacheDir(getRepositoryIdVersion());
  }

  private File createPackageDir() {
    return getCacheDir(PACKAGE_FILE_DIR_NAME);
  }

  private File getCacheDir(final String name) {
    final File cacheDir = new File(getCacheDir(), name);
    try {
      DirSupport.mkdir(cacheDir.toPath());
    }
    catch (IOException e) {
      Throwables.propagate(e);
    }
    return cacheDir;
  }

  private File getCacheDir() {
    return new File(yumRegistry.getTemporaryDirectory(), CACHE_DIR_PREFIX + getRepositoryId());
  }

  @Override
  public File getRpmListFile(String version) {
    return new File(createPackageDir(), getRepositoryId() + "-" + version + ".txt");
  }

  public String getRepositoryId() {
    return getConfiguration().getRepositoryId();
  }

  public void setRepositoryId(String repositoryId) {
    getConfiguration().setRepositoryId(repositoryId);
  }

  public String getAddedFiles() {
    return getConfiguration().getString(PARAM_ADDED_FILES);
  }

  public void setAddedFiles(String addedFiles) {
    getConfiguration().setString(PARAM_ADDED_FILES, addedFiles);
  }

  public File getRepoDir() {
    return new File(getConfiguration().getString(PARAM_REPO_DIR));
  }

  public void setRepoDir(File repoDir) {
    getConfiguration().setString(PARAM_REPO_DIR, repoDir.getAbsolutePath());
  }

  public String getRpmDir() {
    return getConfiguration().getString(PARAM_RPM_DIR);
  }

  public void setRpmDir(String rpmDir) {
    getConfiguration().setString(PARAM_RPM_DIR, rpmDir);
  }

  public String getVersion() {
    return getConfiguration().getString(PARAM_VERSION);
  }

  public void setVersion(String version) {
    getConfiguration().setString(PARAM_VERSION, version);
  }

  public String getYumGroupsDefinitionFile() {
    return getConfiguration().getString(PARAM_YUM_GROUPS_DEFINITION_FILE);
  }

  public void setYumGroupsDefinitionFile(String file) {
    getConfiguration().setString(PARAM_YUM_GROUPS_DEFINITION_FILE, file);
  }

  public boolean isSingleRpmPerDirectory() {
    return getConfiguration().getBoolean(PARAM_SINGLE_RPM_PER_DIR, false);
  }

  public boolean shouldForceFullScan() {
    return getConfiguration().getBoolean(PARAM_FORCE_FULL_SCAN, false);
  }

  public void setSingleRpmPerDirectory(boolean singleRpmPerDirectory) {
    getConfiguration().setBoolean(PARAM_SINGLE_RPM_PER_DIR, singleRpmPerDirectory);
  }
}
