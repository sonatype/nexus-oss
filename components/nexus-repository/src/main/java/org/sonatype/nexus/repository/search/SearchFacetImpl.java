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
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.elasticsearch.client.Client;

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

  private final Map<String, SourceProducer> sourceProducers;

  @Inject
  public SearchFacetImpl(final Provider<Client> client,
                         final Map<String, SourceProducer> sourceProducers)
  {
    this.client = checkNotNull(client);
    this.sourceProducers = checkNotNull(sourceProducers);
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
    String format = getRepository().getFormat().getValue();
    SourceProducer sourceProducer = sourceProducers.get(format);
    if (sourceProducer == null) {
      sourceProducer = sourceProducers.get("default");
    }
    checkState(sourceProducer != null, "Could not find a source producer for format: {}", format);
    client.get().prepareIndex(getRepository().getName(), TYPE, component.getId().toString())
        .setSource(sourceProducer.getSource(bucket, component))
        .execute();
  }

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

}
