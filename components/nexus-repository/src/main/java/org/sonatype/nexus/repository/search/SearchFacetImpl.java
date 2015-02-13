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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.StorageFacet;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.elasticsearch.client.Client;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;
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

  private final List<IndexSettingsContributor> indexSettingsContributors;

  @Inject
  public SearchFacetImpl(final Provider<Client> client,
                         final List<IndexSettingsContributor> indexSettingsContributors)
  {
    this.client = checkNotNull(client);
    this.indexSettingsContributors = checkNotNull(indexSettingsContributors);
  }

  @Override
  @Guarded(by = STARTED)
  public void put(final SearchableComponent searchable) {
    checkNotNull(searchable);
    try {
      Map<String, Object> additional = Maps.newHashMap();
      additional.put(P_REPOSITORY_NAME, getRepository().getName());
      String json = JsonUtils.merge(searchable.toJson(), JsonUtils.from(additional));
      client.get().prepareIndex(getRepository().getName(), TYPE, searchable.getId()).setSource(json).execute();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void delete(final String id) {
    checkNotNull(id);
    client.get().prepareDelete(getRepository().getName(), TYPE, id).execute();
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
            source = JsonUtils.merge(source, contributed);
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

  @Override
  protected void doDelete() {
    if (client.get().admin().indices().prepareExists(getRepository().getName()).execute().actionGet().isExists()) {
      client.get().admin().indices().prepareDelete(getRepository().getName()).execute().actionGet();
    }
  }

}
