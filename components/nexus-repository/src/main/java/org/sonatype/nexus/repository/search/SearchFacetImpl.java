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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.elasticsearch.client.Client;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

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

  private final List<IndexSettingsContributor> indexSettingsContributors;

  private final Map<String, IndexDataProducer> indexDataProducers;

  @Inject
  public SearchFacetImpl(final Provider<Client> client,
                         final List<IndexSettingsContributor> indexSettingsContributors,
                         final Map<String, IndexDataProducer> indexDataProducers)
  {
    this.client = checkNotNull(client);
    this.indexSettingsContributors = checkNotNull(indexSettingsContributors);
    this.indexDataProducers = checkNotNull(indexDataProducers);
  }

  @Override
  @Guarded(by = STARTED)
  public void index(final OrientVertex component) {
    checkNotNull(component);
    String format = getRepository().getFormat().getValue();
    IndexDataProducer indexDataProducer = indexDataProducers.get(format);
    if (indexDataProducer == null) {
      indexDataProducer = indexDataProducers.get("default");
    }
    checkState(indexDataProducer != null, "Could not find an index data producer for format: {}", format);
    client.get().prepareIndex(getRepository().getName(), TYPE, component.getId().toString())
        .setSource(indexDataProducer.getIndexData(getRepository(), component))
        .execute();
  }

  @Override
  @Guarded(by = STARTED)
  public void deindex(final OrientVertex component) {
    checkNotNull(component);
    client.get().prepareDelete(getRepository().getName(), TYPE, component.getId().toString()).execute();
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
        index(component);
      }
    }
  }

  @Override
  protected void doConfigure() throws Exception {
    configureIndex();
  }

  private void configureIndex() {
    // TODO we should calculate the checksum of index settings and compare it with a value stored in index _meta tags
    // in case that they not match (settings changed) we should drop the index, recreate it and re-index all components
    if (!client.get().admin().indices().prepareExists(getRepository().getName()).execute().actionGet().isExists()) {
      try {
        String source = Resources.toString(Resources.getResource(SearchFacetImpl.class, "es-mapping.json"), UTF_8);
        for (IndexSettingsContributor contributor : indexSettingsContributors) {
          String contributed = contributor.getIndexSettings(getRepository());
          if (contributed != null) {
            source = merge(source, contributed);
          }
        }
        client.get().admin().indices().prepareCreate(getRepository().getName())
            .setSource(source)
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

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

  private static String merge(final String... jsons) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode merged = mapper.readTree("{}");
    for (String json : jsons) {
      merged = merge(merged, mapper.readTree(json));
    }
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(merged);
  }

  private static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {

      String fieldName = fieldNames.next();
      JsonNode jsonNode = mainNode.get(fieldName);
      // if field exists and is an embedded object
      if (jsonNode != null && jsonNode.isObject()) {
        merge(jsonNode, updateNode.get(fieldName));
      }
      else {
        if (mainNode instanceof ObjectNode) {
          // Overwrite field
          JsonNode value = updateNode.get(fieldName);
          ((ObjectNode) mainNode).put(fieldName, value);
        }
      }

    }

    return mainNode;
  }

}
