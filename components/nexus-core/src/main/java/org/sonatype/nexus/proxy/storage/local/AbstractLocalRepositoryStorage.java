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

package org.sonatype.nexus.proxy.storage.local;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreIteratorRequest;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ChecksummingContentLocator;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

/**
 * Abstract Storage class. It have ID and defines logger. Predefines all write methods to be able to "decorate"
 * StorageItems with attributes if supported.
 *
 * @author cstamas
 */
public abstract class AbstractLocalRepositoryStorage
    extends AbstractLoggingComponent
    implements LocalRepositoryStorage
{
  /**
   * Key used to mark a repository context as "initialized". This flag and the generation together controls how the
   * context is about to be updated. See NEXUS-5145.
   */
  private static final String CONTEXT_UPDATED_KEY = AbstractLocalRepositoryStorage.class.getName() + ".updated";

  /**
   * The wastebasket.
   */
  private final Wastebasket wastebasket;

  /**
   * The default Link persister.
   */
  private final LinkPersister linkPersister;

  /**
   * The MIME support.
   */
  private final MimeSupport mimeSupport;

  /**
   * Since storages are shared, we are tracking the last changes from each of them.
   */
  private final Map<String, Integer> repositoryContexts;

  protected AbstractLocalRepositoryStorage(final Wastebasket wastebasket, final LinkPersister linkPersister,
                                           final MimeSupport mimeSupport)
  {
    this.wastebasket = wastebasket;
    this.linkPersister = linkPersister;
    this.mimeSupport = mimeSupport;
    this.repositoryContexts = new HashMap<String, Integer>();
  }

  protected Wastebasket getWastebasket() {
    return wastebasket;
  }

  protected LinkPersister getLinkPersister() {
    return linkPersister;
  }

  protected MimeSupport getMimeSupport() {
    return mimeSupport;
  }

  // ==

  /**
   * Remote storage specific, when the remote connection settings are actually applied.
   */
  protected void updateContext(Repository repository, LocalStorageContext context)
      throws LocalStorageException
  {
    // empty, override if needed
  }

  protected synchronized LocalStorageContext getLocalStorageContext(Repository repository)
      throws LocalStorageException
  {
    final LocalStorageContext ctx = repository.getLocalStorageContext();
    if (ctx != null) {
      // we have repo specific settings
      // if repositoryContexts does not contain this context ID, or
      // if localStorageContext does not contain CONTEXT_UPDATED_KEY, or
      // if repositoryContext generation is less than localStorageContext generation
      if (!repositoryContexts.containsKey(repository.getId()) || !ctx.hasContextObject(CONTEXT_UPDATED_KEY)
          || ctx.getGeneration() > repositoryContexts.get(repository.getId())) {
        if (getLogger().isDebugEnabled()) {
          if (!repositoryContexts.containsKey(repository.getId())) {
            getLogger().debug("Local context {} is about to be initialized", ctx);
          }
          else {
            getLogger().debug("Local context {} has been changed. Previous generation {}",
                new Object[]{ctx, repositoryContexts.get(repository.getId())});
          }
        }

        updateContext(repository, repository.getLocalStorageContext());
        ctx.putContextObject(CONTEXT_UPDATED_KEY, Boolean.TRUE);
        repositoryContexts.put(repository.getId(),
            Integer.valueOf(repository.getLocalStorageContext().getGeneration()));
      }
    }
    return ctx;
  }

  // ==

  /**
   * Gets the absolute url from base.
   *
   * @param uid the uid
   * @return the absolute url from base
   */
  @Deprecated
  public URL getAbsoluteUrlFromBase(Repository repository, ResourceStoreRequest request)
      throws LocalStorageException
  {
    StringBuilder urlStr = new StringBuilder(repository.getLocalUrl());

    if (request.getRequestPath().startsWith(RepositoryItemUid.PATH_SEPARATOR)) {
      urlStr.append(request.getRequestPath());
    }
    else {
      urlStr.append(RepositoryItemUid.PATH_SEPARATOR).append(request.getRequestPath());
    }
    try {
      return new URL(urlStr.toString());
    }
    catch (MalformedURLException e) {
      try {
        return new File(urlStr.toString()).toURI().toURL();
      }
      catch (MalformedURLException e1) {
        throw new LocalStorageException("The local storage has a malformed URL as baseUrl!", e);
      }
    }
  }

  public final void deleteItem(Repository repository, ResourceStoreRequest request)
      throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
  {
    getWastebasket().delete(this, repository, request);
  }

  // ==

  public Iterator<StorageItem> iterateItems(Repository repository, ResourceStoreIteratorRequest request)
      throws ItemNotFoundException, LocalStorageException
  {
    throw new UnsupportedOperationException("Iteration not supported!");
  }

  // ==

  protected void prepareStorageFileItemForStore(final StorageFileItem item)
      throws LocalStorageException
  {
    try {
      // replace content locator
      ChecksummingContentLocator sha1cl =
          new ChecksummingContentLocator(item.getContentLocator(), MessageDigest.getInstance("SHA1"),
              StorageFileItem.DIGEST_SHA1_KEY, item.getItemContext());

      // md5 is deprecated but still calculated
      ChecksummingContentLocator md5cl =
          new ChecksummingContentLocator(sha1cl, MessageDigest.getInstance("MD5"),
              StorageFileItem.DIGEST_MD5_KEY, item.getItemContext());

      item.setContentLocator(md5cl);
    }
    catch (NoSuchAlgorithmException e) {
      throw new LocalStorageException(
          "The JVM does not support SHA1 MessageDigest or MD5 MessageDigest, that is essential for Nexus. We cannot write to local storage! Please run Nexus on JVM that does provide these MessageDigests.",
          e);
    }
  }
}
