/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.maven.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionScheme;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RecreateMavenMetadataWalkerProcessor;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.DottedStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.proxy.wastebasket.DeleteOperation;
import org.sonatype.nexus.util.ItemPathUtils;
import org.sonatype.scheduling.TaskUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Class SnapshotRemoverJob. After a successful run, the job guarantees that there will remain at least
 * minCountOfSnapshotsToKeep (but maybe more) snapshots per one snapshot collection by removing all older from
 * removeSnapshotsOlderThanDays. If should remove snaps if their release counterpart exists, the whole GAV will be
 * removed.
 *
 * @author cstamas
 */
@Named
@Singleton
public class DefaultSnapshotRemover
    extends AbstractLoggingComponent
    implements SnapshotRemover
{

  private RepositoryRegistry repositoryRegistry;

  private Walker walker;

  private ContentClass maven2ContentClass;

  private VersionScheme versionScheme = new GenericVersionScheme();

  @Inject
  public DefaultSnapshotRemover(final RepositoryRegistry repositoryRegistry,
                                final Walker walker,
                                final @Named("maven2") ContentClass maven2ContentClass)
  {
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    this.walker = checkNotNull(walker);
    this.maven2ContentClass = checkNotNull(maven2ContentClass);
  }

  protected RepositoryRegistry getRepositoryRegistry() {
    return repositoryRegistry;
  }

  public SnapshotRemovalResult removeSnapshots(SnapshotRemovalRequest request)
      throws NoSuchRepositoryException, IllegalArgumentException
  {
    SnapshotRemovalResult result = new SnapshotRemovalResult();

    logDetails(request);

    if (request.getRepositoryId() != null) {
      Repository repository = getRepositoryRegistry().getRepository(request.getRepositoryId());

      if (!process(request, result, repository)) {
        throw new IllegalArgumentException("The repository with ID=" + repository.getId()
            + " is not valid for Snapshot Removal Task!");
      }
    }
    else {
      for (Repository repository : getRepositoryRegistry().getRepositories()) {
        process(request, result, repository);
      }
    }

    return result;
  }

  private void process(SnapshotRemovalRequest request, SnapshotRemovalResult result, GroupRepository group) {
    for (Repository repository : group.getMemberRepositories()) {
      process(request, result, repository);
    }
  }

  private boolean process(SnapshotRemovalRequest request, SnapshotRemovalResult result, Repository repository) {
    // only from maven repositories, stay silent for others and simply skip
    if (!repository.getRepositoryContentClass().isCompatible(maven2ContentClass)) {
      getLogger().debug("Skipping '" + repository.getId() + "' is not a maven 2 repository");
      return false;
    }

    if (!repository.getLocalStatus().shouldServiceRequest()) {
      getLogger().debug("Skipping '" + repository.getId() + "' the repository is out of service");
      return false;
    }

    if (repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
      getLogger().debug("Skipping '" + repository.getId() + "' is a proxy repository");
      return false;
    }

    if (repository.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
      process(request, result, repository.adaptToFacet(GroupRepository.class));
    }
    else if (repository.getRepositoryKind().isFacetAvailable(MavenRepository.class)) {
      result.addResult(removeSnapshotsFromMavenRepository(repository.adaptToFacet(MavenRepository.class),
          request));
    }

    return true;
  }

  /**
   * Removes the snapshots from maven repository.
   *
   * @param repository the repository
   * @throws Exception the exception
   */
  protected SnapshotRemovalRepositoryResult removeSnapshotsFromMavenRepository(MavenRepository repository,
                                                                               SnapshotRemovalRequest request)
  {
    TaskUtil.checkInterruption();

    SnapshotRemovalRepositoryResult result = new SnapshotRemovalRepositoryResult(repository.getId(), 0, 0, true);

    if (!repository.getLocalStatus().shouldServiceRequest()) {
      return result;
    }

    // we are already processed here, so skip repo
    if (request.isProcessedRepo(repository.getId())) {
      return new SnapshotRemovalRepositoryResult(repository.getId(), true);
    }

    request.addProcessedRepo(repository.getId());

    // if this is not snap repo, do nothing
    if (!RepositoryPolicy.SNAPSHOT.equals(repository.getRepositoryPolicy())) {
      return result;
    }

    if (getLogger().isDebugEnabled()) {
      getLogger().debug(
          "Collecting deletable snapshots on repository " + repository.getId() + " from storage directory "
              + repository.getLocalUrl());
    }

    final ParentOMatic parentOMatic = new ParentOMatic();

    // create a walker to collect deletables and let it loose on collections only
    SnapshotRemoverWalkerProcessor snapshotRemoveProcessor =
        new SnapshotRemoverWalkerProcessor(repository, request, parentOMatic);

    DefaultWalkerContext ctxMain =
        new DefaultWalkerContext(repository, new ResourceStoreRequest("/"), new DottedStoreWalkerFilter());

    ctxMain.getContext().put(DeleteOperation.DELETE_OPERATION_CTX_KEY, getDeleteOperation(request));

    ctxMain.getProcessors().add(snapshotRemoveProcessor);

    walker.walk(ctxMain);

    if (ctxMain.getStopCause() != null) {
      result.setSuccessful(false);
    }

    // and collect results
    result.setDeletedSnapshots(snapshotRemoveProcessor.getDeletedSnapshots());
    result.setDeletedFiles(snapshotRemoveProcessor.getDeletedFiles());

    if (getLogger().isDebugEnabled()) {
      getLogger().debug(
          "Collected and deleted " + snapshotRemoveProcessor.getDeletedSnapshots()
              + " snapshots with alltogether " + snapshotRemoveProcessor.getDeletedFiles()
              + " files on repository " + repository.getId());
    }

    // if we are processing a hosted-snapshot repository, we need to rebuild maven metadata
    // without this if below, the walk would happen against proxy repositories too, but doing nothing!
    if (repository.getRepositoryKind().isFacetAvailable(HostedRepository.class)) {
      // expire NFC since we might create new maven metadata files
      repository.expireNotFoundCaches(new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT));

      RecreateMavenMetadataWalkerProcessor metadataRebuildProcessor =
          new RecreateMavenMetadataWalkerProcessor(getLogger(), getDeleteOperation(request));

      for (String path : parentOMatic.getMarkedPaths()) {
        TaskUtil.checkInterruption();

        DefaultWalkerContext ctxMd =
            new DefaultWalkerContext(repository, new ResourceStoreRequest(path),
                new DottedStoreWalkerFilter());

        ctxMd.getProcessors().add(metadataRebuildProcessor);

        try {
          walker.walk(ctxMd);
        }
        catch (WalkerException e) {
          if (!(e.getCause() instanceof ItemNotFoundException)) {
            // do not ignore it
            throw e;
          }
        }
      }
    }

    return result;
  }

  private DeleteOperation getDeleteOperation(final SnapshotRemovalRequest request) {
    return request.isDeleteImmediately() ? DeleteOperation.DELETE_PERMANENTLY : DeleteOperation.MOVE_TO_TRASH;
  }

  private void logDetails(SnapshotRemovalRequest request) {
    if (request.getRepositoryId() != null) {
      getLogger().info("Removing old SNAPSHOT deployments from " + request.getRepositoryId() + " repository.");
    }
    else {
      getLogger().info("Removing old SNAPSHOT deployments from all repositories.");
    }

    if (getLogger().isDebugEnabled()) {
      getLogger().debug("With parameters: ");
      getLogger().debug("    MinCountOfSnapshotsToKeep: " + request.getMinCountOfSnapshotsToKeep());
      getLogger().debug("    RemoveSnapshotsOlderThanDays: " + request.getRemoveSnapshotsOlderThanDays());
      getLogger().debug("    RemoveIfReleaseExists: " + request.isRemoveIfReleaseExists());
      getLogger().debug("    DeleteImmediately: " + request.isDeleteImmediately());
      getLogger().debug("    UseLastRequestedTimestamp: " + request.shouldUseLastRequestedTimestamp());
    }
  }

  private class SnapshotRemoverWalkerProcessor
      extends AbstractFileDeletingWalkerProcessor
  {

    private static final long MILLIS_IN_A_DAY = 86400000L;

    private final MavenRepository repository;

    private final SnapshotRemovalRequest request;

    private final Map<Version, List<StorageFileItem>> remainingSnapshotsAndFiles =
        new HashMap<Version, List<StorageFileItem>>();

    private final Map<Version, List<StorageFileItem>> deletableSnapshotsAndFiles =
        new HashMap<Version, List<StorageFileItem>>();

    private final ParentOMatic collectionNodes;

    private final long dateThreshold;

    private final long startTime;

    private final long gracePeriodInMillis;

    private boolean shouldProcessCollection;

    private boolean removeWholeGAV;

    private int deletedSnapshots = 0;

    private int deletedFiles = 0;

    public SnapshotRemoverWalkerProcessor(MavenRepository repository, SnapshotRemovalRequest request,
                                          final ParentOMatic collectionNodes)
    {
      this.repository = repository;
      this.request = request;
      this.collectionNodes = collectionNodes;

      this.startTime = System.currentTimeMillis();

      int days = request.getRemoveSnapshotsOlderThanDays();

      if (days > 0) {
        this.dateThreshold = startTime - (days * MILLIS_IN_A_DAY);
      }
      else {
        this.dateThreshold = -1;
      }

      gracePeriodInMillis = Math.max(0, request.getGraceDaysAfterRelease()) * MILLIS_IN_A_DAY;
    }

    protected void addStorageFileItemToMap(Map<Version, List<StorageFileItem>> map, Gav gav, StorageFileItem item) {
      Version key = null;
      try {
        key = versionScheme.parseVersion(gav.getVersion());
      }
      catch (InvalidVersionSpecificationException e) {
        try {
          key = versionScheme.parseVersion("0.0-SNAPSHOT");
        }
        catch (InvalidVersionSpecificationException e1) {
          // nah
        }
      }

      if (!map.containsKey(key)) {
        map.put(key, new ArrayList<StorageFileItem>());
      }

      map.get(key).add(item);
    }

    @Override
    public void processItem(WalkerContext context, StorageItem item)
        throws Exception
    {
    }

    @Override
    public void onCollectionExit(WalkerContext context, StorageCollectionItem coll) {
      try {
        doOnCollectionExit(context, coll);
      }
      catch (Exception e) {
        // we always simply log the exception and continue
        getLogger().warn("SnapshotRemover is failed to process path: '" + coll.getPath() + "'.", e);
      }
    }

    public void doOnCollectionExit(WalkerContext context, StorageCollectionItem coll)
        throws Exception
    {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("onCollectionExit() :: " + coll.getRepositoryItemUid().toString());
      }

      shouldProcessCollection = coll.getPath().endsWith("SNAPSHOT");

      if (!shouldProcessCollection) {
        return;
      }

      deletableSnapshotsAndFiles.clear();

      remainingSnapshotsAndFiles.clear();

      removeWholeGAV = false;

      Gav gav = null;

      Collection<StorageItem> items;

      items = repository.list(false, coll);

      HashSet<Long> versionsToRemove = new HashSet<Long>();

      // gathering the facts
      for (StorageItem item : items) {
        if (!item.isVirtual() && !StorageCollectionItem.class.isAssignableFrom(item.getClass())) {
          gav =
              ((MavenRepository) coll.getRepositoryItemUid().getRepository()).getGavCalculator().pathToGav(
                  item.getPath());

          if (gav != null) {
            // if we find a pom, check for delete on release
            if (!gav.isHash() && !gav.isSignature() && gav.getExtension().equals("pom")) {
              if (request.isRemoveIfReleaseExists()
                  && releaseExistsForSnapshot(gav, item.getItemContext())) {
                getLogger().debug("Found POM and release exists, removing whole gav.");

                removeWholeGAV = true;

                // Will break out and junk whole gav
                break;
              }
            }

            item.getItemContext().put(Gav.class.getName(), gav);

            getLogger().debug(item.getPath());

            if (gav.getSnapshotTimeStamp() != null) {
              long itemTimestamp = gav.getSnapshotTimeStamp().longValue();

              if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                    "itemTimestamp={} ({}), dateThreshold={} ({})",
                    itemTimestamp, itemTimestamp > 0 ? new Date(itemTimestamp) : "",
                    dateThreshold, dateThreshold > 0 ? new Date(dateThreshold) : ""
                );
              }

              // If this timestamp is already marked to be removed, junk it
              if (versionsToRemove.contains(new Long(itemTimestamp))) {
                addStorageFileItemToMap(deletableSnapshotsAndFiles, gav, (StorageFileItem) item);
              }
              else {
                if (snapshotShouldBeRemoved(coll, item, gav, itemTimestamp)) {
                  versionsToRemove.add(new Long(itemTimestamp));
                  addStorageFileItemToMap(deletableSnapshotsAndFiles, gav, (StorageFileItem) item);
                }
                else {
                  //do not delete if dateThreshold not met
                  addStorageFileItemToMap(remainingSnapshotsAndFiles, gav, (StorageFileItem) item);
                }
              }
            }
            else {
              // If no timestamp on gav, then it is a non-unique snapshot
              // and should _not_ be removed
              getLogger().debug("GAV Snapshot timestamp not available, skipping non-unique snapshot");

              addStorageFileItemToMap(remainingSnapshotsAndFiles, gav, (StorageFileItem) item);
            }
          }
        }
      }

      // and doing the work here
      if (removeWholeGAV) {
        try {
          for (StorageItem item : items) {
            try {
              // preserve possible subdirs
              if (!(item instanceof StorageCollectionItem)) {
                repository.deleteItem(false, createResourceStoreRequest(item, context));
              }
            }
            catch (ItemNotFoundException e) {
              if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                    "Could not delete whole GAV " + coll.getRepositoryItemUid().toString(), e);
              }
            }
          }
        }
        catch (Exception e) {
          getLogger().warn("Could not delete whole GAV " + coll.getRepositoryItemUid().toString(), e);
        }
      }
      else {
        // and now check some things
        if (remainingSnapshotsAndFiles.size() < request.getMinCountOfSnapshotsToKeep()) {
          // do something
          if (remainingSnapshotsAndFiles.size() + deletableSnapshotsAndFiles.size() <
              request.getMinCountOfSnapshotsToKeep()) {
            // delete nothing, since there is less snapshots in total as allowed
            deletableSnapshotsAndFiles.clear();
          }
          else {
            TreeSet<Version> keys = new TreeSet<Version>(deletableSnapshotsAndFiles.keySet());

            while (!keys.isEmpty()
                && remainingSnapshotsAndFiles.size() < request.getMinCountOfSnapshotsToKeep()) {
              Version keyToMove = keys.last();

              if (remainingSnapshotsAndFiles.containsKey(keyToMove)) {
                remainingSnapshotsAndFiles.get(keyToMove).addAll(
                    deletableSnapshotsAndFiles.get(keyToMove));
              }
              else {
                remainingSnapshotsAndFiles.put(keyToMove, deletableSnapshotsAndFiles.get(keyToMove));
              }

              deletableSnapshotsAndFiles.remove(keyToMove);

              keys.remove(keyToMove);
            }

          }
        }

        // NEXUS-814: is this GAV have remaining artifacts?
        boolean gavHasMoreTimestampedSnapshots = remainingSnapshotsAndFiles.size() > 0;

        for (Version key : deletableSnapshotsAndFiles.keySet()) {

          List<StorageFileItem> files = deletableSnapshotsAndFiles.get(key);
          deletedSnapshots++;

          for (StorageFileItem file : files) {
            try {
              // NEXUS-814: mark that we are deleting a TS snapshot, but there are still remaining
              // ones in repository.
              if (gavHasMoreTimestampedSnapshots) {
                file.getItemContext().put(MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV, Boolean.TRUE);
              }

              gav = (Gav) file.getItemContext().get(Gav.class.getName());

              repository.deleteItem(false, createResourceStoreRequest(file, context));

              deletedFiles++;
            }
            catch (ItemNotFoundException e) {
              // NEXUS-5682 Since checksum files are no longer physically represented on the file system,
              // it is expected that they will generate ItemNotFoundException. Log at trace level only for
              // diagnostic purposes.
              if (getLogger().isTraceEnabled()) {
                getLogger().trace("Could not delete file:", e);
              }

            }
            catch (Exception e) {
              getLogger().info("Could not delete file:", e);
            }
          }
        }
      }

      removeDirectoryIfEmpty(repository, coll);

      updateMetadataIfNecessary(context, coll);

    }

    /**
     * if dateThreshold is not used (zero days) OR
     * if last requested is less then dateThreshold (and las requested should be used) OR
     * if itemTimestamp is less then dateThreshold (NB: both are positive!) OR
     *
     * @since 2.7.0
     */
    private boolean snapshotShouldBeRemoved(final StorageCollectionItem coll,
                                            final StorageItem item,
                                            final Gav gav,
                                            final long itemTimestamp)
        throws Exception
    {
      if (-1 == dateThreshold) {
        return true;
      }

      if (request.shouldUseLastRequestedTimestamp()) {
        return getLastRequested(coll, item, gav) < dateThreshold;
      }

      return itemTimestamp < dateThreshold;
    }

    /**
     * Returns the most recent requested timestamp for a specified item by looking at item itself, its pom and any
     * attached artifacts that share the same timestamp/build number.
     *
     * @since 2.7.0
     */
    private long getLastRequested(final StorageCollectionItem coll, final StorageItem item, final Gav gav)
        throws Exception
    {
      long lastRequested = item.getLastRequested();
      final MavenRepository repository = (MavenRepository) coll.getRepositoryItemUid().getRepository();
      final Collection<StorageItem> items = repository.list(false, coll);
      for (final StorageItem listedItem : items) {
        final Gav listedItemGav = repository.getGavCalculator().pathToGav(listedItem.getPath());
        if (gav.getSnapshotBuildNumber().equals(listedItemGav.getSnapshotBuildNumber())
            && gav.getSnapshotTimeStamp().equals(listedItemGav.getSnapshotTimeStamp())) {
          lastRequested = Math.max(lastRequested, listedItem.getLastRequested());
        }
      }
      if (getLogger().isDebugEnabled()) {
        // FIXME this debug message lacks storage item context, and could be possibly better at TRACE as well
        getLogger().debug(
            "lastRequested={} ({}), dateThreshold={} ({})",
            lastRequested, lastRequested > 0 ? new Date(lastRequested) : "",
            dateThreshold, dateThreshold > 0 ? new Date(dateThreshold) : ""
        );
      }
      return lastRequested;
    }

    private void updateMetadataIfNecessary(WalkerContext context, StorageCollectionItem coll)
        throws Exception
    {
      // all snapshot files are deleted
      if (!deletableSnapshotsAndFiles.isEmpty() && remainingSnapshotsAndFiles.isEmpty()) {
        collectionNodes.addAndMarkPath(ItemPathUtils.getParentPath(coll.getPath()));
      }
      else {
        collectionNodes.addAndMarkPath(coll.getPath());
      }
    }

    public boolean releaseExistsForSnapshot(Gav snapshotGav, Map<String, Object> context) {
      long releaseTimestamp = -1;

      for (Repository repository : repositoryRegistry.getRepositories()) {
        // we need to filter for:
        // repository that is MavenRepository and is hosted or proxy
        // repository that has release policy
        if (repository.getRepositoryKind().isFacetAvailable(MavenHostedRepository.class)
            || repository.getRepositoryKind().isFacetAvailable(MavenProxyRepository.class)) {
          // actually, we don't care is it proxy or hosted, we only need to filter out groups and other
          // "composite" reposes like shadows
          MavenRepository mrepository = repository.adaptToFacet(MavenRepository.class);

          // look in release reposes only
          if (mrepository.isUserManaged()
              && RepositoryPolicy.RELEASE.equals(mrepository.getRepositoryPolicy())) {
            try {
              String releaseVersion = null;

              // NEXUS-3148
              if (snapshotGav.getBaseVersion().endsWith("-SNAPSHOT")) {
                // "-SNAPSHOT" :== 9 chars
                releaseVersion =
                    snapshotGav.getBaseVersion().substring(0,
                        snapshotGav.getBaseVersion().length() - 9);
              }
              else {
                // "SNAPSHOT" :== 8 chars
                releaseVersion =
                    snapshotGav.getBaseVersion().substring(0,
                        snapshotGav.getBaseVersion().length() - 8);
              }

              Gav releaseGav =
                  new Gav(snapshotGav.getGroupId(), snapshotGav.getArtifactId(), releaseVersion,
                      snapshotGav.getClassifier(), snapshotGav.getExtension(), null, null, null, false,
                      null, false, null);

              String path = mrepository.getGavCalculator().gavToPath(releaseGav);

              ResourceStoreRequest req = new ResourceStoreRequest(path, true);

              req.getRequestContext().putAll(context);

              getLogger().debug("Checking for release counterpart in repository '{}' and path '{}'",
                  mrepository.getId(), req.toString());

              final StorageItem item = mrepository.retrieveItem(false, req);

              releaseTimestamp = item.getCreated();

              break;
            }
            catch (ItemNotFoundException e) {
              // nothing
            }
            catch (Exception e) {
              // nothing
              getLogger().debug("Unexpected exception!", e);
            }
          }
        }
      }

      return releaseTimestamp == 0  // 0 when item creation day is unknown
          || (releaseTimestamp > 0 && startTime > releaseTimestamp + gracePeriodInMillis);
    }

    public int getDeletedSnapshots() {
      return deletedSnapshots;
    }

    public int getDeletedFiles() {
      return deletedFiles;
    }

  }

}
