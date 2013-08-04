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

package org.sonatype.nexus.proxy.item;

import java.io.File;
import java.util.Map;

import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.internal.AttributesMapAdapter;
import org.sonatype.nexus.proxy.attributes.internal.DefaultAttributes;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.util.ItemPathUtils;

import com.google.common.base.Strings;

/**
 * The Class AbstractStorageItem.
 *
 * @author cstamas
 */
public abstract class AbstractStorageItem
    implements StorageItem
{

  /**
   * The request
   */
  private transient ResourceStoreRequest request;

  /**
   * The repository item uid.
   */
  private transient RepositoryItemUid repositoryItemUid;

  /**
   * The store.
   */
  private transient ResourceStore store;

  /**
   * The item context
   */
  private transient RequestContext context;

  /**
   * the attributes
   */
  private transient Attributes itemAttributes;

  /**
   * Used for versioning of attribute
   */
  private int generation = 0;

  /**
   * The path.
   */
  private String path;

  /**
   * The readable.
   */
  private boolean readable;

  /**
   * The writable.
   */
  private boolean writable;

  /**
   * The repository id.
   */
  private String repositoryId;

  /**
   * The created.
   */
  private long created;

  /**
   * The modified.
   */
  private long modified;

  /**
   * The stored locally.
   */
  private long storedLocally;

  /**
   * The last remoteCheck timestamp.
   */
  // TODO: leave the field name as-is coz of persistence and old nexuses!
  private long lastTouched;

  /**
   * The last requested timestamp.
   */
  private long lastRequested;

  /**
   * Expired flag
   */
  private boolean expired;

  /**
   * The remote url.
   */
  private String remoteUrl;

  /**
   * The persisted attributes.
   */
  private Map<String, String> attributes;

  // ==

  public Attributes getRepositoryItemAttributes() {
    return itemAttributes;
  }

  /**
   * This method should be called ONLY when you load up a _legacy_ attribute using _legacy_ attribute store!
   */
  public void upgrade() {
    this.context = new RequestContext();
    this.itemAttributes = new DefaultAttributes();

    // this here is for ITs only, some of them use "manually crafter" attributes XML files and would NPE
    // In "real life", all the files stored in Nexus have at least sha1/md5 set as attributes, meaning,
    // all the real life items has at least two attributes and this map would never be null!
    if (attributes != null) {
      getRepositoryItemAttributes().putAll(attributes);
    }

    getRepositoryItemAttributes().setGeneration(generation);
    getRepositoryItemAttributes().setPath(path);
    getRepositoryItemAttributes().setReadable(readable);
    getRepositoryItemAttributes().setWritable(writable);
    getRepositoryItemAttributes().setRepositoryId(repositoryId);
    getRepositoryItemAttributes().setCreated(created);
    getRepositoryItemAttributes().setModified(modified);
    getRepositoryItemAttributes().setStoredLocally(storedLocally);
    getRepositoryItemAttributes().setCheckedRemotely(lastTouched);
    getRepositoryItemAttributes().setLastRequested(lastRequested);
    getRepositoryItemAttributes().setExpired(expired);
    if (!Strings.isNullOrEmpty(remoteUrl)) {
      getRepositoryItemAttributes().setRemoteUrl(remoteUrl);
    }
  }

  // ==

  /**
   * Default constructor.
   */
  private AbstractStorageItem() {
    this.context = new RequestContext();
    this.itemAttributes = new DefaultAttributes();
  }

  /**
   * Instantiates a new abstract storage item.
   */
  public AbstractStorageItem(final ResourceStoreRequest request, final boolean readable, final boolean writable) {
    this();
    this.request = request.cloneAndDetach();
    this.context.setParentContext(request.getRequestContext());
    setPath(request.getRequestPath());
    setReadable(readable);
    setWritable(writable);
    setCreated(System.currentTimeMillis());
    setModified(getCreated());
  }

  /**
   * Instantiates a new abstract storage item.
   */
  public AbstractStorageItem(Repository repository, ResourceStoreRequest request, boolean readable,
                             boolean writable)
  {
    this(request, readable, writable);
    this.store = repository;
    this.repositoryItemUid = repository.createUid(getPath());
    setRepositoryId(repository.getId());
  }

  /**
   * Instantiates a new abstract storage item.
   */
  public AbstractStorageItem(RepositoryRouter router, ResourceStoreRequest request, boolean readable,
                             boolean writable)
  {
    this(request, readable, writable);
    this.store = router;
  }

  /**
   * Gets the store.
   *
   * @return the store
   */
  public ResourceStore getStore() {
    return this.store;
  }

  /**
   * Sets the store.
   */
  public void setStore(ResourceStore store) {
    // only allow this when we are virtual!
    if (isVirtual()) {
      this.store = store;
    }
  }

  public ResourceStoreRequest getResourceStoreRequest() {
    return request;
  }

  public void setResourceStoreRequest(ResourceStoreRequest request) {
    this.request = request;

    this.context = new RequestContext(request.getRequestContext());
  }

  public RepositoryItemUid getRepositoryItemUid() {
    return repositoryItemUid;
  }

  /**
   * Sets the UID.
   */
  public void setRepositoryItemUid(RepositoryItemUid repositoryItemUid) {
    this.repositoryItemUid = repositoryItemUid;
    this.store = repositoryItemUid.getRepository();

    getRepositoryItemAttributes().setRepositoryId(repositoryItemUid.getRepository().getId());
    getRepositoryItemAttributes().setPath(repositoryItemUid.getPath());
  }

  public String getRepositoryId() {
    return getRepositoryItemAttributes().getRepositoryId();
  }

  /**
   * Sets the repository id.
   *
   * @param repositoryId the new repository id
   */
  public void setRepositoryId(String repositoryId) {
    getRepositoryItemAttributes().setRepositoryId(repositoryId);
  }

  public long getCreated() {
    return getRepositoryItemAttributes().getCreated();
  }

  /**
   * Sets the created.
   *
   * @param created the new created
   */
  public void setCreated(long created) {
    getRepositoryItemAttributes().setCreated(created);
  }

  public long getModified() {
    return getRepositoryItemAttributes().getModified();
  }

  /**
   * Sets the modified.
   *
   * @param modified the new modified
   */
  public void setModified(long modified) {
    getRepositoryItemAttributes().setModified(modified);
  }

  public boolean isReadable() {
    return getRepositoryItemAttributes().isReadable();
  }

  /**
   * Sets the readable.
   *
   * @param readable the new readable
   */
  public void setReadable(boolean readable) {
    getRepositoryItemAttributes().setReadable(readable);
  }

  public boolean isWritable() {
    return getRepositoryItemAttributes().isWritable();
  }

  /**
   * Sets the writable.
   *
   * @param writable the new writable
   */
  public void setWritable(boolean writable) {
    getRepositoryItemAttributes().setWritable(writable);
  }

  public String getPath() {
    return getRepositoryItemAttributes().getPath();
  }

  /**
   * Sets the path.
   *
   * @param path the new path
   */
  public void setPath(String path) {
    getRepositoryItemAttributes().setPath(ItemPathUtils.cleanUpTrailingSlash(path));
  }

  public boolean isExpired() {
    return getRepositoryItemAttributes().isExpired();
  }

  /**
   * Sets the expired flag.
   */
  public void setExpired(boolean expired) {
    getRepositoryItemAttributes().setExpired(expired);
  }

  public String getName() {
    return new File(getPath()).getName();
  }

  public String getParentPath() {
    return ItemPathUtils.getParentPath(getPath());
  }

  /**
   * {@inheritDoc}
   */
  @Deprecated
  public Map<String, String> getAttributes() {
    return new AttributesMapAdapter(itemAttributes);
  }

  public RequestContext getItemContext() {
    return context;
  }

  public boolean isVirtual() {
    return getRepositoryItemUid() == null;
  }

  public String getRemoteUrl() {
    return getRepositoryItemAttributes().getRemoteUrl();
  }

  /**
   * Sets the remote url.
   *
   * @param remoteUrl the new remote url
   */
  public void setRemoteUrl(String remoteUrl) {
    getRepositoryItemAttributes().setRemoteUrl(remoteUrl);
  }

  public long getStoredLocally() {
    return getRepositoryItemAttributes().getStoredLocally();
  }

  /**
   * Sets the stored locally.
   *
   * @param storedLocally the new stored locally
   */
  public void setStoredLocally(long storedLocally) {
    getRepositoryItemAttributes().setStoredLocally(storedLocally);
  }

  public long getRemoteChecked() {
    return getRepositoryItemAttributes().getCheckedRemotely();
  }

  /**
   * Sets the remote checked.
   *
   * @param lastTouched the new remote checked
   */
  public void setRemoteChecked(long lastTouched) {
    getRepositoryItemAttributes().setCheckedRemotely(lastTouched);
  }

  public long getLastRequested() {
    return getRepositoryItemAttributes().getLastRequested();
  }

  /**
   * Sets the last requested timestamp.
   */
  public void setLastRequested(long lastRequested) {
    getRepositoryItemAttributes().setLastRequested(lastRequested);
  }

  public int getGeneration() {
    return getRepositoryItemAttributes().getGeneration();
  }

  public void incrementGeneration() {
    getRepositoryItemAttributes().incrementGeneration();
  }

  @Deprecated
  public void overlay(StorageItem item)
      throws IllegalArgumentException
  {
    if (item == null) {
      throw new NullPointerException("Cannot overlay null item onto this item of class "
          + this.getClass().getName());
    }
    // here was the "overlay" implemented, which was moved to DefaultAttributes#overlayAttributes method
    // instead with much cleaner implementation. Here, it was unlear and code looked "arbitrary" (why
    // some fields "win" over others).
  }

  protected boolean isOverlayable(StorageItem item) {
    return this.getClass().isAssignableFrom(item.getClass());
  }

  // ==

  public String toString() {
    if (isVirtual()) {
      return getPath();
    }
    else {
      return getRepositoryItemUid().toString();
    }
  }

}
