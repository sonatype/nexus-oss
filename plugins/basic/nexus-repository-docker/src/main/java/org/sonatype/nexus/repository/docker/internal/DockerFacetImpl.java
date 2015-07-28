/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.docker.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.TempStreamSupplier;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.docker.DockerFacet;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ComponentQuery;
import org.sonatype.nexus.repository.storage.ComponentQuery.Builder;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.util.TypeTokens;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import org.apache.commons.lang.StringUtils;

import static java.util.Collections.singletonList;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.*;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_PATH;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_VERSION;

/**
 * {@link DockerFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class DockerFacetImpl
    extends FacetSupport
    implements DockerFacet
{

  private final static List<HashAlgorithm> hashAlgorithms = Lists.newArrayList(SHA256);

  private final static ObjectMapper mapper = new ObjectMapper();

  private final static Pattern validLayerId = Pattern.compile("^([a-f0-9]{16}|[a-f0-9]{64})$");

  private final static Pattern validTag = Pattern.compile("^[\\w][\\w.-]{0,127}$");


  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getImagesIndex(final String name) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    String imagesIndexAssetName = name + "/images";
    Bucket bucket = tx.getBucket();
    // check that we have images index asset
    Asset asset = findAsset(tx, bucket, imagesIndexAssetName);
    if (asset == null) {
      throw new V1Exception("images not found", HttpStatus.NOT_FOUND);
    }

    return marshall(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  public void putImagesIndex(final String name, final Payload content) throws IOException {
    try (TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      readImagesIndex(streamSupplier);
      doPutImagesIndex(name, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutImagesIndex(final String name,
                                  final Supplier<InputStream> streamSupplier,
                                  final Payload payload)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    InputStream indexStream;

    String imagesIndexAssetName = name + "/images";

    Bucket bucket = tx.getBucket();
    Asset imagesIndexAsset = findAsset(tx, bucket, imagesIndexAssetName);
    if (imagesIndexAsset == null) {
      imagesIndexAsset = tx.createAsset(bucket, getRepository().getFormat());
      imagesIndexAsset.name(imagesIndexAssetName);
      imagesIndexAsset.formatAttributes().set(P_PATH, "/v1/repositories/" + imagesIndexAssetName);

      indexStream = streamSupplier.get();
    }
    else {
      // merge existing index with the new one
      List<Map<String, Object>> existingIndex = readImagesIndex(blobStreamSupplier(tx, imagesIndexAsset));
      List<Map<String, Object>> newIndex = readImagesIndex(streamSupplier);
      List<Map<String, Object>> mergedIndex = new ArrayList<>(existingIndex);
      for (Map<String, Object> newImage : newIndex) {
        boolean found = false;
        for (Map<String, Object> existingImage : existingIndex) {
          if (Objects.equals(existingImage.get("id"), newImage.get("id")) &&
              Objects.equals(existingImage.get("Tag"), newImage.get("Tag"))) {
            existingImage.putAll(newImage);
            found = true;
            break;
          }
        }
        if (!found) {
          mergedIndex.add(newImage);
        }
      }
      indexStream = new ByteArrayInputStream(mapper.writeValueAsBytes(mergedIndex));
    }

    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(imagesIndexAsset, Content.maintainLastModified(imagesIndexAsset, contentAttributes));
    tx.setBlob(imagesIndexAsset, imagesIndexAssetName, indexStream, hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(imagesIndexAsset);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getLayerMetadata(final String layerId) throws IOException {
    validateLayerId(layerId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = layerId + "/json";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    // check that we have the layer and layer content was fully uploaded
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }

    Content content = marshall(metadataAsset, tx.requireBlob(metadataAsset.requireBlobRef()));

    NestedAttributesMap componentAttributes = metadataAsset.formatAttributes();
    content.getAttributes().set(P_X_DOCKER_SIZE, componentAttributes.get(P_X_DOCKER_SIZE));
    content.getAttributes().set(P_X_DOCKER_CHECKSUM_PAYLOAD, componentAttributes.get(P_X_DOCKER_CHECKSUM_PAYLOAD));

    return content;
  }

  @Override
  public void putLayerMetadata(final String layerId, final Payload content) throws IOException {
    validateLayerId(layerId);
    try (TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      doPutLayerMetadata(layerId, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutLayerMetadata(final String layerId,
                                    final Supplier<InputStream> streamSupplier,
                                    final Payload payload)
      throws IOException
  {
    Map<String, Object> data = readLayerMetadata(streamSupplier);
    if (!data.containsKey("id")) {
      throw new V1Exception("Missing key 'id' in JSON");
    }
    if (!data.get("id").equals(layerId)) {
      throw new V1Exception("JSON data contains invalid id");
    }
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = layerId + "/json";
    String parentId = (String) data.get(DockerAttributes.P_PARENT);

    Bucket bucket = tx.getBucket();

    Asset parentAsset = null;
    List<String> ancestry = null;
    if (parentId != null) {
      // check that we have the parent layer and parent layer content was fully uploaded
      parentAsset = findAsset(tx, bucket, parentId + "/json");
      if (parentAsset == null || Boolean.TRUE.equals(parentAsset.formatAttributes().get(P_IN_PROGRESS))) {
        throw new V1Exception("Image depends on a non existing parent");
      }
    }
    // TODO check that parent asset is accessible via authorisation once we have per assets auth
    if (parentAsset != null) {
      ancestry = parentAsset.formatAttributes().get(P_ANCESTRY, TypeTokens.LIST_STRING);
    }
    if (ancestry == null) {
      ancestry = Lists.newArrayList();
    }
    ancestry.add(0, layerId);

    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null) {
      metadataAsset = tx.createAsset(bucket, getRepository().getFormat());
      metadataAsset.name(metadataAssetName);
      metadataAsset.formatAttributes().set(P_PATH, "/v1/images/" + metadataAssetName);
      metadataAsset.formatAttributes().set(P_IN_PROGRESS, true);
    }
    else {
      if (!Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
        throw new V1Exception("Image already exists", HttpStatus.CONFLICT);
      }
      // cleanup checksums as we have potentially new metadata/layer
      metadataAsset.formatAttributes().remove(P_X_DOCKER_CHECKSUM_PAYLOAD);
    }
    metadataAsset.formatAttributes().set(P_ANCESTRY, ancestry);
    metadataAsset.formatAttributes().set(P_ARCH, data.get("os"));
    metadataAsset.formatAttributes().set(P_OS, data.get("architecture"));

    // If we reach that point, it means that this is a new image or a retry on a failed push
    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(metadataAsset, Content.maintainLastModified(metadataAsset, contentAttributes));
    tx.setBlob(metadataAsset, metadataAssetName, streamSupplier.get(), hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(metadataAsset);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getLayerContent(final String layerId) throws IOException {
    validateLayerId(layerId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = layerId + "/json";
    String layerAssetName = layerId + "/layer";

    Bucket bucket = tx.getBucket();
    // check that we have the layer and layer content was fully uploaded
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }
    // check that we have layer content
    Asset layerAsset = findAsset(tx, bucket, layerAssetName);
    if (layerAsset == null) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }

    return marshall(layerAsset, tx.requireBlob(layerAsset.requireBlobRef()));
  }

  @Override
  public void putLayerContent(final String layerId, final Payload content) throws IOException, InvalidContentException {
    validateLayerId(layerId);
    try (TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      doPutLayerContent(layerId, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutLayerContent(final String layerId,
                                   final Supplier<InputStream> streamSupplier,
                                   final Payload payload)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = layerId + "/json";
    String layerAssetName = layerId + "/layer";

    Bucket bucket = tx.getBucket();
    // check that layer metadata was previously uploaded
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }
    // check that image was not already fully updated
    if (!Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1Exception("Image already exists", HttpStatus.CONFLICT);
    }
    Asset layerAsset = findAsset(tx, bucket, layerAssetName);
    if (layerAsset == null) {
      layerAsset = tx.createAsset(bucket, getRepository().getFormat());
      layerAsset.name(layerAssetName);
      layerAsset.formatAttributes().set(P_PATH, "/v1/images/" + layerAssetName);
    }

    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(layerAsset, Content.maintainLastModified(layerAsset, contentAttributes));
    tx.setBlob(layerAsset, layerAssetName, streamSupplier.get(), hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(layerAsset);

    // calculate sha256 of layer metadata + "\n" + layer content
    Hasher hasher = Hashing.sha256().newHasher();
    OutputStream output = Funnels.asOutputStream(hasher);
    try (InputStream in = tx.requireBlob(metadataAsset.requireBlobRef()).getInputStream()) {
      ByteStreams.copy(in, output);
    }
    hasher.putString("\n", Charset.forName("UTF-8"));
    try (InputStream in = tx.requireBlob(layerAsset.requireBlobRef()).getInputStream()) {
      ByteStreams.copy(in, output);
    }

    // set extra component attributes
    metadataAsset.formatAttributes().set(P_X_DOCKER_SIZE, layerAsset.size());
    metadataAsset.formatAttributes().set(P_X_DOCKER_CHECKSUM_PAYLOAD, SHA256.name() + ":" + hasher.hash());
    tx.saveAsset(metadataAsset);
  }

  @Override
  public void putLayerChecksum(final String layerId, final String checksum) {
    validateLayerId(layerId);
    // checksum must be specified (cannot be null)
    if (checksum == null) {
      throw new V1Exception(
          P_X_DOCKER_CHECKSUM_PAYLOAD +
              " not set.  If you are using the Docker daemon, you should upgrade to version 0.10 or later",
          HttpStatus.PRECONDITION_FAILED
      );
    }
    // checksum must be specified (cannot be blank)
    if (StringUtils.isBlank(checksum)) {
      throw new V1Exception("Missing Image's checksum");
    }
    // checksum should be of format <checksum algorithm>:<checksum>
    if (checksum.split(":").length != 2) {
      throw new V1Exception("Invalid checksum format");
    }
    doPutLayerChecksum(layerId, checksum);
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutLayerChecksum(final String layerId, final String checksum)
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = layerId + "/json";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    // check that we have the layer
    if (metadataAsset == null) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }
    // check that layer content was fully uploaded
    if (!Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1Exception("Cannot set this image checksum", HttpStatus.CONFLICT);
    }
    // compare checksum with pre-calculated checksum (calculated when layer content was uploaded)
    Object actualChecksum = metadataAsset.formatAttributes().get(P_X_DOCKER_CHECKSUM_PAYLOAD);
    if (!checksum.equals(actualChecksum)) {
      log.error("Wrong checksum. Actual: {}; Expected: {}", actualChecksum, checksum);
      throw new V1Exception("Checksum mismatch");
    }
    // checksum match, so mark layer as fully uploaded
    metadataAsset.formatAttributes().remove(P_IN_PROGRESS);
    tx.saveAsset(metadataAsset);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getLayerAncestry(final String layerId) throws IOException {
    validateLayerId(layerId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = layerId + "/json";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    // check that we have the layer and layer content was fully uploaded
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }

    String ancestry = mapper.writeValueAsString(metadataAsset.formatAttributes().get(P_ANCESTRY));
    return new StringPayload(ancestry, Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getTags(final String name) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    Iterable<Component> tagComponents = findComponents(tx, name);
    Map<String, String> tags = new HashMap<>();
    for (Component tagComponent : tagComponents) {
      tags.put(tagComponent.version(), tagComponent.formatAttributes().get(P_LAYER_ID, String.class));
    }
    if (tags.isEmpty()) {
      throw new V1Exception("images not found", HttpStatus.NOT_FOUND);
    }
    return new StringPayload(mapper.writeValueAsString(tags), Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getTag(final String name, final String tag) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    Component tagComponent = findComponent(tx, name, tag);
    if (tagComponent == null) {
      throw new V1Exception("Tag not found", HttpStatus.NOT_FOUND);
    }
    String layerId = tagComponent.formatAttributes().get(P_LAYER_ID, String.class);
    return new StringPayload(mapper.writeValueAsString(layerId), Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
  }

  @Override
  public void putTag(final String name,
                     final String tag,
                     final Map<String, String> daemonMetadata,
                     final Payload content)
      throws IOException
  {
    validateTagName(tag);
    try (TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      doPutTag(name, tag, readLayerId(streamSupplier), daemonMetadata);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutTag(final String name,
                          final String tag,
                          final String layerId,
                          final Map<String, String> daemonMetadata)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String imageIndexAssetName = name + "/images";
    String metadataAssetName = layerId + "/json";

    Bucket bucket = tx.getBucket();
    // check that we have an image index
    Asset imageIndexAsset = findAsset(tx, bucket, imageIndexAssetName);
    if (imageIndexAsset == null) {
      throw new V1Exception("images not found", HttpStatus.NOT_FOUND);
    }
    // check that we have the layer specified by tag and layer content was fully uploaded
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1Exception("Image not found", HttpStatus.NOT_FOUND);
    }

    // create a component / name / tag (version) if we do not already have one
    Component tagComponent = findComponent(tx, name, tag);
    if (tagComponent == null) {
      tagComponent = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .version(tag);
    }

    // set image name and tag
    tagComponent.formatAttributes().set(P_IMAGE_NAME, name);
    tagComponent.formatAttributes().set(P_IMAGE_TAG, tag);
    // set layer reference
    tagComponent.formatAttributes().set(P_LAYER_ID, layerId);
    // set docker daemon metadata
    NestedAttributesMap tagDaemonMetadata = tagComponent.formatAttributes().child(P_DAEMON_METADATA);
    tagDaemonMetadata.clear();
    for (Entry<String, String> entry : daemonMetadata.entrySet()) {
      tagDaemonMetadata.set(entry.getKey(), entry.getValue());
    }
    tx.saveComponent(tagComponent);
  }

  /**
   * Find a component by its name and tag (version)
   *
   * @param tx   storage transaction
   * @param name image name
   * @param tag  image tag
   * @return found component of null if not found
   */
  @Nullable
  private Component findComponent(final StorageTx tx, final String name, final String tag) {
    ComponentQuery query = new Builder()
        .where(P_NAME + "=").param(name)
        .where(" AND " + P_VERSION + "=").param(tag)
        .build();
    Iterable<Component> components = tx.findComponents(
        query.getWhere(), query.getParameters(), singletonList(getRepository()), null
    );
    if (components.iterator().hasNext()) {
      return components.iterator().next();
    }
    return null;
  }

  /**
   * Find all components (tags) given a component name.
   *
   * @param tx   storage transaction
   * @param name image name
   * @return found components
   */
  private Iterable<Component> findComponents(final StorageTx tx, final String name) {
    ComponentQuery query = new Builder()
        .where(P_NAME + "=").param(name)
        .build();
    return tx.findComponents(
        query.getWhere(), query.getParameters(), singletonList(getRepository()), null
    );
  }

  /**
   * Find an asset by its name.
   *
   * @param tx        storage transaction
   * @param bucket    repository bucket
   * @param assetName asset name
   * @return found asset or null if not found
   */
  @Nullable
  private Asset findAsset(final StorageTx tx, final Bucket bucket, final String assetName) {
    return tx.findAssetWithProperty(StorageFacet.P_NAME, assetName, bucket);
  }

  /**
   * Validate that layer metadata is a proper JSON map.
   *
   * @param layerMetadataContent layer metadata content
   * @return parsed layer metadata
   */
  private Map<String, Object> readLayerMetadata(final Supplier<InputStream> layerMetadataContent) {
    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
    try {
      return mapper.readValue(layerMetadataContent.get(), typeRef);
    }
    catch (Exception e) {
      throw new V1Exception("Invalid JSON");
    }
  }

  /**
   * Validate that images index is a proper JSON list containing layer references.
   *
   * @param imagesIndexContent images index content
   * @return parsed images index
   */
  private List<Map<String, Object>> readImagesIndex(final Supplier<InputStream> imagesIndexContent) throws IOException {
    try (InputStream in = imagesIndexContent.get()) {
      TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
      try {
        return mapper.readValue(in, typeRef);
      }
      catch (Exception e) {
        throw new V1Exception("Invalid JSON");
      }
    }
  }

  /**
   * Validate layer id.
   *
   * @param layerId to validate
   * @return validated layer id
   */
  private String validateLayerId(final String layerId) {
    if (!validLayerId.matcher(layerId).matches()) {
      throw new V1Exception("Invalid image ID", HttpStatus.NOT_FOUND);
    }
    return layerId;
  }

  /**
   * Validate tag name.
   *
   * @param tag to validate
   */
  private void validateTagName(final String tag) {
    if (!validTag.matcher(tag).matches()) {
      throw new V1Exception("Invalid tag name (must match ${VALID_TAG})");
    }
  }

  /**
   * Read layer id.
   *
   * @param streamSupplier layer id content
   * @return parsed layer id
   */
  private String readLayerId(final Supplier<InputStream> streamSupplier) {
    try {
      return validateLayerId(mapper.readValue(streamSupplier.get(), String.class));
    }
    catch (Exception e) {
      throw new V1Exception("Invalid data");
    }
  }

  /**
   * Convert an asset blob to {@link Content}.
   *
   * @param asset asset
   * @param blob  blob
   * @return content
   */
  private Content marshall(final Asset asset, final Blob blob) {
    Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
    Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
    return content;
  }

  /**
   * Create a supplier for an asset blob.
   *
   * @param tx    storage transaction
   * @param asset asset
   * @return asset blob content supplier
   */
  private Supplier<InputStream> blobStreamSupplier(final StorageTx tx, final Asset asset) {
    return new Supplier<InputStream>()
    {
      @Override
      public InputStream get() {
        return tx.requireBlob(asset.requireBlobRef()).getInputStream();
      }
    };
  }

}
