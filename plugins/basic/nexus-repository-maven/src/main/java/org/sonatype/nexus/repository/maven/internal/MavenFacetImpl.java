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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.TempStreamSupplier;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.maven.internal.MavenPath.Coordinates;
import org.sonatype.nexus.repository.maven.internal.MavenPath.HashType;
import org.sonatype.nexus.repository.maven.internal.policy.ChecksumPolicy;
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

  private static final String P_COMPONENT_KEY = "key"; // component key: G:A:V for fast look-ups

  // shared properties for both artifact and metadata assets

  private static final String P_ASSET_KEY = "key"; // asset key: G:A:V[:C]:E for fast look-ups

  private static final String P_LAST_MODIFIED = "lastModified"; // date

  private static final String P_LAST_VERIFIED = "lastVerified"; // date

  private static final String P_ASSET_KIND = "kind"; // denoting asset kind

  private static final String ASSET_KIND_ARTIFACT = "artifact";

  private static final String ASSET_KIND_METADATA = "metadata";

  private static final String CONFIG_KEY = "maven";

  // members

  private final MimeSupport mimeSupport;

  private boolean strictContentTypeValidation;

  private VersionPolicy versionPolicy;

  private ChecksumPolicy checksumPolicy;

  @Inject
  public MavenFacetImpl(final MimeSupport mimeSupport) {
    this.mimeSupport = checkNotNull(mimeSupport);
  }

  @Override
  protected void doConfigure() throws Exception {
    super.doConfigure();
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    this.strictContentTypeValidation =
        attributes.get("strictContentTypeValidation", Boolean.class, Boolean.FALSE).booleanValue();
    this.versionPolicy = VersionPolicy.valueOf(
        attributes.require("versionPolicy", String.class)
    );
    this.checksumPolicy = ChecksumPolicy.valueOf(
        attributes.require("checksumPolicy", String.class)
    );
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
  public MavenPayload get(final MavenPath path) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final OrientVertex asset;
      if (path.getCoordinates() != null) {
        final OrientVertex component = findComponent(tx, path.getCoordinates(), tx.getBucket());
        if (component == null) {
          return null;
        }
        asset = selectComponentAsset(tx, component, path);
      }
      else {
        asset = findAsset(tx, path, tx.getBucket());
      }
      if (asset == null) {
        return null;
      }
      final BlobRef blobRef = getBlobRef(path, asset);
      final Blob blob = tx.getBlob(blobRef);
      checkState(blob != null, "asset of component with at path %s refers to missing blob %s", path.getPath(),
          blobRef);
      return marshall(tx, asset, blob);
    }
  }

  @Override
  public void put(final MavenPath path, final MavenPayload content)
      throws IOException, InvalidContentException
  {
    try (StorageTx tx = getStorage().openTx()) {
      try {
        putTx(tx, path, content);
        tx.commit();
      }
      catch (IOException | InvalidContentException e) {
        tx.rollback();
        throw e;
      }
    }
  }

  private void putTx(final StorageTx tx, final MavenPath path, final MavenPayload content)
      throws IOException, InvalidContentException
  {
    OrientVertex component = null;
    OrientVertex asset;
    if (path.getCoordinates() != null) {
      component = findComponent(tx, path.getCoordinates(), tx.getBucket());
      if (component == null) {
        component = tx.createComponent(tx.getBucket());

        // Set normalized properties: format, group, and name
        component.setProperty(StorageFacet.P_FORMAT, getRepository().getFormat().getValue());
        component.setProperty(StorageFacet.P_PATH, path.getPath());
        component.setProperty(StorageFacet.P_GROUP, path.getCoordinates().getGroupId());
        component.setProperty(StorageFacet.P_NAME, path.getCoordinates().getArtifactId());
        component.setProperty(StorageFacet.P_VERSION, path.getCoordinates().getVersion());

        final NestedAttributesMap componentAttributes = getFormatAttributes(tx, component);
        componentAttributes.set(P_COMPONENT_KEY, getComponentKey(path.getCoordinates()));
        componentAttributes.set(P_GROUP_ID, path.getCoordinates().getGroupId());
        componentAttributes.set(P_ARTIFACT_ID, path.getCoordinates().getArtifactId());
        componentAttributes.set(P_VERSION, path.getCoordinates().getVersion());
        if (path.getCoordinates().isSnapshot()) {
          componentAttributes.set(P_BASE_VERSION, path.getCoordinates().getBaseVersion());
        }
      }

      asset = selectComponentAsset(tx, component, path);
    }
    else {
      asset = findAsset(tx, path, tx.getBucket());
    }
    if (asset == null) {
      asset = tx.createAsset(tx.getBucket());
      if (component != null) {
        asset.addEdge(StorageFacet.E_PART_OF_COMPONENT, component);
      }
      asset.setProperty(StorageFacet.P_FORMAT, getRepository().getFormat().getValue());
      asset.setProperty(StorageFacet.P_NAME, path.getFileName());
      asset.setProperty(StorageFacet.P_PATH, path.getPath());

      final NestedAttributesMap assetAttributes = getFormatAttributes(tx, asset);
      assetAttributes.set(P_ASSET_KEY, getAssetKey(path));
      if (path.getCoordinates() != null) {
        assetAttributes.set(P_ASSET_KIND, ASSET_KIND_ARTIFACT);
        assetAttributes.set(P_GROUP_ID, path.getCoordinates().getGroupId());
        assetAttributes.set(P_ARTIFACT_ID, path.getCoordinates().getArtifactId());
        assetAttributes.set(P_VERSION, path.getCoordinates().getVersion());
        if (path.getCoordinates().isSnapshot()) {
          assetAttributes.set(P_BASE_VERSION, path.getCoordinates().getBaseVersion());
        }
        assetAttributes.set(P_CLASSIFIER, path.getCoordinates().getClassifier());
        assetAttributes.set(P_EXTENSION, path.getCoordinates().getExtension());
      }
      else {
        assetAttributes.set(P_ASSET_KIND, ASSET_KIND_METADATA);
        assetAttributes.set(StorageFacet.P_NAME, path.getFileName());
        assetAttributes.set(StorageFacet.P_PATH, path.getPath());
      }

      // TODO: if subordinate asset (sha1/md5), should we link it somehow to main asset?
    }

    storeAssetBlob(tx, asset, path, content);

    if (path.isHash()) {
      verifyAssetChecksum(tx, path, asset);
    }
    else {
      if (!content.getHashAlgorithms().isEmpty()) {
        for (HashType hashType : HashType.values()) {
          final HashCode hashCode = content.getHashCode(hashType.getHashAlgorithm());
          if (hashCode == null) {
            continue;
          }
          putTx(
              tx,
              path.hash(hashType),
              new MavenPayload(
                  new StringPayload(hashCode.toString(), "text/plain"),
                  content.getLastModified(),
                  content.getLastVerified(),
                  Collections.<HashAlgorithm, HashCode>emptyMap()
              )
          );
        }
      }
    }
  }

  @Override
  public boolean delete(final MavenPath path) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final OrientVertex component;
      final OrientVertex asset;
      if (path.getCoordinates() != null) {
        component = findComponent(tx, path.getCoordinates(), tx.getBucket());
        if (component == null) {
          return false;
        }
        asset = selectComponentAsset(tx, component, path);
      }
      else {
        component = null;
        asset = findAsset(tx, path, tx.getBucket());
      }
      if (asset == null) {
        return false;
      }
      final boolean lastAsset = component != null && getComponentAssets(component).size() == 1;
      tx.deleteBlob(getBlobRef(path, asset));
      tx.deleteVertex(asset);
      if (lastAsset) {
        tx.deleteVertex(component);
      }
      tx.commit();
      return true;
    }
  }

  @Override
  public boolean verifyMetadata(final MavenPath path, final DateTime dateTime) throws IOException {
    try (StorageTx tx = getStorage().openTx()) {
      final OrientVertex asset;
      if (path.getCoordinates() != null) {
        final OrientVertex component = findComponent(tx, path.getCoordinates(), tx.getBucket());
        if (component == null) {
          return false;
        }
        asset = selectComponentAsset(tx, component, path);
      }
      else {
        asset = findAsset(tx, path, tx.getBucket());
      }
      if (asset == null) {
        return false;
      }
      getFormatAttributes(tx, asset).set(P_LAST_VERIFIED, dateTime.toDate());
      tx.commit();
      return true;
    }
  }

  private void storeAssetBlob(final StorageTx tx,
                              final OrientVertex asset,
                              final MavenPath mavenPath,
                              final MavenPayload payload) throws IOException
  {
    // TODO: Figure out created-by header
    final ImmutableMap<String, String> headers = ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, mavenPath.getPath(),
        BlobStore.CREATED_BY_HEADER, "unknown"
    );

    try (InputStream inputStream = payload.openInputStream()) {
      try (TempStreamSupplier supplier = new TempStreamSupplier(inputStream)) {
        final String contentType = determineContentType(mavenPath, supplier, payload.getContentType());
        try (InputStream is = supplier.get()) {
          tx.setBlob(is, headers, asset, MavenPayload.HASH_ALGORITHMS, contentType);
        }
      }
    }
    getFormatAttributes(tx, asset).set(P_LAST_MODIFIED, payload.getLastModified().toDate());
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
  private OrientVertex findComponent(final StorageTx tx,
                                     final Coordinates coordinates,
                                     final OrientVertex bucket)
  {
    final String componentKeyName =
        StorageFacet.P_ATTRIBUTES + "." + getRepository().getFormat().getValue() + "." + P_COMPONENT_KEY;
    return tx.findComponentWithProperty(componentKeyName, getComponentKey(coordinates), bucket);
  }

  /**
   * Returns a list of all component assets.
   */
  private List<OrientVertex> getComponentAssets(final OrientVertex component)
  {
    final List<Vertex> vertices = Lists
        .newArrayList(component.getVertices(Direction.IN, StorageFacet.E_PART_OF_COMPONENT));
    final List<OrientVertex> result = Lists.newArrayList();
    for (Vertex v : vertices) {
      if (v instanceof OrientVertex) {
        result.add((OrientVertex) v);
      }
    }
    return result;
  }

  /**
   * Selects a component asset by key.
   */
  @Nullable
  private OrientVertex selectComponentAsset(final StorageTx tx,
                                            final OrientVertex component,
                                            final MavenPath artifactPath)
  {
    final String assetKey = getAssetKey(artifactPath);
    final List<OrientVertex> assets = getComponentAssets(component);
    for (OrientVertex v : assets) {
      final NestedAttributesMap attributesMap = getFormatAttributes(tx, v);
      if (assetKey.equals(attributesMap.get(P_ASSET_KEY, String.class))) {
        return v;
      }
    }
    return null;
  }

  /**
   * Finds asset by key.
   */
  @Nullable
  private OrientVertex findAsset(final StorageTx tx,
                                 final MavenPath mavenPath,
                                 final OrientVertex bucket)
  {
    final String assetKeyName =
        StorageFacet.P_ATTRIBUTES + "." + getRepository().getFormat().getValue() + "." + P_ASSET_KEY;
    return tx.findAssetWithProperty(assetKeyName, getAssetKey(mavenPath), bucket);
  }

  private NestedAttributesMap getAttributes(final StorageTx tx, final OrientVertex vertex) {
    return tx.getAttributes(vertex);
  }

  private NestedAttributesMap getFormatAttributes(final StorageTx tx, final OrientVertex vertex) {
    return getAttributes(tx, vertex).child(getRepository().getFormat().getValue());
  }

  /**
   * Assumes payload is a hash file, parses it like that. Takes existing -- same type -- hash from asset attributes and
   * compares the two, throws if not.
   */
  private void verifyAssetChecksum(final StorageTx tx, final MavenPath checksumMavenPath,
                                   final OrientVertex checksumAsset)
      throws IOException, InvalidContentException
  {
    checkState(checksumMavenPath.isHash()); // sanity check, if not, bad call is made here
    if (ChecksumPolicy.IGNORE == this.checksumPolicy) {
      return; // just ignore it
    }
    final OrientVertex mainAsset = findAsset(tx, checksumMavenPath.subordinateOf(), tx.getBucket());
    final String internalChecksum = tx.getAttributes(mainAsset).child(StorageFacet.P_CHECKSUM).require(
        checksumMavenPath.getHashType().getHashAlgorithm().name(), String.class);
    checkState(!Strings.isNullOrEmpty(internalChecksum)); // sanity check, this MUST exists

    final BlobRef blobRef = getBlobRef(checksumMavenPath, checksumAsset);
    final Blob blob = tx.getBlob(blobRef);
    final String externalChecksum = DigestExtractor.extract(blob);
    if (Strings.isNullOrEmpty(externalChecksum)) {
      // we could not interpret the stored payload
      final String message = "Unrecognized checksum for path: " + checksumMavenPath.getPath();
      if (ChecksumPolicy.WARN == this.checksumPolicy) {
        log.warn(message);
      }
      else {
        throw new InvalidContentException(message);
      }
    }
    else {
      if (!Objects.equals(internalChecksum, externalChecksum)) {
        final String message =
            "Invalid checksum for " + checksumMavenPath.getPath() + ": expected '" + internalChecksum + "' got '" +
                externalChecksum + "'";
        if (ChecksumPolicy.WARN == this.checksumPolicy) {
          log.warn(message);
        }
        else {
          throw new InvalidContentException(message);
        }
      }
    }
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
        throw new InvalidContentException(String.format("Content type could not be determined."));
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

  private BlobRef getBlobRef(final MavenPath mavenPath, final OrientVertex asset) {
    String blobRefStr = asset.getProperty(StorageFacet.P_BLOB_REF);
    checkState(blobRefStr != null, "asset of component at path %s has missing blob reference",
        mavenPath.getPath());
    return BlobRef.parse(blobRefStr);
  }

  /**
   * Creates a payload out of a Blob, for content originating from Blobs.
   */
  private MavenPayload marshall(final StorageTx tx, final OrientVertex asset, final Blob blob) {
    final String contentType = asset.getProperty(StorageFacet.P_CONTENT_TYPE);

    final NestedAttributesMap attributesMap = getFormatAttributes(tx, asset);
    final Date lastModified = attributesMap.require(P_LAST_MODIFIED, Date.class);
    checkNotNull(lastModified);
    final Date lastVerified = attributesMap.require(P_LAST_VERIFIED, Date.class);
    checkNotNull(lastVerified);

    final NestedAttributesMap checksums = getAttributes(tx, asset).child(StorageFacet.P_CHECKSUM);
    final Map<HashAlgorithm, HashCode> hashCodes = Maps.newHashMap();
    for (HashAlgorithm algorithm : MavenPayload.HASH_ALGORITHMS) {
      final HashCode hashCode = HashCode.fromString(checksums.require(algorithm.name(), String.class));
      hashCodes.put(algorithm, hashCode);
    }

    return new MavenPayload(
        new BlobPayload(blob, contentType),
        new DateTime(lastModified.getTime()),
        new DateTime(lastVerified.getTime()),
        hashCodes
    );
  }
}
