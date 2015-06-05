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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.TempStreamSupplier;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPath.Coordinates;
import org.sonatype.nexus.repository.maven.MavenPath.HashType;
import org.sonatype.nexus.repository.maven.MavenPathParser;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageFacet.Operation;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
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
  /**
   * How many times should method {@link #toContent(StorageTx, MavenPath)} attempt to create {@link Content}. Retries
   * might happen due to changed backing BLOBs for example, in case of an asset update.
   */
  private static final int MAX_ASSET_TO_CONTENT_ATTEMPTS = 3;

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

  private final Map<String, MavenPathParser> mavenPathParsers;

  @VisibleForTesting
  static final String CONFIG_KEY = "maven";

  @VisibleForTesting
  static class Config
  {
    @NotNull(groups = {HostedType.ValidationGroup.class, ProxyType.ValidationGroup.class})
    public VersionPolicy versionPolicy;

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "versionPolicy=" + versionPolicy +
          '}';
    }
  }

  private Config config;

  private MavenPathParser mavenPathParser;

  private StorageFacet storageFacet;

  @Inject
  public MavenFacetImpl(final Map<String, MavenPathParser> mavenPathParsers) {
    this.mavenPathParsers = checkNotNull(mavenPathParsers);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, Config.class,
        Default.class, getRepository().getType().getValidationGroup()
    );
  }

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);
    mavenPathParser = checkNotNull(mavenPathParsers.get(getRepository().getFormat().getValue()));
    storageFacet = getRepository().facet(StorageFacet.class);
    storageFacet.registerWritePolicySelector(new MavenWritePolicySelector(mavenPathParser));
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    log.debug("Config: {}", config);
  }

  @Override
  protected void doDestroy() throws Exception {
    config = null;
  }

  @Nonnull
  @Override
  public MavenPathParser getMavenPathParser() {
    return mavenPathParser;
  }

  @Nonnull
  @Override
  public VersionPolicy getVersionPolicy() {
    return config.versionPolicy;
  }

  @Nullable
  @Override
  public Content get(final MavenPath path) throws IOException {
    try (StorageTx tx = storageFacet.openTx()) {
      return get(tx, path);
    }
  }

  @Nullable
  @Override
  public Content get(final StorageTx tx, final MavenPath path) throws IOException {
    log.debug("GET {} : {}", getRepository().getName(), path.getPath());
    return toContent(tx, path);
  }

  /**
   * Creates {@link Content} from passed in {@link Asset}.
   */
  private Content toContent(final StorageTx tx, final MavenPath mavenPath) throws IOException {
    IllegalStateException lastIllegalStateException = null;
    for (int attempt = 0; attempt < MAX_ASSET_TO_CONTENT_ATTEMPTS; attempt++) {
      try {
        final Asset asset = findAsset(tx, tx.getBucket(), mavenPath);
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
      catch (IllegalStateException e) {
        // backing blob changed, retry by reloading all again
        log.debug("Failed attempt to create content for asset: {}", mavenPath.getPath(), e);
        lastIllegalStateException = e;
      }
    }
    throw lastIllegalStateException;
  }

  @Override
  public Content put(final MavenPath path, final Payload payload)
      throws IOException, InvalidContentException
  {
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(payload.openInputStream())) {
      return storageFacet.perform(new Operation<Content>()
      {
        @Override
        public Content execute(final StorageTx tx) {
          try {
            return put(tx, path, payload, streamSupplier.get());
          }
          catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    }
    catch (RuntimeException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      throw e;
    }
  }

  @Override
  public Content put(final StorageTx tx, final MavenPath path, final Payload payload)
      throws IOException, InvalidContentException
  {
    return put(tx, path, payload, payload.openInputStream());
  }

  private Content put(final StorageTx tx,
                      final MavenPath path,
                      final Payload payload,
                      final InputStream inputStream)
      throws IOException, InvalidContentException
  {
    log.debug("PUT {} : {}", getRepository().getName(), path.getPath());
    final AssetBlob assetBlob = tx.createBlob(
        path.getPath(),
        inputStream,
        HashType.ALGORITHMS,
        null,
        payload.getContentType()
    );
    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    if (path.getCoordinates() != null) {
      return putArtifact(tx, path, assetBlob, contentAttributes);
    }
    else {
      return putFile(tx, path, assetBlob, contentAttributes);
    }
  }

  private Content putArtifact(final StorageTx tx,
                              final MavenPath path,
                              final AssetBlob assetBlob,
                              @Nullable final AttributesMap contentAttributes)
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

      // Set format specific attributes
      final NestedAttributesMap componentAttributes = component.formatAttributes();
      componentAttributes.set(P_COMPONENT_KEY, getComponentKey(coordinates));
      componentAttributes.set(P_GROUP_ID, coordinates.getGroupId());
      componentAttributes.set(P_ARTIFACT_ID, coordinates.getArtifactId());
      componentAttributes.set(P_VERSION, coordinates.getVersion());
      componentAttributes.set(P_BASE_VERSION, coordinates.getBaseVersion());
      tx.saveComponent(component);
    }

    Asset asset = selectComponentAsset(tx, component, path);
    if (asset == null) {
      asset = tx.createAsset(tx.getBucket(), component);

      asset.name(path.getPath());
      asset.formatAttributes().set(StorageFacet.P_PATH, path.getPath());

      final NestedAttributesMap assetAttributes = asset.formatAttributes();
      assetAttributes.set(P_ASSET_KEY, getAssetKey(path));
      assetAttributes.set(P_GROUP_ID, coordinates.getGroupId());
      assetAttributes.set(P_ARTIFACT_ID, coordinates.getArtifactId());
      assetAttributes.set(P_VERSION, coordinates.getVersion());
      assetAttributes.set(P_BASE_VERSION, coordinates.getBaseVersion());
      assetAttributes.set(P_CLASSIFIER, coordinates.getClassifier());
      assetAttributes.set(P_EXTENSION, coordinates.getExtension());
    }

    putAssetPayload(tx, asset, assetBlob, contentAttributes);
    tx.saveAsset(asset);
    return toContent(tx, path);
  }

  private Content putFile(final StorageTx tx,
                          final MavenPath path,
                          final AssetBlob assetBlob,
                          @Nullable final AttributesMap contentAttributes)
      throws IOException, InvalidContentException
  {
    Asset asset = findAsset(tx, tx.getBucket(), path);
    if (asset == null) {
      asset = tx.createAsset(tx.getBucket(), getRepository().getFormat());
      asset.name(path.getPath());
      asset.formatAttributes().set(StorageFacet.P_PATH, path.getPath());

      final NestedAttributesMap assetAttributes = asset.formatAttributes();
      assetAttributes.set(P_ASSET_KEY, getAssetKey(path));
    }

    putAssetPayload(tx, asset, assetBlob, contentAttributes);
    tx.saveAsset(asset);
    return toContent(tx, path);
  }

  private void putAssetPayload(final StorageTx tx,
                               final Asset asset,
                               final AssetBlob assetBlob,
                               @Nullable final AttributesMap contentAttributes)
      throws IOException
  {
    tx.attachBlob(asset, assetBlob);

    final NestedAttributesMap formatAttributes = asset.formatAttributes();
    if (contentAttributes != null) {
      final DateTime lastModified = contentAttributes.get(Content.CONTENT_LAST_MODIFIED, DateTime.class);
      if (lastModified != null) {
        formatAttributes.set(P_CONTENT_LAST_MODIFIED, lastModified.toDate());
      }
      formatAttributes.set(P_CONTENT_ETAG, contentAttributes.get(Content.CONTENT_ETAG, String.class));
    }
    else {
      formatAttributes.set(P_CONTENT_LAST_MODIFIED, DateTime.now().toDate());
    }
  }

  @Override
  public boolean delete(final MavenPath... paths) throws IOException {
    try (StorageTx tx = storageFacet.openTx()) {
      boolean result = delete(tx, paths);
      tx.commit();
      return result;
    }
  }

  @Override
  public boolean delete(final StorageTx tx, final MavenPath... paths) throws IOException {
    boolean result = false;
    for (MavenPath path : paths) {
      log.debug("DELETE {} : {}", getRepository().getName(), path.getPath());
      if (path.getCoordinates() != null) {
        result = deleteArtifact(path, tx) || result;
      }
      else {
        result = deleteFile(path, tx) || result;
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
    }
    return true;
  }

  private boolean deleteFile(final MavenPath path, final StorageTx tx) throws IOException {
    final Asset asset = findAsset(tx, tx.getBucket(), path);
    if (asset == null) {
      return false;
    }
    tx.deleteAsset(asset);
    return true;
  }

  @Override
  public DateTime getLastVerified(final MavenPath path) throws IOException {
    try (StorageTx tx = storageFacet.openTx()) {
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
    return storageFacet.perform(new Operation<Boolean>()
    {
      @Override
      public Boolean execute(final StorageTx tx) {
        final Asset asset = findAsset(tx, tx.getBucket(), path);
        if (asset == null) {
          return false;
        }
        final NestedAttributesMap attributes = asset.formatAttributes();
        attributes.set(P_LAST_VERIFIED, verified.toDate());
        tx.saveAsset(asset);
        return true;
      }

      @Override
      public String toString() {
        return "setLastVerified(" + path.getPath() + ", " + verified + ")";
      }
    });
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
}
