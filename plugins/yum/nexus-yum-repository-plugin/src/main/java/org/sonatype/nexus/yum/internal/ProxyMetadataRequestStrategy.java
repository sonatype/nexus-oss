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
package org.sonatype.nexus.yum.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.AbstractRequestStrategy;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestStrategy;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumGroup;
import org.sonatype.nexus.yum.YumRegistry;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 *
 * @since 2.7.0
 */
@Named
@Singleton
public class ProxyMetadataRequestStrategy
    extends AbstractRequestStrategy
{
  private static final Logger log = LoggerFactory.getLogger(ProxyMetadataRequestStrategy.class);

  private static final String REPOMD_XML_PATH = "/" + Yum.PATH_OF_REPOMD_XML;

  private final RepositoryRegistry repositoryRegistry;

  private final YumRegistry yumRegistry;

  @Inject
  public ProxyMetadataRequestStrategy(final RepositoryRegistry repositoryRegistry,
                                      final YumRegistry yumRegistry)
  {
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    this.yumRegistry = checkNotNull(yumRegistry);
  }

  @Override
  public void onHandle(final Repository repository, final ResourceStoreRequest request, final Action action) {
    if (request.getRequestPath().startsWith("/" + Yum.PATH_OF_REPODATA)) {
      try {
        long now = System.currentTimeMillis();

        log.trace("Checking if remote {}:repomd.xml changed", repository.getId());
        StorageFileItem repoMDItem = (StorageFileItem) repository.retrieveItem(
            false, new ResourceStoreRequest(REPOMD_XML_PATH)
        );
        if (repoMDItem.getRepositoryItemAttributes().getStoredLocally() > now) {
          MetadataRewriter.rewriteProxiedMetadata((ProxyRepository) repository);

          log.trace("Yum metadata changed for {}. Looking if we should merge it...", repository.getId());
          List<GroupRepository> groups = repositoryRegistry.getGroupsOfRepository(repository);
          for (GroupRepository group : groups) {
            Yum yum = yumRegistry.get(group.getId());
            if (yum != null && yum instanceof YumGroup) {
              ((YumGroup) yum).markDirty();
            }
          }
        }
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  }

}
