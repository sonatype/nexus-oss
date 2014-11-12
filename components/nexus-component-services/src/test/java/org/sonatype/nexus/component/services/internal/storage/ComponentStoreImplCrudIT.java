/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.services.internal.storage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.model.Envelope;
import org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter;
import org.sonatype.nexus.component.services.internal.adapter.TestComponentAdapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.component.services.adapter.ComponentAdapter.P_ASSETS;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;
import static org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter.P_DOWNLOAD_COUNT;
import static org.sonatype.nexus.component.services.internal.adapter.TestComponentAdapter.P_STRING;

/**
 * Integration tests for {@link ComponentStoreImpl}'s CRUD functionality.
 */
public class ComponentStoreImplCrudIT
    extends ComponentStoreImplITSupport
{
  //
  // createComponent
  //

  @Test(expected = RuntimeException.class)
  public void createComponentNoAdapter() {
    componentStore.createComponent(testComponent());
  }

  @Test
  public void createComponent() {
    registerTestAdapters();

    Component sourceComponent = testComponent();
    Component storedComponent = componentStore.createComponent(sourceComponent);

    checkComponent(storedComponent, sourceComponent);
  }

  //
  // createAsset
  //

  @Test(expected = RuntimeException.class)
  public void createAssetNoAdapter() {
    adapterRegistry.registerAdapter(testComponentAdapter);
    componentStore.prepareStorage(TestComponentAdapter.CLASS_NAME);

    Component storedComponent = componentStore.createComponent(testComponent());
    Asset asset = testAsset(1);

    componentStore.createAsset(storedComponent.get(P_ID, EntityId.class), asset);
  }

  @Test
  public void createAsset() throws Exception {
    registerTestAdapters();

    Component storedComponent = componentStore.createComponent(testComponent());
    Asset storedAsset = componentStore.createAsset(storedComponent.get(P_ID, EntityId.class), testAsset(1));

    checkAsset(storedAsset, storedComponent.get(P_ID, EntityId.class), 1);
  }

  // createComponentWithAssets

  @Test
  public void createComponentWithAssets() {
    registerTestAdapters();

    Envelope sourceEnvelope = testEnvelope(10);

    Component storedComponent = componentStore.createComponentWithAssets(sourceEnvelope).getComponent();

    checkComponent(storedComponent, sourceEnvelope.getComponent());
    Set<EntityId> assetIds = getAssetIds(storedComponent);

    assertThat(assetIds.size(), is(10));

    for (EntityId assetId: assetIds) {
      Asset storedAsset = componentStore.readAsset(TestAssetAdapter.CLASS_NAME, assetId);
      long downloadCount = storedAsset.get(P_DOWNLOAD_COUNT, Long.class);
      checkAsset(storedAsset, storedComponent.get(P_ID, EntityId.class), (int) downloadCount);
    }
  }

  // createComponentsWithAssets

  @Test
  public void createComponentsWithAssetsOneTransaction() {
    registerTestAdapters();

    Set<Envelope> sourceEnvelopes = Sets.newHashSet();
    for (int i = 0; i < 10; i++) {
      sourceEnvelopes.add(testEnvelope(2));
    }

    final Set<EntityId> resultIds = Sets.newHashSet();
    boolean finished = componentStore.createComponentsWithAssets(sourceEnvelopes, new Predicate<List<EntityId>>() {
      @Override
      public boolean apply(final List<EntityId> entityIds) {
        assertThat(entityIds.size(), is(10));
        resultIds.addAll(entityIds);
        return true;
      }
    }, 0);

    assertThat(finished, is(true));
    assertThat(resultIds.size(), is(10));
  }

  @Test
  public void createComponentsWithAssetsTwoTransactions() {
    registerTestAdapters();

    Set<Envelope> sourceEnvelopes = Sets.newHashSet();
    for (int i = 0; i < 10; i++) {
      sourceEnvelopes.add(testEnvelope(2));
    }

    final Set<EntityId> resultIds = Sets.newHashSet();
    boolean finished = componentStore.createComponentsWithAssets(sourceEnvelopes, new Predicate<List<EntityId>>() {
      @Override
      public boolean apply(final List<EntityId> entityIds) {
        assertThat(entityIds.size(), is(5));
        resultIds.addAll(entityIds);
        return true;
      }
    }, 5);

    assertThat(finished, is(true));
    assertThat(resultIds.size(), is(10));
  }

  // read

  @Test(expected = IllegalStateException.class)
  public void readNonExistingComponent() {
    registerTestAdapters();

    componentStore.readComponent(TestComponentAdapter.CLASS_NAME, new EntityId("bogusId"));
  }

  @Test(expected = IllegalStateException.class)
  public void readNonExistingAsset() {
    registerTestAdapters();

    componentStore.readAsset(TestAssetAdapter.CLASS_NAME, new EntityId("bogusId"));
  }

  @Test
  public void readComponent() {
    registerTestAdapters();

    Component sourceComponent = testComponent(TEST_STRING_1, true);
    EntityId componentId = componentStore.createComponent(sourceComponent).get(P_ID, EntityId.class);

    Component storedComponent = componentStore.readComponent(TestComponentAdapter.CLASS_NAME, componentId);

    checkComponent(storedComponent, sourceComponent);
  }

  @Test
  public void readAsset() {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).get(P_ID, EntityId.class);
    EntityId assetId = componentStore.createAsset(componentId, testAsset(1)).get(P_ID, EntityId.class);

    Asset storedAsset = componentStore.readAsset(TestAssetAdapter.CLASS_NAME, assetId);

    checkAsset(storedAsset, componentId, 1);
  }

  // readWithAssets

  @Test
  public void readComponentWithAssets() {
    registerTestAdapters();

    Component storedComponent = componentStore.createComponentWithAssets(testEnvelope(10)).getComponent();

    Envelope storedEnvelope = componentStore.readComponentWithAssets(TestComponentAdapter.CLASS_NAME,
        storedComponent.get(P_ID, EntityId.class));

    checkComponent(storedEnvelope.getComponent(), storedComponent);
    assertThat(getAssetIds(storedEnvelope.getComponent()), is(getAssetIds(storedComponent)));
    assertThat(Iterables.size(storedEnvelope.getAssets()), is(10));
    for (Asset storedAsset: storedEnvelope.getAssets()) {
      long downloadCount = storedAsset.get(P_DOWNLOAD_COUNT, Long.class);
      checkAsset(storedAsset, storedComponent.get(P_ID, EntityId.class), (int) downloadCount);
    }
  }

  // update

  @Test(expected = IllegalStateException.class)
  public void updateNonExistingComponent() {
    registerTestAdapters();

    componentStore.updateComponent(new EntityId("bogusId"), testComponent());
  }

  @Test(expected = IllegalStateException.class)
  public void updateNonExistingAsset() {
    registerTestAdapters();

    componentStore.updateAsset(new EntityId("bogusId"), testAsset(1));
  }

  @Test
  public void updateComponent() {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).get(P_ID, EntityId.class);

    Component sourceComponent = testComponent();
    sourceComponent.put(P_STRING, TEST_STRING_2);
    Component updatedComponent = componentStore.updateComponent(componentId, sourceComponent);

    assertThat(updatedComponent.get(P_STRING, String.class), is(TEST_STRING_2));
    checkComponent(updatedComponent, sourceComponent);
  }

  @Test
  public void updateAsset() throws Exception {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).get(P_ID, EntityId.class);
    EntityId assetId = componentStore.createAsset(componentId, testAsset(1)).get(P_ID, EntityId.class);

    Asset sourceAsset = testAsset(2);
    Thread.sleep(1000); // ensure different modified date
    Asset updatedAsset = componentStore.updateAsset(assetId, sourceAsset);

    checkAsset(updatedAsset, componentId, 2, false);
  }

  @Test
  public void updateAssetWithoutModifyingStream() throws IOException {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).get(P_ID, EntityId.class);
    EntityId assetId = componentStore.createAsset(componentId, testAsset(1)).get(P_ID, EntityId.class);

    Asset sourceAsset = testAsset(2);
    sourceAsset.setStreamSupplier(null);
    Asset updatedAsset = componentStore.updateAsset(assetId, sourceAsset);

    assertThat(updatedAsset.get(P_DOWNLOAD_COUNT, Long.class), is(2L));
    assertThat(IOUtils.toString(updatedAsset.openStream()), is(IOUtils.toString(testAsset(1).openStream())));
  }

  // delete

  @Test
  public void deleteNonExistingComponent() {
    registerTestAdapters();

    assertThat(componentStore.delete(TestComponentAdapter.CLASS_NAME, new EntityId("bogusId")), is(false));
  }

  @Test
  public void deleteNonExistingAsset() {
    registerTestAdapters();

    assertThat(componentStore.delete(TestAssetAdapter.CLASS_NAME, new EntityId("bogusId")), is(false));
  }

  @Test
  public void deleteComponent() {
    registerTestAdapters();

    Envelope sourceEnvelope = testEnvelope(1);
    Component storedComponent = componentStore.createComponentWithAssets(sourceEnvelope).getComponent();
    EntityId assetId = getAssetIds(storedComponent).iterator().next();

    assertThat(componentStore.delete(TestComponentAdapter.CLASS_NAME, storedComponent.get(P_ID, EntityId.class)), is(true));
    assertThat(componentStore.delete(TestComponentAdapter.CLASS_NAME, storedComponent.get(P_ID, EntityId.class)), is(false));
    assertThat(componentStore.delete(TestAssetAdapter.CLASS_NAME, assetId), is(false));
  }

  @Test
  public void deleteAsset() {
    registerTestAdapters();

    Envelope sourceEnvelope = testEnvelope(1);
    Component storedComponent = componentStore.createComponentWithAssets(sourceEnvelope).getComponent();
    EntityId assetId = getAssetIds(storedComponent).iterator().next();

    assertThat(componentStore.delete(TestAssetAdapter.CLASS_NAME, assetId), is(true));
    Component updatedComponent = componentStore.readComponent(TestComponentAdapter.CLASS_NAME, storedComponent.get(P_ID,
        EntityId.class));
    assertThat(getAssetIds(updatedComponent).size(), is(0));
  }

  @SuppressWarnings("unchecked")
  private Set<EntityId> getAssetIds(Component component) {
    return component.get(P_ASSETS, Set.class);
  }
}
