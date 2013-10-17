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

package org.sonatype.nexus.yum.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.AbstractRequestStrategy;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestStrategy;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.internal.task.MergeMetadataTask;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A request strategy that applies to yum enabled groups that will fetch repomd.xml from each proxy member repository
 * before serving group repomd.xml. If repomd.xml changes (new one is downloaded) it will trigger a re-merge so served
 * group repomd.xml will be up to date.
 *
 * @since 2.7
 */
@Named
@Singleton
public class MergeMetadataRequestStrategy
    extends AbstractRequestStrategy
    implements RequestStrategy
{

  private static final Logger log = LoggerFactory.getLogger(MergeMetadataRequestStrategy.class);

  private static final String PATH_OF_REPOMD_XML = "/" + Yum.PATH_OF_REPOMD_XML;

  private final NexusScheduler nexusScheduler;

  public MergeMetadataRequestStrategy(final NexusScheduler nexusScheduler) {
    this.nexusScheduler = checkNotNull(nexusScheduler);
  }

  @Override
  public void onHandle(final Repository repository, final ResourceStoreRequest request, final Action action) {
    GroupRepository groupRepository = repository.adaptToFacet(GroupRepository.class);
    final String requestPath = request.getRequestPath();
    if (Action.read.equals(action) && requestPath.endsWith(PATH_OF_REPOMD_XML) && groupRepository != null) {
      boolean shouldMerge = false;
      for (Repository member : groupRepository.getMemberRepositories()) {
        if (member.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
          try {
            log.debug("Fetch local {}:{}", member.getId(), PATH_OF_REPOMD_XML);
            StorageItem memberRepoMd = retrieveRepoMd(member, true);
            String sha1Local = memberRepoMd.getRepositoryItemAttributes().get(StorageFileItem.DIGEST_SHA1_KEY);
            log.debug("Fetch remote {}:{}", member.getId(), PATH_OF_REPOMD_XML);
            memberRepoMd = retrieveRepoMd(member, false);
            String sha1After = memberRepoMd.getRepositoryItemAttributes().get(StorageFileItem.DIGEST_SHA1_KEY);
            if (!StringUtils.equals(sha1Local, sha1After)) {
              log.debug("{}:{} changed. Will merge after fetching all members.", member.getId(), PATH_OF_REPOMD_XML);
              shouldMerge = true;
            }
          }
          catch (Exception e) {
            log.debug(
                "Could not retrieve {} from {}, member of yum enabled group {}. Ignoring.",
                PATH_OF_REPOMD_XML, member.getId(), groupRepository.getId(), e
            );
          }
        }
      }
      if (shouldMerge) {
        try {
          // create merge task and wait for it to finish
          MergeMetadataTask.createTaskFor(nexusScheduler, groupRepository).get();
        }
        catch (Exception e) {
          log.debug(
              "Could not merge Yum metadata on group {} due to {}/{}",
              groupRepository.getId(), e.getClass().getName(), e.getMessage(), log.isDebugEnabled() ? e : null
          );
        }
      }
    }
  }

  private StorageItem retrieveRepoMd(final Repository member, boolean localOnly) throws Exception {
    try {
      return member.retrieveItem(new ResourceStoreRequest(PATH_OF_REPOMD_XML, localOnly, false));
    }
    catch (ItemNotFoundException e) {
      // proxy repo is not a yum repository, go on
    }
    return null;
  }

}