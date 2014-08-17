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
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.commons.io.FilenameUtils
import org.apache.maven.index.IteratorSearchResponse
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.index.IndexerManager
import org.sonatype.nexus.proxy.AccessDeniedException
import org.sonatype.nexus.proxy.ItemNotFoundException
import org.sonatype.nexus.proxy.NoSuchRepositoryException
import org.sonatype.nexus.proxy.RepositoryNotAvailableException
import org.sonatype.nexus.proxy.ResourceStoreRequest
import org.sonatype.nexus.proxy.access.AccessManager
import org.sonatype.nexus.proxy.item.StorageCollectionItem
import org.sonatype.nexus.proxy.item.StorageFileItem
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.item.StorageLinkItem
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.repository.Repository
import org.sonatype.nexus.proxy.router.RepositoryRouter
import org.sonatype.nexus.validation.Validate

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import static org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY

/**
 * Repository Storage {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RepositoryStorage')
class RepositoryStorageComponent
extends DirectComponentSupport
{

  @Named("protected")
  @Inject
  RepositoryRegistry protectedRepositoryRegistry

  @Inject
  RepositoryRouter repositoryRouter

  @Inject
  IndexerManager indexerManager

  /**
   * Retrieves children of specified path.
   *
   * @param repositoryId containing the path
   * @param path to retrieve children for
   * @return list of children
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  @Validate
  List<RepositoryStorageItemXO> readChildren(final @NotEmpty(message = '[repositoryId] may not be empty') String repositoryId,
                                             final @NotEmpty(message = '[path] may not be empty') String path)
  {
    def repository = protectedRepositoryRegistry.getRepository(repositoryId)
    def request = new ResourceStoreRequest(path, true, false)
    StorageItem item = repository.retrieveItem(request)
    def itemXOs = render(item)
    itemXOs.each { itemXO ->
      itemXO.repositoryId = repositoryId
      if (itemXO.leaf) {
        itemXO.type = FilenameUtils.getExtension(itemXO.text)
      }
    }
    return itemXOs
  }

  /**
   * Retrieves generic information about a storage item specified by path.
   *
   * @param repositoryId containing storage item
   * @param path of storage item
   * @return generic info
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  @Validate
  RepositoryStorageItemInfoXO readInfo(final @NotEmpty(message = '[repositoryId] may not be empty') String repositoryId,
                                       final @NotEmpty(message = '[path] may not be empty') String path)
  {
    def repository = protectedRepositoryRegistry.getRepository(repositoryId)
    def request = new ResourceStoreRequest(path, true, false)
    try {
      StorageItem item = repository.retrieveItem(request)
      if (item instanceof StorageLinkItem) {
        item = repositoryRouter.dereferenceLink(item, true, false)
      }
      return new RepositoryStorageItemInfoXO(
          repositoryId: repositoryId,
          path: item.path,
          inLocalStorage: true,
          createdBy: item.repositoryItemAttributes.get(AccessManager.REQUEST_USER),
          created: new Date(item.created),
          modified: new Date(item.modified),
          sha1: item.repositoryItemAttributes.get(StorageFileItem.DIGEST_SHA1_KEY),
          md5: item.repositoryItemAttributes.get(StorageFileItem.DIGEST_MD5_KEY),
          size: item instanceof StorageFileItem ? item.length : null,
          repositories: findContainingRepositories(item).collect {
            new ReferenceXO(id: it.id, name: it.name)
          }
      )
    }
    catch (ItemNotFoundException e) {
      return new RepositoryStorageItemInfoXO(
          repositoryId: repositoryId,
          path: path,
          inLocalStorage: false
      )
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @Validate
  void delete_(final @NotEmpty(message = '[id] may not be empty') String id,
               final String path)
  {
    protectedRepositoryRegistry.getRepository(id).deleteItem(new ResourceStoreRequest(path, true))
    log.info "Storage item(s) on path \"${path}\" (and below) were deleted from repository [${id}]"
  }

  List<RepositoryStorageItemXO> render(final StorageItem item) {
    if (item instanceof StorageFileItem) {
      return render(item as StorageFileItem);
    }
    else if (item instanceof StorageLinkItem) {
      return render(item as StorageLinkItem);
    }
    else if (item instanceof StorageCollectionItem) {
      return render(item as StorageCollectionItem);
    }
    return null
  }

  List<RepositoryStorageItemXO> render(final StorageFileItem item) {
    def itemXO = new RepositoryStorageItemXO(
        repositoryId: item.repositoryId,
        path: item.path,
        text: item.name,
        leaf: true
    )
    if (item.itemContext.containsKey(OVERRIDE_FILENAME_KEY)) {
      itemXO.text = item.itemContext.get(OVERRIDE_FILENAME_KEY) as String;
    }
    return [itemXO]
  }

  List<RepositoryStorageItemXO> render(final StorageLinkItem item) {
    def actualItem = repositoryRouter.dereferenceLink(item, true, false)
    def itemXO = new RepositoryStorageItemXO(
        repositoryId: item.repositoryId,
        path: item.path,
        text: actualItem.name,
        leaf: true
    )
    if (actualItem.itemContext.containsKey(OVERRIDE_FILENAME_KEY)) {
      itemXO.text = actualItem.itemContext.get(OVERRIDE_FILENAME_KEY) as String;
    }
    return [itemXO]
  }

  List<RepositoryStorageItemXO> render(final StorageCollectionItem item) {
    return item.list().findResults { StorageItem child ->
      child.repositoryItemUid.getBooleanAttributeValue(IsHiddenAttribute.class) ? null : child
    }.collect() { child ->
      def actualChild = child;
      if (actualChild instanceof StorageLinkItem) {
        actualChild = repositoryRouter.dereferenceLink(actualChild, true, false)
      }
      def itemXO = new RepositoryStorageItemXO(
          repositoryId: child.repositoryId,
          path: child.path,
          text: actualChild.name,
          leaf: !(actualChild instanceof StorageCollectionItem)
      )
      if (actualChild.itemContext.containsKey(OVERRIDE_FILENAME_KEY)) {
        itemXO.text = actualChild.itemContext.get(OVERRIDE_FILENAME_KEY) as String;
      }
      return itemXO
    }
  }

  Set<Repository> findContainingRepositories(final StorageItem item) {
    Set<Repository> repositories = [item.repositoryItemUid.repository]
    // search item by checksum
    String sha1 = item.repositoryItemAttributes.get(StorageFileItem.DIGEST_SHA1_KEY)
    if (sha1) {
      IteratorSearchResponse searchResponse = null
      try {
        searchResponse = indexerManager.searchArtifactSha1ChecksumIterator(sha1, null, null, null, null, null)
        searchResponse.each {
          try {
            repositories << protectedRepositoryRegistry.getRepository(it.repository)
          }
          catch (NoSuchRepositoryException e) {
            log.trace 'Repository not found even if present in search results; ignoring', e
          }
        }
      }
      finally {
        searchResponse?.close()
      }
    }
    // search repositories
    protectedRepositoryRegistry.repositories.each { repository ->
      if (!repositories.contains(repository)) {
        ResourceStoreRequest request = new ResourceStoreRequest(item.getPath(), true)
        if (repository.localStorage.containsItem(repository, request)) {
          try {
            StorageItem similarItem = repository.retrieveItem(request)
            if (!sha1 || sha1 == similarItem.repositoryItemAttributes.get(StorageFileItem.DIGEST_SHA1_KEY)) {
              repositories << repository
            }
          }
          catch (AccessDeniedException e) {
            // that is fine, user doesn't have access
          }
          catch (RepositoryNotAvailableException e) {
            // this could happen normally if a repository is not available, do not complain too loudly
            log.trace 'Repository not available; ignoring', e
          }
          catch (Exception e) {
            log.error e.getMessage(), e
          }
        }
      }
    }
    return repositories
  }

}
