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

import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry
import org.sonatype.nexus.proxy.repository.Repository
import org.sonatype.nexus.web.BaseUrlHolder

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * URL builder.
 *
 * @since 3.0
 */
@Named
@Singleton
class UrlBuilder
{

  @Inject
  RepositoryTypeRegistry repositoryTypeRegistry

  /**
   * @param repository get get content url for
   * @return repository content url
   */
  String getRepositoryContentUrl(final Repository repository) {
    def url = BaseUrlHolder.get()
    if (!url.endsWith('/')) {
      url += '/'
    }
    def descriptor = repositoryTypeRegistry.getRepositoryTypeDescriptor(repository.providerRole, repository.providerHint)
    return "${url}content/${descriptor.prefix}/${repository.pathPrefix}"
  }

  /**
   * @param repository get get content url for
   * @return repository content url if repository is exposed, null otherwise
   */
  @Nullable
  String getExposedRepositoryContentUrl(final Repository repository) {
    return repository.exposed ? getRepositoryContentUrl(repository) : null
  }

}
