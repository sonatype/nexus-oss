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

import org.sonatype.nexus.component.model.ComponentEnvelope;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.services.model.TestAsset;
import org.sonatype.nexus.component.services.model.TestComponent;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

  @Test(expected = RuntimeException.class)
  public void createComponentRequiredPropertyUnspecified() {
    registerTestAdapters();

    componentStore.createComponent(new TestComponent());
  }

  @Test
  public void createComponent() {
    registerTestAdapters();

    TestComponent sourceComponent = testComponent();
    TestComponent storedComponent = componentStore.createComponent(sourceComponent);

    checkComponent(storedComponent, sourceComponent);
  }

  //
  // createAsset
  //

  @Test(expected = RuntimeException.class)
  public void createAssetNoAdapter() {
    adapterRegistry.registerComponentAdapter(testComponentAdapter);

    TestComponent storedComponent = componentStore.createComponent(testComponent());
    TestAsset asset = testAsset(1);

    componentStore.createAsset(storedComponent.getId(), asset);
  }

  @Test
  public void createAsset() throws Exception {
    registerTestAdapters();

    TestComponent storedComponent = componentStore.createComponent(testComponent());
    TestAsset storedAsset = componentStore.createAsset(storedComponent.getId(), testAsset(1));

    checkAsset(storedAsset, storedComponent.getId(), 1);
  }

  // createComponentWithAssets

  @Test
  public void createComponentWithAssets() {
    registerTestAdapters();

    ComponentEnvelope<TestComponent, TestAsset> sourceEnvelope = testEnvelope(10);

    TestComponent storedComponent = componentStore.createComponentWithAssets(sourceEnvelope);

    checkComponent(storedComponent, sourceEnvelope.getComponent());
    Set<EntityId> assetIds = storedComponent.getAssetIds();

    assertThat(assetIds.size(), is(10));

    for (EntityId assetId: assetIds) {
      TestAsset storedAsset = componentStore.readAsset(TestAsset.class, assetId);
      checkAsset(storedAsset, storedComponent.getId(), (int) storedAsset.getDownloadCount());
    }
  }

  // createComponentsWithAssets

  @Test
  public void createComponentsWithAssetsOneTransaction() {
    registerTestAdapters();

    Set<ComponentEnvelope<TestComponent, TestAsset>> sourceEnvelopes = Sets.newHashSet();
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

    Set<ComponentEnvelope<TestComponent, TestAsset>> sourceEnvelopes = Sets.newHashSet();
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

    componentStore.readComponent(TestComponent.class, new EntityId("bogusId"));
  }

  @Test(expected = IllegalStateException.class)
  public void readNonExistingAsset() {
    registerTestAdapters();

    componentStore.readAsset(TestAsset.class, new EntityId("bogusId"));
  }

  @Test
  public void readComponent() {
    registerTestAdapters();

    TestComponent sourceComponent = testComponent(TEST_STRING_1, true);
    EntityId componentId = componentStore.createComponent(sourceComponent).getId();

    TestComponent storedComponent = componentStore.readComponent(TestComponent.class, componentId);

    checkComponent(storedComponent, sourceComponent);
  }

  @Test
  public void readAsset() {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).getId();
    EntityId assetId = componentStore.createAsset(componentId, testAsset(1)).getId();

    TestAsset storedAsset = componentStore.readAsset(TestAsset.class, assetId);

    checkAsset(storedAsset, componentId, 1);
  }

  // readComponentWithAssets

  @Test
  public void readComponentWithAssets() {
    registerTestAdapters();

    TestComponent storedComponent = componentStore.createComponentWithAssets(testEnvelope(10));

    ComponentEnvelope<TestComponent, TestAsset> storedEnvelope =
        componentStore.readComponentWithAssets(TestComponent.class, TestAsset.class, storedComponent.getId());

    checkComponent(storedEnvelope.getComponent(), storedComponent);
    assertThat(storedEnvelope.getComponent().getAssetIds(), is(storedComponent.getAssetIds()));
    assertThat(Iterables.size(storedEnvelope.getAssets()), is(10));
    for (TestAsset storedAsset: storedEnvelope.getAssets()) {
      checkAsset(storedAsset, storedComponent.getId(), (int) storedAsset.getDownloadCount());
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

    EntityId componentId = componentStore.createComponent(testComponent()).getId();

    TestComponent sourceComponent = testComponent();
    sourceComponent.setStringProp(TEST_STRING_2);
    TestComponent updatedComponent = componentStore.updateComponent(componentId, sourceComponent);

    assertThat(updatedComponent.getStringProp(), is(TEST_STRING_2));
    checkComponent(updatedComponent, sourceComponent);
  }

  @Test
  public void updateAsset() throws Exception {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).getId();
    EntityId assetId = componentStore.createAsset(componentId, testAsset(1)).getId();

    TestAsset sourceAsset = testAsset(2);
    Thread.sleep(1000); // ensure different modified date
    TestAsset updatedAsset = componentStore.updateAsset(assetId, sourceAsset);

    checkAsset(updatedAsset, componentId, 2, false);
  }

  @Test
  public void updateAssetWithoutModifyingStream() throws IOException {
    registerTestAdapters();

    EntityId componentId = componentStore.createComponent(testComponent()).getId();
    EntityId assetId = componentStore.createAsset(componentId, testAsset(1)).getId();

    TestAsset sourceAsset = testAsset(2);
    sourceAsset.setStreamSupplier(null);
    TestAsset updatedAsset = componentStore.updateAsset(assetId, sourceAsset);

    assertThat(updatedAsset.getDownloadCount(), is(2L));
    assertThat(IOUtils.toString(updatedAsset.openStream()), is(IOUtils.toString(testAsset(1).openStream())));
  }

  // delete

  @Test
  public void deleteNonExistingComponent() {
    registerTestAdapters();

    assertThat(componentStore.deleteComponent(TestComponent.class, new EntityId("bogusId")), is(false));
  }

  @Test
  public void deleteNonExistingAsset() {
    registerTestAdapters();

    assertThat(componentStore.deleteAsset(TestAsset.class, new EntityId("bogusId")), is(false));
  }

  @Test
  public void deleteComponent() {
    registerTestAdapters();

    ComponentEnvelope<TestComponent, TestAsset> sourceEnvelope = testEnvelope(1);
    TestComponent storedComponent = componentStore.createComponentWithAssets(sourceEnvelope);
    EntityId assetId = storedComponent.getAssetIds().iterator().next();

    assertThat(componentStore.deleteComponent(TestComponent.class, storedComponent.getId()), is(true));
    assertThat(componentStore.deleteComponent(TestComponent.class, storedComponent.getId()), is(false));
    assertThat(componentStore.deleteAsset(TestAsset.class, assetId), is(false));
  }

  @Test
  public void deleteAsset() {
    registerTestAdapters();

    ComponentEnvelope<TestComponent, TestAsset> sourceEnvelope = testEnvelope(1);
    TestComponent storedComponent = componentStore.createComponentWithAssets(sourceEnvelope);
    EntityId assetId = storedComponent.getAssetIds().iterator().next();

    assertThat(componentStore.deleteAsset(TestAsset.class, assetId), is(true));
    TestComponent updatedComponent = componentStore.readComponent(TestComponent.class, storedComponent.getId());
    assertThat(updatedComponent.getAssetIds().size(), is(0));
  }
}
