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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
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

import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_IN_PROGRESS;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_TAG;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_TAGS;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_CHECKSUM_PAYLOAD;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_SIZE;

/**
 * @since 3.0
 */
@Named
public class DockerFacetImpl
    extends FacetSupport
    implements DockerFacet
{

  private final static List<HashAlgorithm> hashAlgorithms = Lists.newArrayList(SHA256);

  private final static ObjectMapper mapper = new ObjectMapper();

  private final static Pattern validImageId = Pattern.compile("^([a-f0-9]{16}|[a-f0-9]{64})$");

  private final static Pattern validTag = Pattern.compile("^[\\w][\\w.-]{0,127}$");


  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getImagesIndex(final String namespace, final String name) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    String assetName = namespace + "/" + name + "/index_images";
    Asset asset = findAsset(tx, tx.getBucket(), assetName);
    if (asset == null) {
      throw new V1ApiException("images not found", HttpStatus.NOT_FOUND);
    }

    return marshall(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  public void putImagesIndex(final String namespace, final String name, final Payload content) throws IOException {
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      validateImagesIndex(streamSupplier);
      doPutImagesIndex(namespace, name, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutImagesIndex(final String namespace,
                                  final String name,
                                  final Supplier<InputStream> streamSupplier,
                                  final Payload payload)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    InputStream indexStream;

    String assetName = namespace + "/" + name + "/index_images";

    Bucket bucket = tx.getBucket();
    Asset asset = findAsset(tx, bucket, assetName);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetName);

      indexStream = streamSupplier.get();
    }
    else {
      String existingIndex;
      try (InputStream in = tx.requireBlob(asset.requireBlobRef()).getInputStream()) {
        existingIndex = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
      }
      String newIndex;
      try (InputStream in = streamSupplier.get()) {
        newIndex = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
      }
      // TODO merge existing images index with the new one
      String mergedIndex = existingIndex;
      indexStream = new ByteArrayInputStream(mergedIndex.getBytes(Charsets.UTF_8));
    }

    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(asset, Content.maintainLastModified(asset, contentAttributes));
    // FIXME Nexus is changing content type from application/json to text/plain even if is a json file
    tx.setBlob(asset, assetName, indexStream, hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(asset);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getMetadata(final String imageId) throws IOException {
    validateImageId(imageId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String componentName = imageId;
    String metadataAssetName = componentName + "/json";

    Bucket bucket = tx.getBucket();
    Component component = findComponent(tx, bucket, componentName);
    if (component == null || Boolean.TRUE.equals(component.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Asset metadataAsset = findAsset(tx, component, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    Content content = marshall(metadataAsset, tx.requireBlob(metadataAsset.requireBlobRef()));

    NestedAttributesMap componentAttributes = component.formatAttributes();
    content.getAttributes().set(P_X_DOCKER_SIZE, componentAttributes.get(P_X_DOCKER_SIZE));
    content.getAttributes().set(P_X_DOCKER_CHECKSUM_PAYLOAD, componentAttributes.get(P_X_DOCKER_CHECKSUM_PAYLOAD));

    return content;
  }

  @Override
  public void putMetadata(final String imageId, final Payload content) throws IOException {
    validateImageId(imageId);
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      Map<String, Object> data = readMetadata(streamSupplier);
      if (!data.containsKey("id")) {
        throw new V1ApiException("Missing key 'id' in JSON");
      }
      if (!data.get("id").equals(imageId)) {
        throw new V1ApiException("JSON data contains invalid id");
      }
      // TODO check image belongs to repository?
      // TODO check parent exists
      // TODO check parent is accessible via authorisation?
      doPutMetadata(imageId, streamSupplier, content);
    }
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getLayer(final String imageId) throws IOException {
    validateImageId(imageId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String componentName = imageId;
    String layerAssetName = componentName + "/layer";

    Bucket bucket = tx.getBucket();
    Component component = findComponent(tx, bucket, componentName);
    if (component == null || Boolean.TRUE.equals(component.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Asset layerAsset = findAsset(tx, component, layerAssetName);
    if (layerAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    return marshall(layerAsset, tx.requireBlob(layerAsset.requireBlobRef()));
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutMetadata(final String imageId, final Supplier<InputStream> streamSupplier, final Payload payload)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String componentName = imageId;
    String metadataAssetName = componentName + "/json";

    Bucket bucket = tx.getBucket();
    Component component = findComponent(tx, bucket, componentName);
    Asset metadataAsset = null;
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat()).name(componentName);

      component.formatAttributes().set(P_IN_PROGRESS, true);
      tx.saveComponent(component);
    }
    else {
      if (!Boolean.TRUE.equals(component.formatAttributes().get(P_IN_PROGRESS))) {
        throw new V1ApiException("Image already exists", HttpStatus.CONFLICT);
      }
      metadataAsset = findAsset(tx, component, metadataAssetName);

      // cleanup checksums as we have potentially new metadata/layer
      component.formatAttributes().remove(P_X_DOCKER_CHECKSUM_PAYLOAD);
      tx.saveComponent(component);
    }
    if (metadataAsset == null) {
      metadataAsset = tx.createAsset(bucket, component);
      metadataAsset.name(metadataAssetName);
    }

    // If we reach that point, it means that this is a new image or a retry on a failed push
    // TODO Put attributes in component/asset based on parsed metadata json file
    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(metadataAsset, Content.maintainLastModified(metadataAsset, contentAttributes));
    // FIXME Nexus is changing content type from application/json to text/plain even if is a json file
    tx.setBlob(metadataAsset, metadataAssetName, streamSupplier.get(), hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(metadataAsset);

    // TODO generate ancestry (maybe not in transaction?)
  }

  @Override
  public void putLayer(final String imageId, final Payload content) throws IOException, InvalidContentException {
    validateImageId(imageId);
    // TODO check docker client version >= 0.10
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      doPutLayer(imageId, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutLayer(final String imageId, final Supplier<InputStream> streamSupplier, final Payload payload)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String componentName = imageId;
    String metadataAssetName = componentName + "/json";
    String layerAssetName = componentName + "/layer";

    Bucket bucket = tx.getBucket();
    Component component = findComponent(tx, bucket, componentName);
    if (component == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    if (!Boolean.TRUE.equals(component.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image already exists", HttpStatus.CONFLICT);
    }
    Asset metadataAsset = findAsset(tx, component, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Asset layerAsset = findAsset(tx, component, layerAssetName);
    if (layerAsset == null) {
      layerAsset = tx.createAsset(bucket, component);
      layerAsset.name(layerAssetName);
    }

    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(layerAsset, Content.maintainLastModified(layerAsset, contentAttributes));
    // FIXME Nexus is changing content type from application/octet-stream to application/gzip
    tx.setBlob(layerAsset, layerAssetName, streamSupplier.get(), hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(layerAsset);

    // calculate sha256 of /json + "\n" + /layer
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
    component.formatAttributes().set(P_X_DOCKER_SIZE, layerAsset.size());
    component.formatAttributes().set(P_X_DOCKER_CHECKSUM_PAYLOAD, SHA256.name() + ":" + hasher.hash());
    tx.saveComponent(component);
  }

  @Override
  public void putChecksum(final String imageId, final String checksum) {
    validateImageId(imageId);
    // TODO check docker client version >= 0.10 (same exception as bellow)
    if (checksum == null) {
      throw new V1ApiException(
          P_X_DOCKER_CHECKSUM_PAYLOAD +
              " not set.  If you are using the Docker daemon, you should upgrade to version 0.10 or later",
          HttpStatus.PRECONDITION_FAILED
      );
    }
    if (StringUtils.isBlank(checksum)) {
      throw new V1ApiException("Missing Image's checksum");
    }
    if (checksum.split(":").length != 2) {
      throw new V1ApiException("Invalid checksum format");
    }
    doPutChecksum(imageId, checksum);
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutChecksum(final String imageId, final String checksum)
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String componentName = imageId;
    String metadataAssetName = componentName + "/json";

    Bucket bucket = tx.getBucket();
    Component component = findComponent(tx, bucket, componentName);
    if (component == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    if (!Boolean.TRUE.equals(component.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Cannot set this image checksum", HttpStatus.CONFLICT);
    }
    Asset metadataAsset = findAsset(tx, component, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Object actualChecksum = component.formatAttributes().get(P_X_DOCKER_CHECKSUM_PAYLOAD);
    if (!checksum.equals(actualChecksum)) {
      log.error("Wrong checksum. Actual: {}; Expected: {}", actualChecksum, checksum);
      throw new V1ApiException("Checksum mismatch");
    }
    component.formatAttributes().remove(P_IN_PROGRESS);
    tx.saveComponent(component);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getTags(final String namespace, final String name) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    String imageIndexAssetName = namespace + "/" + name + "/index_images";
    Bucket bucket = tx.getBucket();
    Asset imageIndexAsset = findAsset(tx, bucket, imageIndexAssetName);
    if (imageIndexAsset == null) {
      throw new V1ApiException("images not found", HttpStatus.NOT_FOUND);
    }
    NestedAttributesMap tagsMap = imageIndexAsset.formatAttributes().child(P_TAGS);
    String tags = mapper.writeValueAsString(tagsMap.backing());
    return new StringPayload(tags, Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
  }

  @Override
  public void putTag(final String namespace,
                     final String name,
                     final String tag,
                     final Map<String, String> metadata,
                     final Payload content)
      throws IOException
  {
    validateTagName(tag);
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      doPutTag(namespace, name, tag, readImageId(streamSupplier), metadata);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutTag(final String namespace,
                          final String name,
                          final String tag,
                          final String imageId,
                          final Map<String, String> metadata)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String imageIndexAssetName = namespace + "/" + name + "/index_images";
    String componentName = imageId;
    String metadataAssetName = componentName + "/json";

    Bucket bucket = tx.getBucket();
    Asset imageIndexAsset = findAsset(tx, bucket, imageIndexAssetName);
    if (imageIndexAsset == null) {
      throw new V1ApiException("images not found", HttpStatus.NOT_FOUND);
    }
    Component component = findComponent(tx, bucket, componentName);
    if (component == null || Boolean.TRUE.equals(component.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Asset metadataAsset = findAsset(tx, component, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    // add tag to image index asset
    imageIndexAsset.formatAttributes().child(P_TAGS).set(tag, imageId);
    // add metadata to image index asset
    // FIXME would be nicer to have the child tag above have as value image id and metadata
    NestedAttributesMap metadataTag = imageIndexAsset.formatAttributes().child(P_TAG + "_" + tag);
    metadataTag.clear();
    for (Entry<String, String> entry : metadata.entrySet()) {
      metadataTag.set(entry.getKey(), entry.getValue());
    }
    tx.saveAsset(imageIndexAsset);
  }

  private Component findComponent(final StorageTx tx, final Bucket bucket, final String imageId) {
    return tx.findComponentWithProperty(StorageFacet.P_NAME, imageId, bucket);
  }

  private Asset findAsset(final StorageTx tx, final Bucket bucket, final String assetName) {
    return tx.findAssetWithProperty(StorageFacet.P_NAME, assetName, bucket);
  }

  @Nullable
  private Asset findAsset(final StorageTx tx, final Component component, final String name)
  {
    for (Asset asset : tx.browseAssets(component)) {
      if (asset.name().endsWith(name)) {
        return asset;
      }
    }
    return null;
  }

  private void validateImagesIndex(final Supplier<InputStream> streamSupplier) {
    TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
    try {
      mapper.readValue(streamSupplier.get(), typeRef);
    }
    catch (Exception e) {
      throw new V1ApiException("Invalid data");
    }
  }

  private Map<String, Object> readMetadata(final Supplier<InputStream> streamSupplier) {
    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
    try {
      return mapper.readValue(streamSupplier.get(), typeRef);
    }
    catch (Exception e) {
      throw new V1ApiException("Invalid JSON");
    }
  }

  private String validateImageId(final String imageId) {
    if (!validImageId.matcher(imageId).matches()) {
      throw new V1ApiException("Invalid image ID", HttpStatus.NOT_FOUND);
    }
    return imageId;
  }

  private void validateTagName(final String tag) {
    if (!validTag.matcher(tag).matches()) {
      throw new V1ApiException("Invalid tag name (must match ${VALID_TAG})");
    }
  }

  private String readImageId(final Supplier<InputStream> streamSupplier) {
    try {
      return validateImageId(mapper.readValue(streamSupplier.get(), String.class));
    }
    catch (Exception e) {
      throw new V1ApiException("Invalid data");
    }
  }

  private Content marshall(final Asset asset, final Blob blob) {
    final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
    Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
    return content;
  }

}
