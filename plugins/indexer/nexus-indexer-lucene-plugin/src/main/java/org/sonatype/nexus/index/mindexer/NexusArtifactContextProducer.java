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
package org.sonatype.nexus.index.mindexer;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.google.common.base.Strings;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.context.IndexingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * NX enhanced {@link DefaultArtifactContextProducer} that gets missing SHA1 hashes from NX.
 *
 * @since 2.11
 */
@Named("default")
@Singleton
public class NexusArtifactContextProducer
    extends DefaultArtifactContextProducer
{
  private final Logger log;

  private final RepositoryRegistry repositoryRegistry;

  @Inject
  public NexusArtifactContextProducer(final RepositoryRegistry repositoryRegistry) {
    this.log = LoggerFactory.getLogger(getClass());
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    log.info("NexusArtifactContextProducer created");
  }

  @Override
  public ArtifactContext getArtifactContext(final IndexingContext context, final File file) {
    final ArtifactContext ac = super.getArtifactContext(context, file);
    if (ac != null && ac.getArtifactInfo().sha1 == null) {
      // special case for proxies
      final String repositoryId = context.getRepositoryId();
      String itemPath = file.getPath();
      try {
        final MavenProxyRepository proxyRepository = repositoryRegistry.getRepositoryWithFacet(repositoryId,
            MavenProxyRepository.class);
        final File baseDir = getRepositoryLocalStorageFile(proxyRepository, RepositoryItemUid.PATH_ROOT);
        itemPath = itemPath.substring(baseDir.getPath().length());
        if (!itemPath.startsWith(RepositoryItemUid.PATH_SEPARATOR)) {
          itemPath = RepositoryItemUid.PATH_SEPARATOR + itemPath;
        }
        final StorageItem item = proxyRepository.retrieveItem(new ResourceStoreRequest(itemPath, true, false));
        if (item instanceof StorageFileItem) {
          final String sha1 = item.getRepositoryItemAttributes().get(StorageFileItem.DIGEST_SHA1_KEY);
          if (!Strings.isNullOrEmpty(sha1)) {
            log.info("ArtifactContext of {} enhanced with sha1={}", itemPath, sha1);
            ac.getArtifactInfo().sha1 = sha1;
          }
        }
      }
      catch (NoSuchRepositoryException e) {
        // this means repo being scanned is not a maven proxy, just ignore
        log.debug("Repository {} is not a maven proxy", repositoryId);
      }
      catch (ItemNotFoundException e) {
        // possible, as MI scanning does not use UID locks but scans FS directly. Ignore it.
        log.debug("Item not found for file {} via {}:{}", file.getAbsolutePath(), repositoryId, itemPath, e);
      }
      catch (Exception e) {
        // IOEx means storage problems ahead
        // IllegalOperationException: this should not happen for reads
        // AccessDeniedException: this should not happen as tasks are running as admin
        log.warn("IO problem during retrieve of {}:{}", repositoryId, itemPath, e);
      }
    }
    return ac;
  }

  private File getRepositoryLocalStorageFile(final Repository repository, final String path) {
    if (repository.getLocalUrl() != null && repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage) {
      try {
        return
            ((DefaultFSLocalRepositoryStorage) repository.getLocalStorage())
                .getBaseDir(repository, new ResourceStoreRequest(path));
      }
      catch (LocalStorageException e) {
        log.warn("Cannot get {} file from {} repository's LS", path, repository.getId(), e);
      }
    }
    return null;
  }
}
