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

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

import org.codehaus.plexus.util.StringUtils;

/**
 * The Class DefaultStorageFileItem.
 */
public class DefaultStorageFileItem
    extends AbstractStorageItem
    implements StorageFileItem
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 3608889194663697395L;

  /**
   * The input stream.
   */
  private transient ContentLocator contentLocator;

  private long length;

  /**
   * This is here for backward compatibility only, to enable XStream to load up the old XML attributes.
   *
   * @deprecated The mime-type is now coming from ContentLocator, see getMimeType() method body.
   */
  private String mimeType;

  @Override
  public void upgrade() {
    super.upgrade();

    getRepositoryItemAttributes().setLength(length);
  }

  /**
   * Instantiates a new default storage file item.
   *
   * @param repository     the repository
   * @param path           the path
   * @param canRead        the can read
   * @param canWrite       the can write
   * @param contentLocator the content locator
   */
  public DefaultStorageFileItem(Repository repository, ResourceStoreRequest request, boolean canRead,
                                boolean canWrite, ContentLocator contentLocator)
  {
    super(repository, request, canRead, canWrite);
    this.contentLocator = contentLocator;
  }

  /**
   * Shortcut method.
   *
   * @deprecated supply resourceStoreRequest always
   */
  public DefaultStorageFileItem(Repository repository, String path, boolean canRead, boolean canWrite,
                                ContentLocator contentLocator)
  {
    this(repository, new ResourceStoreRequest(path, true, false), canRead, canWrite, contentLocator);
  }

  /**
   * Instantiates a new default storage file item.
   *
   * @param RepositoryRouter router
   * @param path             the path
   * @param canRead          the can read
   * @param canWrite         the can write
   * @param contentLocator   the content locator
   */
  public DefaultStorageFileItem(RepositoryRouter router, ResourceStoreRequest request, boolean canRead,
                                boolean canWrite, ContentLocator contentLocator)
  {
    super(router, request, canRead, canWrite);
    this.contentLocator = contentLocator;
  }

  @Deprecated
  public DefaultStorageFileItem(RepositoryRouter router, String path, boolean canRead, boolean canWrite,
                                ContentLocator contentLocator)
  {
    this(router, new ResourceStoreRequest(path, true, false), canRead, canWrite, contentLocator);
  }

  @Override
  public long getLength() {
    return getRepositoryItemAttributes().getLength();
  }

  @Override
  public void setLength(long length) {
    getRepositoryItemAttributes().setLength(length);
  }

  @Override
  public String getMimeType() {
    return getContentLocator().getMimeType();
  }

  @Override
  public boolean isReusableStream() {
    return getContentLocator().isReusable();
  }

  @Override
  public InputStream getInputStream()
      throws IOException
  {
    return getContentLocator().getContent();
  }

  @Override
  public void setContentLocator(ContentLocator locator) {
    this.contentLocator = locator;
  }

  @Override
  public ContentLocator getContentLocator() {
    return this.contentLocator;
  }

  @Override
  protected boolean isOverlayable(StorageItem item) {
    // we have an exception here, so, Files are overlayable with any other Files
    return super.isOverlayable(item) || StorageFileItem.class.isAssignableFrom(item.getClass());
  }

  @Override
  public String getContentGeneratorId() {
    if (isContentGenerated()) {
      return getRepositoryItemAttributes().get(ContentGenerator.CONTENT_GENERATOR_ID);
    }
    else {
      return null;
    }
  }

  @Override
  public void setContentGeneratorId(String contentGeneratorId) {
    if (StringUtils.isBlank(contentGeneratorId)) {
      // rempve it from attributes
      getRepositoryItemAttributes().remove(ContentGenerator.CONTENT_GENERATOR_ID);
    }
    else {
      // add it to attributes
      getRepositoryItemAttributes().put(ContentGenerator.CONTENT_GENERATOR_ID, contentGeneratorId);
    }
  }

  @Override
  public boolean isContentGenerated() {
    return getRepositoryItemAttributes().containsKey(ContentGenerator.CONTENT_GENERATOR_ID);
  }

  // ==

  public String toString() {
    if (isContentGenerated()) {
      return String.format("%s (file, contentGenerator=%s)", super.toString(), getContentGeneratorId());
    }
    else {
      return String.format("%s (file)", super.toString());
    }
  }
}
