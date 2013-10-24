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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.AbstractRequestStrategy;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RequestStrategy;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.yum.Yum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link RequestStrategy} that applies to yum enabled proxy repositories that cleans up repository metadata
 * whenever {@code repomd.xml} changes (new from remote is being pulled).
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

  private final Walker walker;

  @Inject
  public ProxyMetadataRequestStrategy(final Walker walker) {
    this.walker = checkNotNull(walker);
  }

  @Override
  public void onRemoteAccess(ProxyRepository proxy, ResourceStoreRequest request, StorageItem item)
      throws ItemNotFoundException, IllegalOperationException
  {
    // do this only if request path in question asks for "repomd.xml"
    if (REPOMD_XML_PATH.equals(request.getRequestPath())) {
      try {
        log.trace("Cleaning up Yum metadata from {}:/{}", proxy.getId(), Yum.PATH_OF_REPODATA);
        final ResourceStoreRequest wrequest = new ResourceStoreRequest(request);
        wrequest.setRequestPath(RepositoryItemUid.PATH_ROOT + Yum.PATH_OF_REPODATA);
        wrequest.getRequestContext().put(AccessManager.REQUEST_AUTHORIZED, Boolean.TRUE);
        final DefaultWalkerContext wcontext = new DefaultWalkerContext(proxy, wrequest);
        wcontext.getProcessors().add(new ProxyMetadataCleanerProcessor());
        walker.walk(wcontext);
      }
      catch (WalkerException e) {
        Throwable stopCause = e.getWalkerContext().getStopCause();
        if (!(stopCause instanceof ItemNotFoundException)) {
          if (stopCause != null) {
            log.warn(
                "Failed to clean proxy YUM metadata due to {}/{}",
                stopCause.getClass().getName(), stopCause.getMessage(), log.isDebugEnabled() ? e : null
            );
          }
          else {
            log.warn(
                "Failed to clean proxy YUM metadata due to {}/{}",
                e.getClass().getName(), e.getMessage(), log.isDebugEnabled() ? e : null
            );
          }
        }
      }
    }
  }

  // ==

  private static final class ProxyMetadataCleanerProcessor
      extends AbstractFileWalkerProcessor
  {
    @Override
    protected void processFileItem(final WalkerContext context, final StorageFileItem fItem) {
      // delete all except the repomd.xml
      if (!fItem.getName().equals(Yum.NAME_OF_REPOMD_XML)) {
        try {
          context.getRepository().deleteItem(true, fItem.getResourceStoreRequest());
        }
        catch (Exception e) {
          log.warn(
              "Failed to clean proxy YUM metadata due to {}/{}",
              e.getClass().getName(), e.getMessage(), log.isDebugEnabled() ? e : null
          );
        }
      }
    }
  }
}
