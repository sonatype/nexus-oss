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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.common.collect.AttributesMap;
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
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;

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

  @Override
  public void putMetadata(final String imageId, final Payload content) throws IOException, InvalidContentException {
    validateImageId(imageId);
    try (final TempStreamSupplier streamSupplier = new TempStreamSupplier(content.openInputStream())) {
      Map<String, Object> data = readJson(streamSupplier);
      if (!data.containsKey("id")) {
        throw new V1ApiException("Missing key 'id' in JSON");
      }
      if (!data.get("id").equals(imageId)) {
        throw new V1ApiException("JSON data contains invalid id");
      }
      // TODO check image belongs to repository
      // TODO check parent exists
      // TODO check parent is accessible via authorisation
      doPutMetadata(imageId, streamSupplier, content);
    }
  }

  @Transactional(retryOn = {ONeedRetryException.class, ORecordDuplicatedException.class})
  protected void doPutMetadata(final String imageId, final Supplier<InputStream> streamSupplier, final Payload payload)
      throws IOException
  {
    StorageTx tx = UnitOfWork.currentTransaction();

    String componentName = imageId;
    String assetNameMetadata = componentName + "/json";

    Bucket bucket = tx.getBucket();
    Component component = getComponent(tx, componentName, bucket);
    Asset asset;
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat()).name(componentName);

      component.formatAttributes().set(DockerAttributes.P_IN_PROGRESS, true);
      tx.saveComponent(component);

      asset = tx.createAsset(bucket, component);
      asset.name(assetNameMetadata);
    }
    else {
      if (!Boolean.TRUE.equals(component.formatAttributes().get(DockerAttributes.P_IN_PROGRESS))) {
        throw new V1ApiException("Image already exists", HttpStatus.CONFLICT);
      }
      asset = findAsset(tx, component, assetNameMetadata);
    }

    // If we reach that point, it means that this is a new image or a retry on a failed push
    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(asset, Content.maintainLastModified(asset, contentAttributes));
    tx.setBlob(asset, assetNameMetadata, streamSupplier.get(), hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(asset);
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
    String assetNameMetadata = componentName + "/json";
    String assetNameLayer = componentName + "/layer";

    Bucket bucket = tx.getBucket();
    Component component = getComponent(tx, componentName, bucket);
    if (component == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    if (!Boolean.TRUE.equals(component.formatAttributes().get(DockerAttributes.P_IN_PROGRESS))) {
      throw new V1ApiException("Image already exists", HttpStatus.CONFLICT);
    }
    Asset assetMetadata = findAsset(tx, component, assetNameMetadata);
    if (assetMetadata == null) {
      throw new V1ApiException("Image not found", HttpStatus.NOT_FOUND);
    }
    Asset asset = findAsset(tx, component, assetNameLayer);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetNameLayer);
    }

    AttributesMap contentAttributes = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
    }
    Content.applyToAsset(asset, Content.maintainLastModified(asset, contentAttributes));
    tx.setBlob(asset, assetNameLayer, streamSupplier.get(), hashAlgorithms, null, payload.getContentType());
    tx.saveAsset(asset);
  }

  private Component getComponent(final StorageTx tx, final String imageId, final Bucket bucket) {
    return tx.findComponentWithProperty(StorageFacet.P_NAME, imageId, bucket);
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

  private Map<String, Object> readJson(final Supplier<InputStream> streamSupplier) {
    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
    try {
      return mapper.readValue(streamSupplier.get(), typeRef);
    }
    catch (Exception e) {
      throw new V1ApiException("Invalid JSON");
    }
  }

  private void validateImageId(final String imageId) {
    if (!validImageId.matcher(imageId).matches()) {
      throw new V1ApiException("Invalid image ID", HttpStatus.NOT_FOUND);
    }
  }

}
