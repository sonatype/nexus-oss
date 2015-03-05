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
package org.sonatype.nexus.proxy.item;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.util.DigesterUtils;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.io.ByteStreams;

/**
 * Reconciles any item attribute checksums affected by NEXUS-8178.
 *
 * @since 2.11.3
 */
@Named
public class ChecksumReconciler
    extends ComponentSupport
{
  private final Walker walker;

  private final DigestCalculatingInspector digestCalculatingInspector;

  private final MessageDigest message;

  @Inject
  public ChecksumReconciler(final Walker walker, final DigestCalculatingInspector digestCalculatingInspector)
      throws NoSuchAlgorithmException
  {
    this.walker = walker;
    this.digestCalculatingInspector = digestCalculatingInspector;
    message = MessageDigest.getInstance("SHA1");
  }

  /**
   * Walks the selected sub-tree in the given repository attempting to reconcile their item attribute checksums.
   */
  public void reconcileChecksums(final Repository repo, final ResourceStoreRequest request) {
    final WalkerContext context = new DefaultWalkerContext(repo, request);

    final String repositoryId = repo.getId();
    context.getProcessors().add(new AbstractFileWalkerProcessor()
    {
      @Override
      protected void processFileItem(final WalkerContext ctx, final StorageFileItem item) throws Exception {
        if (repositoryId.equals(item.getRepositoryId())) {
          reconcileItemChecksum(ctx.getRepository(), item);
        }
      }
    });

    try {
      walker.walk(context);
    }
    catch (final WalkerException e) {
      if (!(e.getWalkerContext().getStopCause() instanceof ItemNotFoundException)) {
        // everything that is not ItemNotFound should be reported, otherwise just neglect it
        throw e;
      }
    }
  }

  /**
   * Checks the checksum cached in the item's attributes to see if it needs reconciling with the stored content.
   */
  void reconcileItemChecksum(final Repository repo, final StorageFileItem item) throws Exception {
    final String attributeChecksum = item.getRepositoryItemAttributes().get(DigestCalculatingInspector.DIGEST_SHA1_KEY);
    if (attributeChecksum != null) {

      message.reset();
      // look for checksums affected by the link persister pre-fetching the first 8 bytes (see NEXUS-8178)
      try (final InputStream is = new DigestInputStream(item.getContentLocator().getContent(), message)) {
        final byte[] buf = new byte[8];
        ByteStreams.read(is, buf, 0, 8);
        message.update(buf);
        ByteStreams.copy(is, ByteStreams.nullOutputStream());
      }

      if (attributeChecksum.equals(DigesterUtils.getDigestAsString(message.digest()))) {
        log.info("Reconciling attribute checksums for {}", item);
        final RepositoryItemUid uid = item.getRepositoryItemUid();
        uid.getLock().lock(Action.update);
        try {
          digestCalculatingInspector.processStorageItem(item);
          repo.getAttributesHandler().storeAttributes(item);
        }
        finally {
          uid.getLock().unlock();
        }
      }
    }
  }
}
