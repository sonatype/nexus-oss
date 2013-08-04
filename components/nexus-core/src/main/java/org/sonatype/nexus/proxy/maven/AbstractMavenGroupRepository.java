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

package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

import org.codehaus.plexus.component.annotations.Requirement;

public abstract class AbstractMavenGroupRepository
    extends AbstractGroupRepository
    implements MavenGroupRepository
{
  /**
   * Metadata manager.
   */
  @Requirement
  private MetadataManager metadataManager;

  /**
   * The artifact packaging mapper.
   */
  @Requirement
  private ArtifactPackagingMapper artifactPackagingMapper;

  private ArtifactStoreHelper artifactStoreHelper;

  private RepositoryKind repositoryKind;

  @Override
  protected AbstractMavenGroupRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (AbstractMavenGroupRepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  public RepositoryKind getRepositoryKind() {
    if (repositoryKind == null) {
      repositoryKind =
          new DefaultRepositoryKind(GroupRepository.class,
              Arrays.asList(new Class<?>[]{MavenGroupRepository.class}));
    }
    return repositoryKind;
  }

  public boolean isMergeMetadata() {
    return getExternalConfiguration(false).isMergeMetadata();
  }

  public void setMergeMetadata(boolean mergeMetadata) {
    getExternalConfiguration(true).setMergeMetadata(mergeMetadata);
  }

  public ArtifactPackagingMapper getArtifactPackagingMapper() {
    return artifactPackagingMapper;
  }

  public ArtifactStoreHelper getArtifactStoreHelper() {
    if (artifactStoreHelper == null) {
      artifactStoreHelper = new ArtifactStoreHelper(this);
    }

    return artifactStoreHelper;
  }

  public MetadataManager getMetadataManager() {
    return metadataManager;
  }

  public boolean recreateMavenMetadata(ResourceStoreRequest request) {
    if (!getLocalStatus().shouldServiceRequest()) {
      return false;
    }

    boolean result = false;

    for (Repository repository : getMemberRepositories()) {
      if (repository.getRepositoryKind().isFacetAvailable(MavenRepository.class)) {
        result |= ((MavenRepository) repository).recreateMavenMetadata(request);
      }
    }

    return result;
  }

  public RepositoryPolicy getRepositoryPolicy() {
    return RepositoryPolicy.MIXED;
  }

  public void setRepositoryPolicy(RepositoryPolicy repositoryPolicy) {
    throw new UnsupportedOperationException(
        "Setting repository policy on a Maven group repository is not possible!");
  }

  public boolean isMavenArtifact(StorageItem item) {
    return isMavenArtifactPath(item.getPath());
  }

  public boolean isMavenMetadata(StorageItem item) {
    return isMavenMetadataPath(item.getPath());
  }

  public boolean isMavenArtifactPath(String path) {
    return getGavCalculator().pathToGav(path) != null;
  }

  public abstract boolean isMavenMetadataPath(String path);

  public void storeItemWithChecksums(ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes)
      throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
             StorageException, AccessDeniedException
  {
    getArtifactStoreHelper().storeItemWithChecksums(request, is, userAttributes);
  }

  public void storeItemWithChecksums(boolean fromTask, AbstractStorageItem item)
      throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
  {
    getArtifactStoreHelper().storeItemWithChecksums(fromTask, item);
  }

  public void deleteItemWithChecksums(ResourceStoreRequest request)
      throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
             StorageException, AccessDeniedException
  {
    getArtifactStoreHelper().deleteItemWithChecksums(request);
  }

  public void deleteItemWithChecksums(boolean fromTask, ResourceStoreRequest request)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
  {
    getArtifactStoreHelper().deleteItemWithChecksums(fromTask, request);
  }

}
