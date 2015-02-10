/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.io.DirSupport;
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
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumGroup;
import org.sonatype.nexus.yum.YumHosted;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.YumRepository;
import org.sonatype.nexus.yum.internal.RepositoryUtils;
import org.sonatype.nexus.yum.internal.RpmScanner;
import org.sonatype.nexus.yum.internal.YumRepositoryImpl;
import org.sonatype.nexus.yum.internal.createrepo.CreateYumRepository;
import org.sonatype.nexus.yum.internal.createrepo.YumPackage;
import org.sonatype.nexus.yum.internal.createrepo.YumPackageParser;
import org.sonatype.nexus.yum.internal.createrepo.YumStore;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
    implements Cancelable
{
  // TODO: is defined in DefaultFSPeer. Do we want to expose it over there?
  private static final String REPO_TMP_FOLDER = ".nexus/tmp";

  private static final Logger LOG = LoggerFactory.getLogger(GenerateMetadataTask.class);

  public static final String PARAM_RPM_DIR = "rpmDir";

  public static final String PARAM_REPO_DIR = "repoDir";

  public static final String PARAM_VERSION = "version";

  public static final String PARAM_ADDED_FILES = "addedFiles";

  public static final String PARAM_REMOVED_FILE = "removedFile";

  public static final String PARAM_FORCE_FULL_SCAN = "forceFullScan";

  public static final String PARAM_YUM_GROUPS_DEFINITION_FILE = "yumGroupsDefinitionFile";

  private final RpmScanner scanner;

  private final YumRegistry yumRegistry;

  private final Manager routingManager;

  @Inject
  public GenerateMetadataTask(final YumRegistry yumRegistry,
                              final RpmScanner scanner,
                              final Manager routingManager)
  {
    this.yumRegistry = checkNotNull(yumRegistry);
    this.scanner = checkNotNull(scanner);
    this.routingManager = checkNotNull(routingManager);
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
    checkState(
        repositoryId != null,
        "Metadata regeneration can only be run when repository id is set"
    );

    checkState(
        yumRegistry.isRegistered(repositoryId),
        "Metadata regeneration can only be run on repositories that have an enabled 'Yum: Generate Metadata' capability"
    );
    Yum yum = yumRegistry.get(repositoryId);
    checkState(
        yum.getNexusRepository().getRepositoryKind().isFacetAvailable(HostedRepository.class),
        "Metadata generation can only be run on hosted repositories"
    );

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
      DirSupport.mkdir(repoTmpRepodataDir);

      try {
        YumStore yumStore = ((YumHosted) yum).getYumStore();
        syncYumPackages(yumStore);
        try (CreateYumRepository createRepo = new CreateYumRepository(repoTmpRepodataDir, null, resolveYumGroups())) {
          String version = getVersion();
          for (YumPackage yumPackage : yumStore.get()) {
            if (version == null || hasRequiredVersion(version, yumPackage.getLocation())) {
              createRepo.write(yumPackage);
            }
          }
        }

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

      final MavenRepository mavenRepository = repository.adaptToFacet(MavenRepository.class);
      if (mavenRepository != null) {
        try {
          routingManager.forceUpdatePrefixFile(mavenRepository);
        }
        catch (Exception e) {
          log.warn("Could not update Whitelist for repository '{}'", mavenRepository, e);
        }
      }

      regenerateMetadataForGroups();
      return new YumRepositoryImpl(repoBaseDir, repositoryId, getVersion());
    }
    finally {
      mdUid.getLock().unlock();
    }
  }

  private File resolveYumGroups() {
    String yumGroupsDefinitionFile = getYumGroupsDefinitionFile();
    if (yumGroupsDefinitionFile != null) {
      File file = new File(getRepoDir().getAbsolutePath(), yumGroupsDefinitionFile);
      String path = file.getAbsolutePath();
      if (file.exists()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          return file;
        }
        else {
          LOG.warn("Yum groups definition file '{}' must have an '.xml' extension, ignoring", path);
        }
      }
      else {
        LOG.warn("Yum groups definition file '{}' doesn't exist, ignoring", path);
      }
    }
    return null;
  }

  private boolean hasRequiredVersion(final String version, String path) {
    String[] segments = path.split("\\/");
    return (segments.length >= 2) && version.equals(segments[segments.length - 2]);
  }

  private void syncYumPackages(final YumStore yumStore) {
    Set<File> files = null;
    File rpmDir = new File(getRpmDir());
    if (shouldForceFullScan()) {
      files = scanner.scan(rpmDir);
      yumStore.deleteAll();
    }
    else if (getAddedFiles() != null) {
      String[] addedFiles = getAddedFiles().split(File.pathSeparator);
      files = Sets.newHashSet();
      for (String addedFile : addedFiles) {
        files.add(new File(rpmDir, addedFile));
      }
    }
    if (files != null) {
      for (File file : files) {
        String location = RpmScanner.getRelativePath(rpmDir, file.getAbsoluteFile());
        try {
          YumPackage yumPackage = new YumPackageParser().parse(
              new FileInputStream(file), location, file.lastModified()
          );
          yumStore.put(yumPackage);
        }
        catch (FileNotFoundException e) {
          log.warn("Could not parse yum metadata for {}", location, e);
        }
      }
    }

    String removedPath = getRemovedFile();
    if (removedPath != null) {
      if (removedPath.startsWith("/")) {
        removedPath = "/".equals(removedPath) ? "" : removedPath.substring(1);
      }
      yumStore.delete(removedPath);
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

  public String getRemovedFile() {
    return getConfiguration().getString(PARAM_REMOVED_FILE);
  }

  public void setRemovedFile(String removedFile) {
    getConfiguration().setString(PARAM_REMOVED_FILE, removedFile);
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

  public boolean shouldForceFullScan() {
    return getConfiguration().getBoolean(PARAM_FORCE_FULL_SCAN, false);
  }

}
