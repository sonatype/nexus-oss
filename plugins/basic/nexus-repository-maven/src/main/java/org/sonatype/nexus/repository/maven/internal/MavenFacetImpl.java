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
package org.sonatype.nexus.repository.maven.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.TempStreamSupplier;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.maven.internal.MavenPath.Coordinates;
import org.sonatype.nexus.repository.maven.internal.MavenPath.HashType;
import org.sonatype.nexus.repository.maven.internal.policy.ChecksumPolicy;
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link MavenFacet} that persists Maven artifacts and metadata to a {@link StorageFacet}.
 * <p/>
 * Structure for artifacts (CMA components and assets):
 * <ul>
 * <li>CMA components: keyed by groupId:artifactId:version</li>
 * <li>CMA assets: keyed by path</li>
 * </ul>
 * <p/>
 * Structure for metadata (CMA assets only):
 * <ul>
 * <li>CMA assets: keyed by path</li>
 * </ul>
 * In both cases, "external" hashes are stored as separate asset, as their path differs too.
 *
 * @since 3.0
 */
@Named
public class MavenFacetImpl
    extends FacetSupport
    implements MavenFacet
{
  // artifact shared properties of both, artifact component and artifact asset

  private static final String P_GROUP_ID = "groupId";

  private static final String P_ARTIFACT_ID = "artifactId";

  private static final String P_VERSION = "version";

  private static final String P_BASE_VERSION = "baseVersion";

  private static final String P_CLASSIFIER = "classifier";

  private static final String P_EXTENSION = "extension";

  // artifact component properties

  private static final String P_COMPONENT_KEY = "key";

  // shared properties for both artifact and metadata assets

  private static final String P_ASSET_KEY = "key";

  private static final String P_CONTENT_LAST_MODIFIED = "contentLastModified";

  private static final String P_CONTENT_ETAG = "etag";

  private static final String P_LAST_VERIFIED = "lastVerified";

  private static final String CONFIG_KEY = "maven";

  // members

  private final MimeSupport mimeSupport;

  private final Map<String, MavenPathParser> mavenPathParsers;

  private boolean strictContentTypeValidation;

  private MavenPathParser mavenPathParser;

  private VersionPolicy versionPolicy;

  private ChecksumPolicy checksumPolicy;

  @Inject
  public MavenFacetImpl(final MimeSupport mimeSupport, final Map<String, MavenPathParser> mavenPathParsers) {
    this.mimeSupport = checkNotNull(mimeSupport);
    this.mavenPathParsers = checkNotNull(mavenPathParsers);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    super.doConfigure(configuration);

    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    this.strictContentTypeValidation =
        attributes.get("strictContentTypeValidation", Boolean.class, Boolean.FALSE).booleanValue();
    this.mavenPathParser = checkNotNull(mavenPathParsers.get(getRepository().getFormat().getValue()));
    this.versionPolicy = VersionPolicy.valueOf(
        attributes.require("versionPolicy", String.class)
    );
    this.checksumPolicy = ChecksumPolicy.valueOf(
        attributes.require("checksumPolicy", String.class)
    );
  }

  @Nonnull
  @Override
  public MavenPathParser getMavenPathParser() {
    return mavenPathParser;
  }

  @Nonnull
  @Override
  public VersionPolicy getVersionPolicy() {
    return versionPolicy;
  }

  @Nonnull
  @Override
  public ChecksumPolicy getChecksumPolicy() {
    return checksumPolicy;
  }

  @Nullable
  @Override
  public Content get(final MavenPath path) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final Asset asset = findAsset(tx, tx.getBucket(), path);
      if (asset == null) {
        return null;
      }
      final Blob blob = tx.requireBlob(asset.requireBlobRef());
      final String contentType = asset.contentType();

      final NestedAttributesMap checksumAttributes = asset.attributes().child(StorageFacet.P_CHECKSUM);
      final Map<HashAlgorithm, HashCode> hashCodes = Maps.newHashMap();
      for (HashAlgorithm algorithm : HashType.ALGORITHMS) {
        final HashCode hashCode = HashCode.fromString(checksumAttributes.require(algorithm.name(), String.class));
        hashCodes.put(algorithm, hashCode);
      }
      final NestedAttributesMap attributesMap = asset.formatAttributes();
      final Date lastModifiedDate = attributesMap.get(P_CONTENT_LAST_MODIFIED, Date.class);
      final String eTag = attributesMap.get(P_CONTENT_ETAG, String.class);
      final Content result = new Content(new BlobPayload(blob, contentType));
      result.getAttributes()
          .set(Content.CONTENT_LAST_MODIFIED, lastModifiedDate == null ? null : new DateTime(lastModifiedDate));
      result.getAttributes().set(Content.CONTENT_ETAG, eTag);
      result.getAttributes().set(Content.CONTENT_HASH_CODES_MAP, hashCodes);
      return result;
    }
  }

  @Override
  public void put(final MavenPath path, final Payload payload)
      throws IOException, InvalidContentException
  {
    try (StorageTx tx = getStorage().openTx()) {
      if (path.getCoordinates() != null) {
        putArtifact(path, payload, tx);
      }
      else {
        putFile(path, payload, tx);
      }
      tx.commit();
    }
  }

  private void putArtifact(final MavenPath path, final Payload payload, final StorageTx tx)
      throws IOException, InvalidContentException
  {
    final Coordinates coordinates = checkNotNull(path.getCoordinates());
    Component component = findComponent(tx, tx.getBucket(), path);
    if (component == null) {
      // Create and set top-level properties
      component = tx.createComponent(tx.getBucket(), getRepository().getFormat())
          .group(coordinates.getGroupId())
          .name(coordinates.getArtifactId())
          .version(coordinates.getVersion());
      component.formatAttributes().set(StorageFacet.P_PATH, path.getPath());

      // Set format specific attributes
      final NestedAttributesMap componentAttributes = component.formatAttributes();
      componentAttributes.set(P_COMPONENT_KEY, getComponentKey(coordinates));
      componentAttributes.set(P_GROUP_ID, coordinates.getGroupId());
      componentAttributes.set(P_ARTIFACT_ID, coordinates.getArtifactId());
      componentAttributes.set(P_VERSION, coordinates.getVersion());
      if (coordinates.isSnapshot()) {
        componentAttributes.set(P_BASE_VERSION, coordinates.getBaseVersion());
      }
      tx.saveComponent(component);
    }

    Asset asset = selectComponentAsset(tx, component, path);
    if (asset == null) {
      asset = tx.createAsset(tx.getBucket(), component);

      asset.name(path.getFileName());
      asset.formatAttributes().set(StorageFacet.P_PATH, path.getPath());

      final NestedAttributesMap assetAttributes = asset.formatAttributes();
      assetAttributes.set(P_ASSET_KEY, getAssetKey(path));
      assetAttributes.set(P_GROUP_ID, coordinates.getGroupId());
      assetAttributes.set(P_ARTIFACT_ID, coordinates.getArtifactId());
      assetAttributes.set(P_VERSION, coordinates.getVersion());
      if (coordinates.isSnapshot()) {
        assetAttributes.set(P_BASE_VERSION, coordinates.getBaseVersion());
      }
      assetAttributes.set(P_CLASSIFIER, coordinates.getClassifier());
      assetAttributes.set(P_EXTENSION, coordinates.getExtension());

      // TODO: if subordinate asset (sha1/md5/asc), should we link it somehow to main asset?
    }

    putAssetPayload(path, tx, asset, payload);
    tx.saveAsset(asset);
    getRepository().facet(SearchFacet.class).put(component);
  }

  private void putFile(final MavenPath path, final Payload payload, final StorageTx tx)
      throws IOException, InvalidContentException
  {
    Asset asset = findAsset(tx, tx.getBucket(), path);
    if (asset == null) {
      asset = tx.createAsset(tx.getBucket(), getRepository().getFormat());
      asset.name(path.getFileName());
      asset.formatAttributes().set(StorageFacet.P_PATH, path.getPath());

      final NestedAttributesMap assetAttributes = asset.formatAttributes();
      assetAttributes.set(P_ASSET_KEY, getAssetKey(path));

      // TODO: if subordinate asset (sha1/md5/asc), should we link it somehow to main asset?
    }

    putAssetPayload(path, tx, asset, payload);
    tx.saveAsset(asset);
  }

  private void putAssetPayload(final MavenPath path,
                               final StorageTx tx,
                               final Asset asset,
                               final Payload payload) throws IOException
  {
    // TODO: Figure out created-by header
    final ImmutableMap<String, String> headers = ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, path.getPath(),
        BlobStore.CREATED_BY_HEADER, "unknown"
    );

    try (InputStream inputStream = payload.openInputStream()) {
      try (TempStreamSupplier supplier = new TempStreamSupplier(inputStream)) {
        final String contentType = determineContentType(path, supplier, payload.getContentType());
        try (InputStream is = supplier.get()) {
          tx.setBlob(is, headers, asset, HashType.ALGORITHMS, contentType);
        }
      }
    }

    final NestedAttributesMap formatAttributes = asset.formatAttributes();
    if (payload instanceof Content) {
      Content content = (Content) payload;
      final DateTime lastModified = content.getAttributes().get(Content.CONTENT_LAST_MODIFIED, DateTime.class);
      if (lastModified != null) {
        formatAttributes.set(P_CONTENT_LAST_MODIFIED, lastModified.toDate());
      }
      formatAttributes.set(P_CONTENT_ETAG, content.getAttributes().get(Content.CONTENT_ETAG, String.class));
    }
    else {
      formatAttributes.set(P_CONTENT_LAST_MODIFIED, DateTime.now().toDate());
    }
  }

  @Override
  public boolean delete(final MavenPath... paths) throws IOException {
    boolean result = false;
    try (StorageTx tx = getStorage().openTx()) {
      for (MavenPath path : paths) {
        if (path.getCoordinates() != null) {
          result = deleteArtifact(path, tx) || result;
        }
        else {
          result = deleteFile(path, tx) || result;
        }
      }
    }
    return result;
  }

  private boolean deleteArtifact(final MavenPath path, final StorageTx tx) throws IOException {
    final Component component = findComponent(tx, tx.getBucket(), path);
    if (component == null) {
      return false;
    }
    final Asset asset = selectComponentAsset(tx, component, path);
    if (asset == null) {
      return false;
    }
    tx.deleteAsset(asset);
    if (!tx.browseAssets(component).iterator().hasNext()) {
      tx.deleteComponent(component);
      getRepository().facet(SearchFacet.class).delete(component);
    }
    else {
      getRepository().facet(SearchFacet.class).put(component);
    }
    tx.commit();
    return true;
  }

  private boolean deleteFile(final MavenPath path, final StorageTx tx) throws IOException {
    final Asset asset = findAsset(tx, tx.getBucket(), path);
    if (asset == null) {
      return false;
    }
    tx.deleteAsset(asset);
    tx.commit();
    return true;
  }

  @Override
  public DateTime getLastVerified(final MavenPath path) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final Asset asset = findAsset(tx, tx.getBucket(), path);
      if (asset == null) {
        return null;
      }
      final NestedAttributesMap attributes = asset.formatAttributes();
      final Date date = attributes.get(P_LAST_VERIFIED, Date.class);
      if (date == null) {
        return null;
      }
      return new DateTime(date);
    }
  }

  @Override
  public boolean setLastVerified(final MavenPath path, final DateTime verified) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final Asset asset = findAsset(tx, tx.getBucket(), path);
      if (asset == null) {
        return false;
      }
      final NestedAttributesMap attributes = asset.formatAttributes();
      attributes.set(P_LAST_VERIFIED, verified.toDate());
      tx.saveAsset(asset);
      tx.commit();
      return true;
    }
  }

  /**
   * Returns component key based on passed in {@link Coordinates} G:A:V values.
   */
  private String getComponentKey(final Coordinates coordinates) {
    // TODO: maybe sha1() the resulting string?
    return coordinates.getGroupId()
        + ":" + coordinates.getArtifactId()
        + ":" + coordinates.getVersion();
  }

  /**
   * Returns asset key based on passed in {@link MavenPath} path value.
   */
  private String getAssetKey(final MavenPath mavenPath) {
    // TODO: maybe sha1() the resulting string?
    return mavenPath.getPath();
  }

  /**
   * Finds component by key.
   */
  @Nullable
  private Component findComponent(final StorageTx tx,
                                  final Bucket bucket,
                                  final MavenPath mavenPath)
  {
    final String componentKeyName =
        StorageFacet.P_ATTRIBUTES + "." + getRepository().getFormat().getValue() + "." + P_COMPONENT_KEY;
    return tx.findComponentWithProperty(componentKeyName, getComponentKey(mavenPath.getCoordinates()), bucket);
  }

  /**
   * Selects a component asset by key.
   */
  @Nullable
  private Asset selectComponentAsset(final StorageTx tx,
                                     final Component component,
                                     final MavenPath mavenPath)
  {
    final String assetKey = getAssetKey(mavenPath);
    for (Asset asset : tx.browseAssets(component)) {
      final NestedAttributesMap attributesMap = asset.formatAttributes();
      if (assetKey.equals(attributesMap.get(P_ASSET_KEY, String.class))) {
        return asset;
      }
    }
    return null;
  }

  /**
   * Finds asset by key.
   */
  @Nullable
  private Asset findAsset(final StorageTx tx,
                          final Bucket bucket,
                          final MavenPath mavenPath)
  {
    final String assetKeyName =
        StorageFacet.P_ATTRIBUTES + "." + getRepository().getFormat().getValue() + "." + P_ASSET_KEY;
    return tx.findAssetWithProperty(assetKeyName, getAssetKey(mavenPath), bucket);
  }

  /**
   * Determines or confirms the content type for the content, or throws {@link InvalidContentException} if it cannot.
   */
  @Nonnull
  private String determineContentType(final MavenPath mavenPath,
                                      final Supplier<InputStream> inputStreamSupplier,
                                      final String declaredContentType)
      throws IOException
  {
    String contentType = declaredContentType;

    if (contentType == null) {
      log.trace("Content PUT to {} has no content type.", mavenPath);
      try (InputStream is = inputStreamSupplier.get()) {
        contentType = mimeSupport.detectMimeType(is, mavenPath.getPath());
      }
      log.trace("Mime support implies content type {}", contentType);

      if (contentType == null && strictContentTypeValidation) {
        throw new InvalidContentException("Content type could not be determined.");
      }
    }
    else {
      try (InputStream is = inputStreamSupplier.get()) {
        final List<String> types = mimeSupport.detectMimeTypes(is, mavenPath.getPath());
        if (!types.isEmpty() && !types.contains(contentType)) {
          log.debug("Discovered content type {} ", types.get(0));
          if (strictContentTypeValidation) {
            throw new InvalidContentException(
                String.format("Declared content type %s, but declared %s.", contentType, types.get(0)));
          }
        }
      }
    }
    return contentType;
  }

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

}
