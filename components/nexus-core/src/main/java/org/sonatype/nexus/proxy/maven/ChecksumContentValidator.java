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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

import static org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector.DIGEST_MD5_KEY;
import static org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector.DIGEST_SHA1_KEY;

/**
 * Maven checksum content validator.
 *
 * @author cstamas
 */
@Named(ChecksumContentValidator.ID)
@Singleton
public class ChecksumContentValidator
    extends AbstractChecksumContentValidator
    implements ItemContentValidator
{
  public static final String ID = "ChecksumContentValidator";

  public static final String SUFFIX_MD5 = ".md5";

  public static final String SUFFIX_SHA1 = ".sha1";

  /**
   * Key of item attribute that holds contents of remote .sha1 file.
   *
   * The attribute is not present if the item does not have corresponding .sha1 file.
   */
  public static final String ATTR_REMOTE_SHA1 = "remote.sha1";

  /**
   * Key of item attribute that holds contents of remote .md5 file.
   *
   * The attribute is not present if the item does not have corresponding .md5 file.
   */
  public static final String ATTR_REMOTE_MD5 = "remote.md5";

  @Override
  protected void cleanup(ProxyRepository proxy, RemoteHashResponse remoteHash, boolean contentValid)
      throws LocalStorageException
  {
    if (!contentValid && remoteHash != null && remoteHash.getHashItem() != null) {
      // TODO should we remove bad checksum if policy==WARN?
      try {
        String path = remoteHash.getHashItem().getRepositoryItemUid().getPath();
        proxy.getLocalStorage().deleteItem(proxy, new ResourceStoreRequest(path, true));
      }
      catch (ItemNotFoundException | UnsupportedStorageOperationException e) {
        // ignore
      }
    }
  }

  @Override
  protected ChecksumPolicy getChecksumPolicy(ProxyRepository proxy, AbstractStorageItem item) {
    if (isChecksum(item.getRepositoryItemUid().getPath())) {
      // do not validate checksum files
      return null;
    }

    if (!proxy.getRepositoryKind().isFacetAvailable(MavenProxyRepository.class)) {
      // we work only with maven proxy reposes, all others are neglected
      return null;
    }

    MavenProxyRepository mpr = proxy.adaptToFacet(MavenProxyRepository.class);

    ChecksumPolicy checksumPolicy = mpr.getChecksumPolicy();

    if (checksumPolicy == null || !checksumPolicy.shouldCheckChecksum()
        || !(item instanceof DefaultStorageFileItem)) {
      // there is either no need to validate or we can't validate the item content
      return null;
    }

    return checksumPolicy;
  }

  @Override
  protected RemoteHashResponse retrieveRemoteHash(AbstractStorageItem item, ProxyRepository proxy, String baseUrl)
      throws LocalStorageException
  {
    RepositoryItemUid uid = item.getRepositoryItemUid();

    ResourceStoreRequest request = new ResourceStoreRequest(item);

    RemoteHashResponse response = null;
    try {
      // we prefer SHA1 ...
      request.pushRequestPath(uid.getPath() + SUFFIX_SHA1);
      try {
        response = doRetrieveSHA1(proxy, request, item);
      }
      finally {
        request.popRequestPath();
      }
    }
    catch (ItemNotFoundException e) {
      // ... but MD5 will do too
      request.pushRequestPath(uid.getPath() + SUFFIX_MD5);
      try {
        response = doRetrieveMD5(proxy, request, item);
      }
      catch (ItemNotFoundException e1) {
        log.debug("Item checksums (SHA1, MD5) remotely unavailable: {}", uid);
      }
      finally {
        request.popRequestPath();
      }
    }

    return response;
  }

  private boolean isChecksum(String path) {
    return path.endsWith(SUFFIX_SHA1) || path.endsWith(SUFFIX_MD5);
  }

  public static RemoteHashResponse doRetrieveSHA1(ProxyRepository proxy, ResourceStoreRequest hashRequest,
                                                  StorageItem artifact)
      throws LocalStorageException, ItemNotFoundException
  {
    return doRetrieveChecksumItem(proxy, hashRequest, artifact, DIGEST_SHA1_KEY, ATTR_REMOTE_SHA1);
  }

  public static RemoteHashResponse doRetrieveMD5(ProxyRepository proxy, ResourceStoreRequest hashRequest,
                                                 StorageItem artifact)
      throws LocalStorageException, ItemNotFoundException
  {
    return doRetrieveChecksumItem(proxy, hashRequest, artifact, DIGEST_MD5_KEY, ATTR_REMOTE_MD5);
  }

  /**
   * Attempt to retrieve the checksum for the given artifact storage-item.
   *
   * If the checksum is cached in item attributes, then it is returned.  Otherwise attempt to fetch
   * the remote checksum from upstream and cache in attributes.
   */
  private static RemoteHashResponse doRetrieveChecksumItem(ProxyRepository proxy,
                                                           ResourceStoreRequest checksumRequest,
                                                           StorageItem artifact,
                                                           String inspector,
                                                           String attrname)
      throws ItemNotFoundException, LocalStorageException
  {
    final RepositoryItemUid itemUid = artifact.getRepositoryItemUid();
    itemUid.getLock().lock(Action.read);
    try {
      final Attributes attributes = artifact.getRepositoryItemAttributes();

      if (attributes == null) {
        throw new LocalStorageException("Null item repository attributes");
      }

      // check/expire checksum item NFC or throw not-found exception
      proxy.maintainNotFoundCache(checksumRequest);

      // If attributes does not contain checksum hash, then attempt to fetch and store it
      String hash = attributes.get(attrname);

      // FIXME: This is unable to detect if the checksum in the proxy is _old_ and needs to be checked in the remote again
      // FIXME: If we add a check here, then we also have to cope with the proxy-cache invalidation token which is an
      // FIXME: optimization to expire entire trees, but if not handled here specifically it will cause more problems

      if (hash == null || checksumRequest.isRequestAsExpired()) {
        try {
          StorageFileItem checksumItem =
              (StorageFileItem) proxy.getRemoteStorage().retrieveItem(proxy, checksumRequest, proxy.getRemoteUrl());

          // fetched checksum item, extract hash and store it in attributes
          hash = doStoreChecksumItem(proxy, artifact, attrname, checksumItem);

          // evict checksum item NFC now that it is found
          proxy.removeFromNotFoundCache(checksumRequest);
        }
        catch (ItemNotFoundException | RemoteStorageException e) {
          // could not fetch checksum, add request to NFC
          proxy.addToNotFoundCache(checksumRequest);
        }
      }

      if (hash != null) {
        return new RemoteHashResponse(inspector, hash, newHashItem(proxy, checksumRequest, artifact, hash));
      }
      else {
        throw new ItemNotFoundException(checksumRequest);
      }
    }
    catch (IOException e) {
      throw new LocalStorageException(e);
    }
    finally {
      itemUid.getLock().unlock();
    }
  }

  public static void doStoreSHA1(ProxyRepository proxy, StorageItem artifact, StorageFileItem checksumItem)
      throws LocalStorageException
  {
    try {
      doStoreChecksumItem(proxy, artifact, ATTR_REMOTE_SHA1, checksumItem);
    }
    catch (IOException e) {
      throw new LocalStorageException(e);
    }
  }

  public static void doStoreMD5(ProxyRepository proxy, StorageItem artifact, StorageFileItem checksumItem)
      throws LocalStorageException
  {
    try {
      doStoreChecksumItem(proxy, artifact, ATTR_REMOTE_MD5, checksumItem);
    }
    catch (IOException e) {
      throw new LocalStorageException(e);
    }
  }

  /**
   * Store checksum information for given artifact in attributes.
   *
   * @param proxy         Proxy repository where artifact lives.
   * @param artifact      Artifact storage-item.
   * @param attrname      Checksum attribute name.
   * @param checksumItem  Checksum storage-item.
   * @return              Checksum hash, read from checksum storage-item.
   */
  private static String doStoreChecksumItem(final ProxyRepository proxy,
                                            final StorageItem artifact,
                                            final String attrname,
                                            final StorageFileItem checksumItem)
      throws IOException
  {
    // Load checksum hash to store in attributes from item
    String hash = MUtils.readDigestFromFileItem(checksumItem);

    // Write checksum details to attributes
    final RepositoryItemUid itemUid = artifact.getRepositoryItemUid();
    itemUid.getLock().lock(Action.update);
    final Attributes attributes = artifact.getRepositoryItemAttributes();
    try {
      attributes.put(attrname, hash);

      // HACK: Clean up turds in attributes from previous not-found handling
      if (ATTR_REMOTE_SHA1.equals(attrname)) {
        attributes.remove("remote.no-sha1");
      }
      if (ATTR_REMOTE_MD5.equals(attrname)) {
        attributes.remove("remote.no-md5");
      }

      proxy.getAttributesHandler().storeAttributes(artifact);
    }
    finally {
      itemUid.getLock().unlock();
    }

    return hash;
  }

  public static DefaultStorageFileItem newHashItem(ProxyRepository proxy, ResourceStoreRequest request,
                                                   StorageItem artifact, String hash)
  {
    StringContentLocator content = new StringContentLocator(hash);
    // XXX do we need to clone request here?
    DefaultStorageFileItem hashItem =
        new DefaultStorageFileItem(proxy, request, true /* canRead */, false/* canWrite */, content);
    hashItem.setModified(artifact.getModified());
    return hashItem;
  }
}
