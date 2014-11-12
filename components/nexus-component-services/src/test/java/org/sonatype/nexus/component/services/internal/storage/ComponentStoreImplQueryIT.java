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

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.services.model.TestAsset;
import org.sonatype.nexus.component.services.model.TestComponent;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_COMPONENT;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_TYPE;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_PATH;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;
import static org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter.P_DOWNLOAD_COUNT;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.and;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.assetPropertyEquals;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.assetPropertyLike;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.componentPropertyEquals;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.or;

/**
 * Integration tests for {@link ComponentStoreImpl}'s query functionality.
 */
public class ComponentStoreImplQueryIT
    extends ComponentStoreImplITSupport
{
  @Test
  public void initialState() {
    assertThat(adapterRegistry.assetClasses().size(), is(0));
    assertThat(adapterRegistry.componentClasses().size(), is(0));

    assertThat(componentStore.assetClasses().size(), is(0));
    assertThat(componentStore.componentClasses().size(), is(0));
  }

  @Test
  public void registerAdapters() {
    adapterRegistry.registerComponentAdapter(testComponentAdapter);
    adapterRegistry.registerAssetAdapter(testAssetAdapter);

    assertThat(adapterRegistry.assetClasses().size(), is(1));
    assertThat(adapterRegistry.componentClasses().size(), is(1));

    assertThat(componentStore.assetClasses().size(), is(1));
    assertThat(componentStore.componentClasses().size(), is(1));

    assertThat(componentStore.countComponents(TestComponent.class, null), is(0L));
    assertThat(componentStore.countAssets(TestAsset.class, null), is(0L));

    assertThat(componentStore.findComponents(TestComponent.class, null).size(), is(0));
    assertThat(componentStore.findAssets(TestAsset.class, null).size(), is(0));
  }

  @Test
  public void totalAssetCount() {
    addTwoTestComponentsWithTwoAssetsEach();

    assertThat(componentStore.countAssets(Asset.class, null), is(4L));
    assertThat(componentStore.countAssets(TestAsset.class, null), is(4L));
  }

  @Test
  public void totalComponentCount() {
    addTwoTestComponentsWithTwoAssetsEach();

    assertThat(componentStore.countComponents(Component.class, null), is(2L));
    assertThat(componentStore.countComponents(TestComponent.class, null), is(2L));
  }

  @Test
  public void queryComponentsWithNoRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent

    // count should be 2
    assertThat(componentStore.countComponents(TestComponent.class, null), is(2L));

    MetadataQuery query = new MetadataQuery().orderBy(P_ID, true);
    List<TestComponent> results = componentStore.findComponents(TestComponent.class, query);

    // query should return component1 then component2
    assertThat(results.size(), is(2));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
    assertTestComponentsEqual(results.get(1), TEST_COMPONENT_2);
  }

  @Test
  public void queryComponentsWithSimpleComponentRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE id = 'component1'
    MetadataQueryRestriction restriction = componentPropertyEquals(P_ID, TEST_COMPONENT_ID_1);

    // count should be 1
    assertThat(componentStore.countComponents(TestComponent.class, restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<TestComponent> results = componentStore.findComponents(TestComponent.class, query);

    // query should return component1 only
    assertThat(results.size(), is(1));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
  }

  @Test
  public void queryComponentsWithCompoundComponentRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE (id = 'component1' OR id = 'component2')
    MetadataQueryRestriction restriction = or(
        componentPropertyEquals(P_ID, TEST_COMPONENT_ID_1),
        componentPropertyEquals(P_ID, TEST_COMPONENT_ID_2));

    // count should be 2
    assertThat(componentStore.countComponents(TestComponent.class, restriction), is(2L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(P_ID, true);
    List<TestComponent> results = componentStore.findComponents(TestComponent.class, query);

    // query should return component1 then component2
    assertThat(results.size(), is(2));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
    assertTestComponentsEqual(results.get(1), TEST_COMPONENT_2);
  }

  @Test
  public void queryComponentsWithSimpleAssetRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE assets contains ( downloadCount = 1 )
    MetadataQueryRestriction restriction = assetPropertyEquals(P_DOWNLOAD_COUNT, 1);

    // count should be 1
    assertThat(componentStore.countComponents(TestComponent.class, restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<TestComponent> results = componentStore.findComponents(TestComponent.class, query);

    // query should return component1 only
    assertThat(results.size(), is(1));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
  }

  @Test
  public void queryComponentsWithCompoundAssetRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE (assets contains ( downloadCount = 1 ) OR assets contains ( contentType = 'text/plain' ))
    MetadataQueryRestriction restriction = or(
        assetPropertyEquals(P_DOWNLOAD_COUNT, 1),
        assetPropertyEquals(P_CONTENT_TYPE, "text/plain"));

    // count should be 2
    assertThat(componentStore.countComponents(TestComponent.class, restriction), is(2L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(P_ID, false);
    List<TestComponent> results = componentStore.findComponents(TestComponent.class, query);

    // query should return component2 then component1 (since we ordered results DESCending this time)
    assertThat(results.size(), is(2));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_2);
    assertTestComponentsEqual(results.get(1), TEST_COMPONENT_1);
  }

  @Test
  public void queryAssetsWithNoRestrictionOrderByTwoPropertys() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testasset

    // count should be 4
    assertThat(componentStore.countAssets(TestAsset.class, null), is(4L));

    MetadataQuery query = new MetadataQuery()
        .orderBy(P_COMPONENT, true)
        .orderBy(P_DOWNLOAD_COUNT, false);
    List<TestAsset> results = componentStore.findAssets(TestAsset.class, query);

    // query should return all four assets in order of component ascending, then downloadCount descending
    assertThat(results.size(), is(4));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 2);
    checkAsset(results.get(1), TEST_COMPONENT_1.getId(), 1);
    checkAsset(results.get(2), TEST_COMPONENT_2.getId(), 4);
    checkAsset(results.get(3), TEST_COMPONENT_2.getId(), 3);
  }

  @Test
  public void queryAssetsWithSimpleAssetRestriction() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testasset WHERE downloadCount = 1
    MetadataQueryRestriction restriction = assetPropertyEquals(P_DOWNLOAD_COUNT, 1);

    // count should be 1
    assertThat(componentStore.countAssets(TestAsset.class, restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<TestAsset> results = componentStore.findAssets(TestAsset.class, query);

    // query should return component1's first asset only
    assertThat(results.size(), is(1));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
  }

  @Test
  public void queryAssetsWithCompoundAssetRestriction() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testasset WHERE (downloadCount = 1 OR contentType = 'text/plain')
    MetadataQueryRestriction restriction = or(
        assetPropertyEquals(P_DOWNLOAD_COUNT, 1),
        assetPropertyEquals(P_CONTENT_TYPE, "text/plain"));

    // count should be 2
    assertThat(componentStore.countAssets(TestAsset.class, restriction), is(4L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(P_ID, true);
    List<TestAsset> results = componentStore.findAssets(TestAsset.class, query);

    // query should return all four assets in ascending order of assetId
    assertThat(results.size(), is(4));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
    checkAsset(results.get(1), TEST_COMPONENT_1.getId(), 2);
    checkAsset(results.get(2), TEST_COMPONENT_2.getId(), 3);
    checkAsset(results.get(3), TEST_COMPONENT_2.getId(), 4);
  }

  @Test
  public void queryAssetsWithSimpleComponentRestriction() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testasset WHERE component.id = 'component1'
    MetadataQueryRestriction restriction = componentPropertyEquals(P_ID, TEST_COMPONENT_ID_1);

    // count should be 2
    assertThat(componentStore.countAssets(TestAsset.class, restriction), is(2L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(P_DOWNLOAD_COUNT, true);
    List<TestAsset> results = componentStore.findAssets(TestAsset.class, query);

    // query should return component1's assets in ascending order of path
    assertThat(results.size(), is(2));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
    checkAsset(results.get(1), TEST_COMPONENT_1.getId(), 2);
  }

  @Test
  public void queryAssetsWithCompoundComponentAndAssetRestrictionUsingLike() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testasset WHERE (component.id = 'component1' AND contentType LIKE '%plain' AND path = "1")
    MetadataQueryRestriction restriction = and(
        componentPropertyEquals(P_ID, TEST_COMPONENT_ID_1),
        assetPropertyLike(P_CONTENT_TYPE, "%plain"),
        assetPropertyEquals(P_PATH, "1"));

    // count should be 1
    assertThat(componentStore.countAssets(TestAsset.class, restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<TestAsset> results = componentStore.findAssets(TestAsset.class, query);

    // query should return component1's first asset only
    assertThat(results.size(), is(1));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
  }

  @Test
  public void pageAssetsUsingSkipLimit() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(2);

    List<TestAsset> page1 = componentStore.findAssets(TestAsset.class, query);
    assertThat(page1.size(), is(2));

    List<TestAsset> page2 = componentStore.findAssets(TestAsset.class, query.skip(2));
    assertThat(page2.size(), is(2));

    List<TestAsset> page3 = componentStore.findAssets(TestAsset.class, query.skip(4));
    assertThat(page3.size(), is(0));
  }

  @Test
  public void pageAssetsUsingSkipEntityId() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(2);

    List<TestAsset> page1 = componentStore.findAssets(TestAsset.class, query);
    assertThat(page1.size(), is(2));

    query.skipEntityId(page1.get(1).getId());
    List<TestAsset> page2 = componentStore.findAssets(TestAsset.class, query);
    assertThat(page2.size(), is(2));

    query.skipEntityId(page2.get(1).getId());
    List<TestAsset> page3 = componentStore.findAssets(TestAsset.class, query);
    assertThat(page3.size(), is(0));
  }

  @Test
  public void pageComponentsUsingSkipLimit() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(1);

    List<TestComponent> page1 = componentStore.findComponents(TestComponent.class, query);
    assertThat(page1.size(), is(1));

    List<TestComponent> page2 = componentStore.findComponents(TestComponent.class, query.skip(1));
    assertThat(page2.size(), is(1));

    List<TestComponent> page3 = componentStore.findComponents(TestComponent.class, query.skip(2));
    assertThat(page3.size(), is(0));
  }

  @Test
  public void pageComponentsUsingSkipEntityId() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(1);

    List<TestComponent> page1 = componentStore.findComponents(TestComponent.class, query);
    assertThat(page1.size(), is(1));

    query.skipEntityId(page1.get(0).getId());
    List<TestComponent> page2 = componentStore.findComponents(TestComponent.class, query);
    assertThat(page2.size(), is(1));

    query.skipEntityId(page2.get(0).getId());
    List<TestComponent> page3 = componentStore.findComponents(TestComponent.class, query);
    assertThat(page3.size(), is(0));
  }
}
