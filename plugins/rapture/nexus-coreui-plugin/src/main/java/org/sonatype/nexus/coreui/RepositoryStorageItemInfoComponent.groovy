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
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.proxy.ResourceStoreRequest
import org.sonatype.nexus.proxy.item.StorageFileItem
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.item.StorageLinkItem
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.router.RepositoryRouter

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Repository Storage Item Info {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RepositoryStorageItemInfo')
class RepositoryStorageItemInfoComponent
extends DirectComponentSupport
{

  @Named("protected")
  @Inject
  RepositoryRegistry repositoryRegistry

  @Inject
  RepositoryRouter repositoryRouter

  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  RepositoryStorageItemInfoXO read(final String repositoryId, final String path) {
    def repository = repositoryRegistry.getRepository(repositoryId)
    def request = new ResourceStoreRequest(path, true, false)
    def item = repository.retrieveItem(request)
    def actualItem = item
    if (actualItem instanceof StorageLinkItem) {
      actualItem = repositoryRouter.dereferenceLink(actualItem, true, false)
    }
    def attributes = actualItem.getRepositoryItemAttributes()
    def info = new RepositoryStorageItemInfoXO(
        repositoryId: item.repositoryId,
        path: item.path,
        created: actualItem.created,
        modified: actualItem.modified,
        sha1: attributes.get(StorageFileItem.DIGEST_SHA1_KEY),
        md5: attributes.get(StorageFileItem.DIGEST_MD5_KEY),
    )
    if (actualItem instanceof StorageFileItem) {
      info.size = actualItem.length
    }
    return info
  }

}
