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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.StorageFacet.E_PART_OF_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BLOB_REF;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_SIZE;

/**
 * Default {@link IndexDataProducer} implementation that indexes all properties of a component & its assets.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultIndexDataProducer
    implements IndexDataProducer
{

  @Override
  public String getIndexData(final OrientVertex component) {
    checkNotNull(component);
    Map<String, Object> source = Maps.newHashMap();
    for (String key : component.getPropertyKeys()) {
      source.put(key, component.getProperty(key));
    }
    List<Map<String, Object>> assets = Lists.newArrayList();
    for (Vertex vertex : component.getVertices(Direction.IN, E_PART_OF_COMPONENT)) {
      OrientVertex asset = (OrientVertex) vertex;
      Map<String, Object> assetSource = Maps.newHashMap();
      for (String key : asset.getPropertyKeys()) {
        if (!P_BLOB_REF.equals(key) && !P_SIZE.equals(key)) {
          assetSource.put(key, asset.getProperty(key));
        }
      }
      assets.add(assetSource);
    }
    if (!assets.isEmpty()) {
      source.put("assets", assets.toArray(new Map[assets.size()]));
    }

    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(source);
    }
    catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

}
