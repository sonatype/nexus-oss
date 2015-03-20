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
package org.sonatype.nexus.repository.search;

import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BLOB_REF;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_SIZE;

/**
 * Default {@link ComponentMetadataProducer} implementation that uses all properties of a component & its assets as
 * metadata.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultComponentMetadataProducer
    implements ComponentMetadataProducer
{

  @Override
  public String getMetadata(final Component component) {
    checkNotNull(component);
    Map<String, Object> metadata = Maps.newHashMap();
    for (String key : component.propertyNames()) {
      metadata.put(key, component.vertex().getProperty(key));
    }
    List<Map<String, Object>> allAssetMetadata = Lists.newArrayList();
    for (Asset asset : component.assets()) {
      Map<String, Object> assetMetadata = Maps.newHashMap();
      for (String key : asset.propertyNames()) {
        if (!P_BLOB_REF.equals(key) && !P_SIZE.equals(key)) {
          assetMetadata.put(key, asset.vertex().getProperty(key));
        }
      }
      allAssetMetadata.add(assetMetadata);
    }
    if (!allAssetMetadata.isEmpty()) {
      metadata.put("assets", allAssetMetadata.toArray(new Map[allAssetMetadata.size()]));
    }

    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
    }
    catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

}
