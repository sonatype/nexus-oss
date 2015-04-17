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
package org.sonatype.nexus.repository.raw.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.TempStreamSupplier;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_PATH;

/**
 * A {@link RawContentFacet} that persists to a {@link StorageFacet}.
 *
 * @since 3.0
 */
public class RawContentFacetImpl
    extends FacetSupport
    implements RawContentFacet
{
  public static final String CONFIG_KEY = "rawContent";

  private final static List<HashAlgorithm> hashAlgorithms = Lists.newArrayList(MD5, SHA1);

  private final MimeSupport mimeSupport;

  private boolean strictContentTypeValidation = false;

  @Inject
  public RawContentFacetImpl(final MimeSupport mimeSupport)
  {
    this.mimeSupport = checkNotNull(mimeSupport);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    NestedAttributesMap attributes = configuration.attributes(CONFIG_KEY);
    this.strictContentTypeValidation = checkNotNull(attributes.require("strictContentTypeValidation", Boolean.class));
  }

  @Nullable
  @Override
  public RawContent get(final String path) {
    try (StorageTx tx = getStorage().openTx()) {
      final Component component = getComponent(tx, path, tx.getBucket());
      if (component == null) {
        return null;
      }

      final Asset asset = tx.firstAsset(component);
      final Blob blob = tx.requireBlob(asset.requireBlobRef());

      return marshall(asset, blob);
    }
  }

  @Override
  public void put(final String path, final RawContent content) throws IOException, InvalidContentException {
    try (StorageTx tx = getStorage().openTx()) {
      final Bucket bucket = tx.getBucket();
      Component component = getComponent(tx, path, bucket);
      Asset asset;
      if (component == null) {
        // CREATE
        component = tx.createComponent(bucket, getRepository().getFormat())
            .group(getGroup(path))
            .name(getName(path));

        // Set attributes map to contain "raw" format-specific metadata (in this case, path)
        component.formatAttributes().set(P_PATH, path);
        tx.saveComponent(component);

        asset = tx.createAsset(bucket, component);
      }
      else {
        // UPDATE
        asset = tx.firstAsset(component);
      }

      // TODO: Figure out created-by header
      final ImmutableMap<String, String> headers = ImmutableMap
          .of(BlobStore.BLOB_NAME_HEADER, path, BlobStore.CREATED_BY_HEADER, "unknown");

      try (TempStreamSupplier supplier = new TempStreamSupplier(content.openInputStream())) {
        try (InputStream is1 = supplier.get(); InputStream is2 = supplier.get()) {
          tx.setBlob(is1, headers, asset, hashAlgorithms, determineContentType(path, is2, content.getContentType()));
        }
      }

      tx.saveAsset(asset);
      getRepository().facet(SearchFacet.class).put(component);

      tx.commit();
    }
  }

  /**
   * Determines or confirms the content type for the content, or throws {@link InvalidContentException} if it cannot.
   */
  @Nonnull
  private String determineContentType(final String path, final InputStream is, final String declaredContentType)
      throws IOException {
    String contentType = declaredContentType;

    if (contentType == null) {
      log.trace("Content PUT to {} has no content type.", path);
      contentType = mimeSupport.detectMimeType(is, path);
      log.trace("Mime support implies content type {}", contentType);

      if (contentType == null && strictContentTypeValidation) {
        throw new InvalidContentException("Content type could not be determined.");
      }
    }
    else {
      final List<String> types = mimeSupport.detectMimeTypes(is, path);
      if (!types.isEmpty() && !types.contains(contentType)) {
        log.debug("Discovered content type {} ", types.get(0));
        if (strictContentTypeValidation) {
          throw new InvalidContentException(
              String.format("Declared content type %s, but declared %s.", contentType, types.get(0)));
        }
      }
    }
    return contentType;
  }

  private String getGroup(String path) {
    StringBuilder group = new StringBuilder();
    if (!path.startsWith("/")) {
      group.append("/");
    }
    int i = path.lastIndexOf("/");
    if (i != -1) {
      group.append(path.substring(0, i));
    }
    return group.toString();
  }

  private String getName(String path) {
    int i = path.lastIndexOf("/");
    if (i != -1) {
      return path.substring(i + 1);
    }
    else {
      return path;
    }
  }

  @Override
  public boolean delete(final String path) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final Component component = getComponent(tx, path, tx.getBucket());
      if (component == null) {
        return false;
      }
      tx.deleteComponent(component);
      getRepository().facet(SearchFacet.class).delete(component);
      tx.commit();
      return true;
    }
  }

  @Override
  public void updateLastUpdated(final String path, final DateTime lastUpdated) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      Component component = tx.findComponentWithProperty(P_PATH, path, tx.getBucket());

      if (component == null) {
        log.debug("Updating lastUpdated time for non-existent raw component {}", path);
        return;
      }

      // last updated will be automatically set on update
      tx.saveAsset(tx.firstAsset(component));
      tx.commit();
    }
  }

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

  // TODO: Consider a top-level indexed property (e.g. "locator") to make these common lookups fast
  private Component getComponent(StorageTx tx, String path, Bucket bucket) {
    String property = String.format("%s.%s.%s", P_ATTRIBUTES, RawFormat.NAME, P_PATH);
    return tx.findComponentWithProperty(property, path, bucket);
  }

  private RawContent marshall(final Asset asset, final Blob blob) {
    final String contentType = asset.requireContentType();
    final DateTime lastUpdated = asset.requireLastUpdated();

    return new RawContent()
    {
      @Override
      public String getContentType() {
        return contentType;
      }

      @Override
      public long getSize() {
        return blob.getMetrics().getContentSize();
      }

      @Override
      public InputStream openInputStream() {
        return blob.getInputStream();
      }

      @Override
      public DateTime getLastUpdated() {
        return lastUpdated;
      }
    };
  }
}
