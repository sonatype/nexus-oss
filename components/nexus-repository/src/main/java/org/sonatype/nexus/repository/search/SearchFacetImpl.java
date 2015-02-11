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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.elasticsearch.client.Client;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;
import static org.sonatype.nexus.repository.storage.StorageFacet.E_PART_OF_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CONTENT_TYPE;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_GROUP;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_LAST_UPDATED;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * Default {@link StorageFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class SearchFacetImpl
    extends FacetSupport
    implements SearchFacet
{

  private static final String TYPE = "component";

  private final Provider<Client> client;

  @Inject
  public SearchFacetImpl(final Provider<Client> client) {
    this.client = checkNotNull(client);
  }

  @Override
  @Guarded(by = STARTED)
  public void index(final Object vertexId) {
    try (StorageTx tx = getStorage().openTx()) {
      OrientVertex bucket = tx.getBucket();
      OrientVertex component = tx.findComponent(vertexId, bucket);
      if (component == null) {
        // component may had been deleted in the mean time
        // TODO log?
        return;
      }
      index(bucket, component);
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void deindex(final Object vertexId) {
    // TODO shall
    client.get().prepareDelete(getRepository().getName(), TYPE, vertexId.toString()).execute();
  }

  @Override
  @Guarded(by = STARTED)
  public void reindex() {
    // TODO shall we block indexing during re-index?
    try (StorageTx tx = getStorage().openTx()) {
      OrientVertex bucket = tx.getBucket();
      deleteIndex();
      configureIndex();
      for (OrientVertex component : tx.browseComponents(bucket)) {
        // TODO maybe use batch indexing
        index(bucket, component);
      }
    }
  }

  @Override
  protected void doConfigure() throws Exception {
    configureIndex();
  }

  private void configureIndex() {
    if (!client.get().admin().indices().prepareExists(getRepository().getName()).execute().actionGet().isExists()) {
      URL resource = Resources.getResource(SearchFacetImpl.class, "es-mapping.json");
      try {
        client.get().admin().indices().prepareCreate(getRepository().getName())
            .addMapping(TYPE, Resources.toString(resource, Charsets.UTF_8))
            .execute()
            .actionGet();
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  private void deleteIndex() {
    if (client.get().admin().indices().prepareExists(getRepository().getName()).execute().actionGet().isExists()) {
      client.get().admin().indices().prepareDelete(getRepository().getName()).execute().actionGet();
    }
  }

  private void index(final OrientVertex bucket, final OrientVertex component) {
    try {
      client.get().prepareIndex(getRepository().getName(), TYPE, component.getId().toString())
          .setSource(extract(bucket, component))
          .execute();
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private String extract(final OrientVertex bucket, final OrientVertex component) throws Exception {
    Map<String, Object> doc = Maps.newHashMap();
    doc.put(P_REPOSITORY_NAME, bucket.getProperty(P_REPOSITORY_NAME));
    doc.put(P_FORMAT, component.getProperty(P_FORMAT));
    doc.put(P_GROUP, component.getProperty(P_GROUP));
    doc.put(P_NAME, component.getProperty(P_NAME));
    doc.put(P_ATTRIBUTES, component.getProperty(P_ATTRIBUTES));
    List<Map<String, Object>> assetDocs = Lists.newArrayList();
    for (Vertex vertex : component.getVertices(Direction.IN, E_PART_OF_COMPONENT)) {
      OrientVertex asset = (OrientVertex) vertex;
      Map<String, Object> assetDoc = Maps.newHashMap();
      assetDoc.put(P_ATTRIBUTES, asset.getProperty(P_ATTRIBUTES));
      assetDoc.put(P_CONTENT_TYPE, asset.getProperty(P_CONTENT_TYPE));
      assetDoc.put(P_LAST_UPDATED, asset.getProperty(P_LAST_UPDATED));
      assetDocs.add(assetDoc);
    }
    if (!assetDocs.isEmpty()) {
      doc.put("assets", assetDocs.toArray(new Map[assetDocs.size()]));
    }
    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(doc);
  }

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

}
