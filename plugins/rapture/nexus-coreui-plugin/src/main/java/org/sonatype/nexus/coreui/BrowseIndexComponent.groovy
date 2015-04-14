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
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.commons.io.FilenameUtils
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.common.validation.Validate
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.proxy.registry.RepositoryRegistry

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Browse Index {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_BrowseIndex')
class BrowseIndexComponent
extends DirectComponentSupport
{

  @Named("protected")
  @Inject
  RepositoryRegistry protectedRepositoryRegistry

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
    // ******** browse index temporarily disabled ********
    return null;
//    def repository = protectedRepositoryRegistry.getRepository(repositoryId)
//    TreeNode node = indexerManager.listNodes(new IndexBrowserTreeNodeFactory(repository, ''), path, repositoryId)
//
//    Closure asChild
//    asChild = { TreeNode child ->
//      new RepositoryStorageItemXO(
//          repositoryId: repositoryId,
//          path: child.path,
//          text: child.nodeName,
//          leaf: child.leaf,
//          processed: child.leaf || !child.children?.isEmpty(),
//          children: child.children?.collect(asChild),
//          type: child.leaf ? FilenameUtils.getExtension(child.nodeName) : null
//      )
//    }
//    return node?.children?.collect(asChild)
  }

}
