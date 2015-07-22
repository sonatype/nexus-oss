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
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_ANCESTRY;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_IMAGE_ID;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_IN_PROGRESS;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_METADATA;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_REPOSITORY;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_CHECKSUM_PAYLOAD;
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_SIZE;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_GROUP;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_PATH;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_VERSION;

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

    String imagesIndexAssetName = namespace + "/" + name + "/images";
    Bucket bucket = tx.getBucket();
    Asset asset = findAsset(tx, bucket, imagesIndexAssetName);
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

    String imagesIndexAssetName = namespace + "/" + name + "/images";

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
    // FIXME Nexus is changing content type from application/json to text/plain even if is a json file
    tx.setBlob(imagesIndexAsset, imagesIndexAssetName, indexStream, hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(imagesIndexAsset);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getMetadata(final String imageId) throws IOException {
    validateImageId(imageId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = imageId + "/json";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    Content content = marshall(metadataAsset, tx.requireBlob(metadataAsset.requireBlobRef()));

    NestedAttributesMap componentAttributes = metadataAsset.formatAttributes();
    content.getAttributes().set(P_X_DOCKER_SIZE, componentAttributes.get(P_X_DOCKER_SIZE));
    content.getAttributes().set(P_X_DOCKER_CHECKSUM_PAYLOAD, componentAttributes.get(P_X_DOCKER_CHECKSUM_PAYLOAD));

    return content;
  }

  @Override
  public void putMetadata(final String imageId, final Payload content) throws IOException {
    validateImageId(imageId);
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      doPutMetadata(imageId, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutMetadata(final String imageId,
                               final Supplier<InputStream> streamSupplier,
                               final Payload payload)
      throws IOException
  {
    Map<String, Object> data = readMetadata(streamSupplier);
    if (!data.containsKey("id")) {
      throw new V1ApiException("Missing key 'id' in JSON");
    }
    if (!data.get("id").equals(imageId)) {
      throw new V1ApiException("JSON data contains invalid id");
    }
    // TODO check image belongs to repository?
    // TODO check parent is accessible via authorisation?
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = imageId + "/json";
    String parentId = (String) data.get(DockerAttributes.P_PARENT);

    Bucket bucket = tx.getBucket();

    Asset parentAsset = null;
    List<String> ancestry = null;
    if (parentId != null && (parentAsset = findAsset(tx, bucket, parentId + "/json")) == null) {
      throw new V1ApiException("Image depends on a non existing parent");
    }
    if (parentAsset != null) {
      ancestry = parentAsset.formatAttributes().get(P_ANCESTRY, TypeTokens.LIST_STRING);
    }
    if (ancestry == null) {
      ancestry = Lists.newArrayList();
    }
    ancestry.add(0, imageId);

    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null) {
      metadataAsset = tx.createAsset(bucket, getRepository().getFormat());
      metadataAsset.name(metadataAssetName);
      metadataAsset.formatAttributes().set(P_PATH, "/v1/images/" + metadataAssetName);
      metadataAsset.formatAttributes().set(P_IN_PROGRESS, true);
    }
    else {
      if (!Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
        throw new V1ApiException("Image already exists", HttpStatus.CONFLICT);
      }
      // cleanup checksums as we have potentially new metadata/layer
      metadataAsset.formatAttributes().remove(P_X_DOCKER_CHECKSUM_PAYLOAD);
    }
    metadataAsset.formatAttributes().set(P_ANCESTRY, ancestry);

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
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getLayer(final String imageId) throws IOException {
    validateImageId(imageId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = imageId + "/json";
    String layerAssetName = imageId + "/layer";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Asset layerAsset = findAsset(tx, bucket, layerAssetName);
    if (layerAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    return marshall(layerAsset, tx.requireBlob(layerAsset.requireBlobRef()));
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

    String metadataAssetName = imageId + "/json";
    String layerAssetName = imageId + "/layer";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    if (!Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image already exists", HttpStatus.CONFLICT);
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
    metadataAsset.formatAttributes().set(P_X_DOCKER_SIZE, layerAsset.size());
    metadataAsset.formatAttributes().set(P_X_DOCKER_CHECKSUM_PAYLOAD, SHA256.name() + ":" + hasher.hash());
    tx.saveAsset(metadataAsset);
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

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getAncestry(final String imageId) throws IOException {
    validateImageId(imageId);
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = imageId + "/json";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    String ancestry = mapper.writeValueAsString(metadataAsset.formatAttributes().get(P_ANCESTRY));
    return new StringPayload(ancestry, Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutChecksum(final String imageId, final String checksum)
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String metadataAssetName = imageId + "/json";

    Bucket bucket = tx.getBucket();
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    if (!Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Cannot set this image checksum", HttpStatus.CONFLICT);
    }
    Object actualChecksum = metadataAsset.formatAttributes().get(P_X_DOCKER_CHECKSUM_PAYLOAD);
    if (!checksum.equals(actualChecksum)) {
      log.error("Wrong checksum. Actual: {}; Expected: {}", actualChecksum, checksum);
      throw new V1ApiException("Checksum mismatch");
    }
    metadataAsset.formatAttributes().remove(P_IN_PROGRESS);
    tx.saveAsset(metadataAsset);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getTags(final String namespace, final String name) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    Iterable<Component> tagComponents = findComponents(tx, namespace, name);
    Map<String, String> tags = new HashMap<>();
    for (Component tagComponent : tagComponents) {
      tags.put(tagComponent.version(), tagComponent.formatAttributes().get(P_IMAGE_ID, String.class));
    }
    if (tags.isEmpty()) {
      throw new V1ApiException("images not found", HttpStatus.NOT_FOUND);
    }
    return new StringPayload(mapper.writeValueAsString(tags), Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
  }

  @Override
  @Transactional(retryOn = IllegalStateException.class)
  public Payload getTag(final String namespace, final String name, final String tag) throws IOException {
    StorageTx tx = UnitOfWork.currentTransaction();

    Component tagComponent = findComponent(tx, namespace, name, tag);
    if (tagComponent == null) {
      throw new V1ApiException("Tag not found", HttpStatus.NOT_FOUND);
    }
    String imageId = tagComponent.formatAttributes().get(P_IMAGE_ID, String.class);
    return new StringPayload(mapper.writeValueAsString(imageId), Charsets.UTF_8, ContentTypes.APPLICATION_JSON);
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
      doPutTag(namespace, name, tag, readLayer(streamSupplier), metadata);
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

    String imageIndexAssetName = namespace + "/" + name + "/images";
    String metadataAssetName = imageId + "/json";

    Bucket bucket = tx.getBucket();
    Asset imageIndexAsset = findAsset(tx, bucket, imageIndexAssetName);
    if (imageIndexAsset == null) {
      throw new V1ApiException("images not found", HttpStatus.NOT_FOUND);
    }
    Asset metadataAsset = findAsset(tx, bucket, metadataAssetName);
    if (metadataAsset == null || Boolean.TRUE.equals(metadataAsset.formatAttributes().get(P_IN_PROGRESS))) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }

    Component tagComponent = findComponent(tx, namespace, name, tag);
    if (tagComponent == null) {
      tagComponent = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .group(namespace)
          .version(tag);
    }

    tagComponent.formatAttributes().set(P_REPOSITORY, namespace + "/" + name);
    // set layer reference
    tagComponent.formatAttributes().set(P_IMAGE_ID, imageId);
    // set metadata
    NestedAttributesMap metadataTag = tagComponent.formatAttributes().child(P_METADATA);
    metadataTag.clear();
    for (Entry<String, String> entry : metadata.entrySet()) {
      metadataTag.set(entry.getKey(), entry.getValue());
    }
    tx.saveComponent(tagComponent);
  }

  private Component findComponent(final StorageTx tx, final String namespace, final String name, final String version) {
    final ComponentQuery query = new Builder()
        .where(P_NAME + "=").param(name)
        .where(" AND " + P_GROUP + "=").param(namespace)
        .where(" AND " + P_VERSION + "=").param(version)
        .build();
    final Iterable<Component> components = tx.findComponents(
        query.getWhere(), query.getParameters(), singletonList(getRepository()), null
    );
    if (components.iterator().hasNext()) {
      return components.iterator().next();
    }
    return null;
  }

  private Iterable<Component> findComponents(final StorageTx tx, final String namespace, final String name) {
    final ComponentQuery query = new Builder()
        .where(P_NAME + "=").param(name)
        .where(" AND " + P_GROUP + "=").param(namespace)
        .build();
    return tx.findComponents(
        query.getWhere(), query.getParameters(), singletonList(getRepository()), null
    );
  }

  private Asset findAsset(final StorageTx tx, final Bucket bucket, final String assetName) {
    return tx.findAssetWithProperty(StorageFacet.P_NAME, assetName, bucket);
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

  private List<Map<String, Object>> readImagesIndex(final Supplier<InputStream> streamSupplier) throws IOException {
    try (InputStream in = streamSupplier.get()) {
      TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
      try {
        return mapper.readValue(streamSupplier.get(), typeRef);
      }
      catch (Exception e) {
        throw new V1ApiException("Invalid JSON");
      }
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

  private String readLayer(final Supplier<InputStream> streamSupplier) {
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

  private Supplier<InputStream> blobStreamSupplier(final StorageTx tx, final Asset imagesIndexAsset) {
    return new Supplier<InputStream>()
    {
      @Override
      public InputStream get() {
        return tx.requireBlob(imagesIndexAsset.requireBlobRef()).getInputStream();
      }
    };
  }

}
