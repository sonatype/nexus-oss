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
package org.sonatype.nexus.views.rawbinaries.internal.storage;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.model.Envelope;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.storage.ComponentStore;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.views.rawbinaries.internal.storage.adapter.RawBinaryAssetAdapter;
import org.sonatype.nexus.views.rawbinaries.internal.storage.adapter.RawBinaryComponentAdapter;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;

import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_COMPONENT;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_TYPE;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_PATH;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.assetPropertyEquals;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.assetPropertyLike;

/**
 * Default implementation of {@link RawBinaryStore}.
 */
@Named
@Singleton
public class RawBinaryStoreImpl
  implements RawBinaryStore, EventSubscriber
{
  private final ComponentStore componentStore;

  @Inject
  public RawBinaryStoreImpl(ComponentStore componentStore) {
    this.componentStore = componentStore;
  }

  @Subscribe
  @SuppressWarnings("unchecked")
  public void on(NexusInitializedEvent event) throws Exception {
    componentStore.prepareStorage(RawBinaryComponentAdapter.CLASS_NAME, RawBinaryAssetAdapter.CLASS_NAME);
  }

  @Override
  public List<Asset> getForPath(final String prefix) {
    return findAssets(assetPropertyLike(P_PATH, prefix + '%'));
  }

  @Nullable
  @Override
  public Asset create(final String path, final String contentType, final InputStream inputStream) {
    Asset asset = new Asset(RawBinaryAssetAdapter.CLASS_NAME);
    asset.put(P_PATH, path);
    asset.put(P_CONTENT_TYPE, contentType);
    asset.setStream(inputStream);

    Component component = new Component(RawBinaryComponentAdapter.CLASS_NAME);

    Envelope storedEnvelope = componentStore.createComponentWithAssets(new Envelope(component, asset));

    return Iterables.getFirst(storedEnvelope.getAssets(), null);
  }

  @Override
  public boolean delete(final String path) {
    Entity asset = findFirst(assetPropertyEquals(P_PATH, path));
    if (asset == null) {
      return false;
    }
    componentStore.delete(RawBinaryComponentAdapter.CLASS_NAME, asset.get(P_COMPONENT, EntityId.class));
    return true;
  }

  private List<Asset> findAssets(MetadataQueryRestriction restriction) {
    return componentStore.findAssets(RawBinaryAssetAdapter.CLASS_NAME, new MetadataQuery().restriction(restriction));
  }

  @Nullable
  private Asset findFirst(MetadataQueryRestriction restriction) {
    List<Asset> results = findAssets(restriction);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }
}
