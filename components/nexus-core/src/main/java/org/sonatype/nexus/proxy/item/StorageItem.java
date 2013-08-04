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

import java.util.Map;

import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.Attributes;

/**
 * The Interface StorageItem, a top of the item abstraction.
 */
public interface StorageItem
{
  /**
   * The request.
   */
  ResourceStoreRequest getResourceStoreRequest();

  /**
   * Gets the repository item uid from where originates item.
   *
   * @return the repository item uid
   */
  RepositoryItemUid getRepositoryItemUid();

  /**
   * Set the repository item uid.
   */
  void setRepositoryItemUid(RepositoryItemUid repositoryItemUid);

  /**
   * Gets the repository id from where originates item.
   *
   * @return the repository id
   */
  String getRepositoryId();

  /**
   * Gets the creation time.
   *
   * @return the created
   */
  long getCreated();

  /**
   * Gets the modification time.
   *
   * @return the modified
   */
  long getModified();

  /**
   * Gets the stored locally time.
   *
   * @return the stored locally
   */
  long getStoredLocally();

  /**
   * Sets the stored locally time.
   *
   * @return the stored locally
   */
  void setStoredLocally(long ts);

  /**
   * Gets the remote checked.
   *
   * @return the remote checked
   */
  long getRemoteChecked();

  /**
   * Sets the remote checked.
   *
   * @return the remote checked
   */
  void setRemoteChecked(long ts);

  /**
   * Gets the last requested.
   *
   * @return time when it was last served
   */
  long getLastRequested();

  /**
   * Sets the last requested.
   *
   * @return time when it was last served
   */
  void setLastRequested(long ts);

  /**
   * Checks if is virtual.
   *
   * @return true, if is virtual
   */
  boolean isVirtual();

  /**
   * Checks if is readable.
   *
   * @return true, if is readable
   */
  boolean isReadable();

  /**
   * Checks if is writable.
   *
   * @return true, if is writable
   */
  boolean isWritable();

  /**
   * Returns true if the item is expired.
   */
  boolean isExpired();

  /**
   * Sets if the item is expired.
   */
  void setExpired(boolean expired);

  /**
   * Gets the path.
   *
   * @return the path
   */
  String getPath();

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  /**
   * Gets the parent path.
   *
   * @return the parent path
   */
  String getParentPath();

  /**
   * Gets the remote url.
   *
   * @return the remote url
   */
  String getRemoteUrl();

  /**
   * Gets the user attributes. These are saved and persisted.
   *
   * @return the attributes
   * @deprecated Use {@link #getRepositoryItemAttributes()} instead! While this method still returns a mutable map
   *             (a map "view" of {@link Attributes} returned by {@link #getRepositoryItemAttributes()}), the Map
   *             mutation
   *             over iterators (key, value or entry-set) is not implemented and will yield in runtime exception!
   */
  @Deprecated
  Map<String, String> getAttributes();

  /**
   * Returns the item attributes. They are persisted and share lifecycle together with item.
   *
   * @return the item attributes
   */
  Attributes getRepositoryItemAttributes();

  /**
   * Gets the item context. It is living only during item processing, it is not stored.
   *
   * @return the attributes
   */
  RequestContext getItemContext();

  /**
   * Overlay.
   *
   * @param item the item
   * @deprecated This method is for internal use only. You really don't want to use this method, but to
   *             modify Attributes with {@link Attributes#get(String)} and {@link Attributes#put(String, String)}
   *             instead. See {@link Attributes}.
   */
  @Deprecated
  void overlay(StorageItem item);

  /**
   * Returns the generation of the attributes. For Nexus internal use only!
   */
  int getGeneration();

  /**
   * Increments the generation of the attributes. For Nexus internal use only!
   */
  void incrementGeneration();
}
