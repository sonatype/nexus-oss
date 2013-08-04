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

package org.sonatype.nexus.proxy.repository;

import java.util.Collection;

import org.sonatype.nexus.proxy.ResourceStoreRequest;

/**
 * AbstractRepositoryMetadataManager specialization leaning to repositories that has "centralized" meta data files
 * (usually few files put in well know places). Repository like these usually have "central meta data" or one single
 * (or
 * multiple, but way lass that artifacts for example) monolithic meta data. These usually make possible more "stable"
 * or
 * "consistent" metadata states, but slower "turn-around" as artifact being deployed will remain non visible until
 * central metadata is updated and enlist the new artifact. Typical example is OBR or P2 repository layout.
 *
 * @author cstamas
 * @since 2.1
 */
public abstract class AbstractRepositoryCentralizedMetadataManager
    extends AbstractRepositoryMetadataManager
{
  public AbstractRepositoryCentralizedMetadataManager(final Repository repository) {
    super(repository);
  }

  @Override
  public boolean expireMetadataCaches(final ResourceStoreRequest request) {
    boolean cacheChanged = false;
    for (String metadataPath : getMetadataFilePaths()) {
      final ResourceStoreRequest expireRequest = new ResourceStoreRequest(request);
      expireRequest.setRequestPath(metadataPath);
      boolean expired = getRepository().expireCaches(expireRequest, null);
      cacheChanged = cacheChanged || expired;
    }
    return cacheChanged;
  }

  @Override
  public boolean expireNotFoundMetadataCaches(final ResourceStoreRequest request) {
    boolean cacheChanged = false;
    for (String metadataPath : getMetadataFilePaths()) {
      final ResourceStoreRequest expireRequest = new ResourceStoreRequest(request);
      expireRequest.setRequestPath(metadataPath);
      boolean expired = getRepository().expireNotFoundCaches(expireRequest, null);
      cacheChanged = cacheChanged || expired;
    }
    return cacheChanged;
  }

  // ==

  /**
   * Returns "normalized" (absolute, starting with leading "/" (slash)) paths of the known metadata files to be found
   * in repository. Must not return {@code null}.
   */
  protected abstract Collection<String> getMetadataFilePaths();
}
