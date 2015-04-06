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

package org.sonatype.nexus.repository.storage;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.orient.PersistentDatabaseInstanceRule;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.search.ComponentMetadataFactory;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_PATH;
import static org.sonatype.nexus.repository.storage.StorageFacet.V_ASSET;

/**
 * Integration tests for {@link StorageFacetImpl}.
 */
public class StorageFacetImplIT
    extends TestSupport
{
  @Rule
  public PersistentDatabaseInstanceRule database = new PersistentDatabaseInstanceRule("test");

  protected StorageFacetImpl underTest;

  protected Repository testRepository1 = mock(Repository.class);

  protected Repository testRepository2 = mock(Repository.class);

  protected TestFormat testFormat = new TestFormat();

  private class TestFormat extends Format {
    public TestFormat() {
      super("test");
    }
  }

  @Before
  public void setUp() throws Exception {
    BlobStoreManager mockBlobStoreManager = mock(BlobStoreManager.class);
    when(mockBlobStoreManager.get(anyString())).thenReturn(mock(BlobStore.class));
    underTest = new StorageFacetImpl(
        mockBlobStoreManager,
        Providers.of(database.getInstance()),
        mock(ComponentMetadataFactory.class)
    );
    underTest.installDependencies(mock(EventBus.class));

    NestedAttributesMap storageAttributes = new NestedAttributesMap(
        "storage", ImmutableMap.of("blobStoreName", (Object) "default"));
    Configuration testConfiguration = mock(Configuration.class);
    when(testConfiguration.attributes(anyString())).thenReturn(storageAttributes);

    when(testRepository1.getName()).thenReturn("test-repository-1");
    when(testRepository1.getConfiguration()).thenReturn(testConfiguration);
    when(testRepository1.facet(SearchFacet.class)).thenReturn(mock(SearchFacet.class));
    when(testRepository2.getName()).thenReturn("test-repository-2");
    when(testRepository2.getConfiguration()).thenReturn(testConfiguration);
    when(testRepository2.facet(SearchFacet.class)).thenReturn(mock(SearchFacet.class));
    underTest.init(testRepository1);
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    underTest.stop();
  }

  @Test
  public void initialState() {
    try (StorageTx tx = underTest.openTx()) {
      // We should have one bucket, which was auto-created for the repository during initialization
      checkSize(tx.browseBuckets(), 1);
    }
  }

  @Test
  public void startWithEmptyAttributes() {
    try (StorageTx tx = underTest.openTx()) {
      Asset asset = tx.createAsset(tx.getBucket(), testFormat);
      Component component = tx.createComponent(tx.getBucket(), testFormat);

      Map<String, Object> assetAttributes = component.vertex().getProperty(P_ATTRIBUTES);
      assertThat(assetAttributes, is(notNullValue()));
      assertThat(assetAttributes.isEmpty(), is(true));

      Map<String, Object> componentAttributes = component.vertex().getProperty(P_ATTRIBUTES);
      assertThat(componentAttributes, is(notNullValue()));
      assertThat(componentAttributes.isEmpty(), is(true));
    }
  }

  @Test
  public void getAndSetAttributes() {
    ORID vertexId;
    try (StorageTx tx = underTest.openTx()) {
      Asset asset = tx.createAsset(tx.getBucket(), testFormat);
      NestedAttributesMap map = asset.attributes();

      assertThat(map.isEmpty(), is(true));

      map.child("bag1").set("foo", "bar");
      map.child("bag2").set("baz", "qux");

      assertThat(map.isEmpty(), is(false));

      tx.commit();
      vertexId = asset.id();
    }

    try (StorageTx tx = underTest.openTx()) {
      NestedAttributesMap map = tx.findAsset(vertexId, tx.getBucket()).attributes();

      assertThat(map.size(), is(2));
      assertThat(map.child("bag1").size(), is(1));
      assertThat((String) map.child("bag1").get("foo"), is("bar"));
      assertThat(map.child("bag2").size(), is(1));
      assertThat((String) map.child("bag2").get("baz"), is("qux"));
    }
  }

  @Test
  public void findAssets() throws Exception {
    // Setup: add an asset in both repositories
    try (StorageTx tx = underTest.openTx()) {
      Asset asset1 = tx.createAsset(tx.getBucket(), testFormat);
      asset1.set("name", "asset1");
      asset1.set("number", 42);
      tx.commit();
    }

    underTest.init(testRepository2);
    try (StorageTx tx = underTest.openTx()) {
      Asset asset2 = tx.createAsset(tx.getBucket(), testFormat);
      asset2.set("name", "asset2");
      asset2.set("number", 42);
      tx.commit();
    }

    // Queries
    try (StorageTx tx = underTest.openTx()) {

      // Find assets with name = "asset1"

      // ..in testRepository1, should yield 1 match
      checkSize(tx.findAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"),
          ImmutableSet.of(testRepository1), null), 1);
      assertThat(tx.countAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"),
          ImmutableSet.of(testRepository1), null), is(1L));
      // ...in testRepository2, should yield 0 matches
      checkSize(tx.findAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"),
          ImmutableSet.of(testRepository2), null), 0);
      assertThat(tx.countAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"),
          ImmutableSet.of(testRepository2), null), is(0L));
      // ..in testRepository1 or testRepository2, should yeild 1 match
      checkSize(tx.findAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"),
          ImmutableSet.of(testRepository1, testRepository2), null), 1);
      assertThat(tx.countAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"),
          ImmutableSet.of(testRepository1, testRepository2), null), is(1L));
      // ..in any repository should yeild 2 matches
      checkSize(tx.findAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"), null, null), 1);
      assertThat(tx.countAssets("name = :name", ImmutableMap.of("name", (Object) "asset1"), null, null), is(1L));

      // Find assets with number = 42

      // ..in testRepository1, should yield 1 match
      checkSize(tx.findAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository1), null), 1);
      assertThat(tx.countAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository1), null), is(1L));
      // ..in testRepository2, should yield 1 match
      checkSize(tx.findAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository2), null), 1);
      assertThat(tx.countAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository2), null), is(1L));
      // ..in testRepository1 or testRepository2, should yield 2 matches
      checkSize(tx.findAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository1, testRepository2), null), 2);
      assertThat(tx.countAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository1, testRepository2), null), is(2L));
      // ..in any repository, should yield 2 matches
      checkSize(tx.findAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository1, testRepository2), null), 2);
      assertThat(tx.countAssets("number = :number", ImmutableMap.of("number", (Object) 42),
          ImmutableSet.of(testRepository1, testRepository2), null), is(2L));

      // Find assets in any repository with name = "foo" or number = 42
      String whereClause = "name = :name or number = :number";
      Map<String, Object> parameters = ImmutableMap.of("name", (Object) "foo", "number", 42);

      // ..in ascending order by name with limit 1, should return asset1
      String suffix = "order by name limit 1";
      List<Asset> results = Lists.newArrayList(tx.findAssets(whereClause, parameters, null, suffix));
      checkSize(results, 1);
      assertThat((String) results.get(0).get("name"), is("asset1"));

      // ..in descending order by name with limit 1, should return asset2
      suffix = "order by name desc limit 1";
      results = Lists.newArrayList(tx.findAssets(whereClause, parameters, null, suffix));
      checkSize(results, 1);
      assertThat((String) results.get(0).get("name"), is("asset2"));
    }
  }

  @Test
  public void mapOfMaps() {
    Map<String, String> bag1 = ImmutableMap.of("foo", "bar");
    Map<String, String> bag2 = ImmutableMap.of("baz", "qux");
    Map<String, Map<String, String>> inputMap = ImmutableMap.of("bag1", bag1, "bag2", bag2);

    // Transaction 1:
    // Create a new asset with property "attributes" that's a map of maps (stored as an embeddedmap)
    Object vertexId;
    try (StorageTx tx = underTest.openTx()) {
      Bucket bucket = tx.getBucket();
      Asset asset = tx.createAsset(bucket, testFormat);
      asset.set("attributes", inputMap);
      tx.commit();
      vertexId = asset.vertex().getIdentity();
    }

    // Transaction 2:
    // Get the asset and make sure it contains what we expect
    try (StorageTx tx = underTest.openTx()) {
      Bucket bucket = tx.getBucket();
      Asset asset = tx.findAsset((ORID) vertexId, bucket);
      assert asset != null;

      Map<String, Map<String, String>> outputMap = asset.vertex().getProperty("attributes");

      assertThat(outputMap.keySet().size(), is(2));

      Map<String, String> outputBag1 = outputMap.get("bag1");
      assertNotNull(outputBag1);
      assertThat(outputBag1.keySet().size(), is(1));
      assertThat(outputBag1.get("foo"), is("bar"));

      Map<String, String> outputBag2 = outputMap.get("bag2");
      assertNotNull(outputBag2);
      assertThat(outputBag2.keySet().size(), is(1));
      assertThat(outputBag2.get("baz"), is("qux"));
    }

    // Transaction 3:
    // Make sure we can use dot notation to query for the asset by some aspect of the attributes
    try (StorageTx tx = underTest.openTx()) {
      Map<String, String> parameters = ImmutableMap.of("fooValue", "bar");
      String query = String.format("select from %s where attributes.bag1.foo = :fooValue", V_ASSET);

      Iterable<OrientVertex> vertices = tx.getGraphTx().command(new OCommandSQL(query)).execute(parameters);
      List<OrientVertex> list = Lists.newArrayList(vertices);

      assertThat(list.size(), is(1));
      assertThat(list.get(0).getId(), is(vertexId));
    }
  }

  @Test
  public void roundTripTest() {
    try (StorageTx tx = underTest.openTx()) {
      // Verify initial state with browse
      Bucket bucket = tx.getBucket();

      checkSize(tx.browseBuckets(), 1);
      checkSize(tx.browseAssets(bucket), 0);
      checkSize(tx.browseComponents(bucket), 0);

      // Create an asset and component and verify state with browse and find
      Asset asset = tx.createAsset(bucket, testFormat);
      asset.set(P_PATH, "path");
      Component component = tx.createComponent(bucket, testFormat);
      component.set("foo", "bar");
      tx.commit();

      checkSize(tx.browseAssets(bucket), 1);
      checkSize(tx.browseComponents(bucket), 1);

      assertNotNull(tx.findAsset(asset.id(), bucket));
      assertNotNull(tx.findComponent(component.id(), bucket));

      assertNull(tx.findAssetWithProperty(P_PATH, "nomatch", bucket));
      assertNotNull(tx.findAssetWithProperty(P_PATH, "path", bucket));

      assertNull(tx.findComponentWithProperty("foo", "nomatch", bucket));
      assertNotNull(tx.findComponentWithProperty("foo", "bar", bucket));

      // Delete both and make sure browse and find behave as expected
      tx.deleteAsset(asset);
      tx.deleteComponent(component);

      checkSize(tx.browseAssets(bucket), 0);
      checkSize(tx.browseComponents(bucket), 0);
      assertNull(tx.findAsset(asset.id(), bucket));
      assertNull(tx.findComponent(component.id(), bucket));

      // NOTE: It doesn't matter for this test, but you should commit when finished with one or more writes
      //       If you don't, your changes will be automatically rolled back.
      tx.commit();
    }
  }

  @Test
  public void concurrentTransactionWithoutConflictTest() throws Exception {
    doConcurrentTransactionTest(false);
  }

  @Test
  public void concurrentTransactionWithConflictTest() throws Exception {
    doConcurrentTransactionTest(true);
  }

  private void doConcurrentTransactionTest(boolean simulateConflict) throws Exception {
    // setup:
    //   main thread: create a new asset and commit it.
    // test:
    //   main thread: start new transaction, and if simulating a conflict, read the asset
    //   aux thread: start new transaction, modify asset, and commit
    //   main thread: if not simulating a conflict, read the asset. then modify the asset, then commit it
    // expectation:
    //   if simulating a conflict: commit on main thread fails with OConcurrentModificationException
    //   if not simulating a conflict: modification made in main thread is persisted after the modification on aux

    // setup
    final ORID assetId;
    int firstVersion;
    try (StorageTx tx = underTest.openTx()) {
      Bucket bucket = tx.getBucket();
      Asset asset = tx.createAsset(bucket, testFormat);
      assetId = asset.id();
      tx.commit();
      firstVersion = asset.get("@version", Integer.class);
    }

    // test
    // 1. start a tx (mainTx) in the main thread
    try (StorageTx mainTx = underTest.openTx()) {
      Bucket bucket = mainTx.getBucket();
      Asset asset = null;

      if (simulateConflict) {
        // cause a conflict to occur later by reading the asset before the other tx starts
        // (this causes the MVCC version comparison at commit-time to fail)
        asset = checkNotNull(mainTx.findAsset(assetId, bucket));
      }

      // 2. modify and commit the asset in a separate tx (auxTx) in another thread
      Thread auxThread = new Thread() {
        @Override
        public void run() {
          try (StorageTx auxTx = underTest.openTx()) {
            Bucket bucket = auxTx.getBucket();
            Asset asset = checkNotNull(auxTx.findAsset(assetId, bucket));
            asset.set("foo", "firstValue");
            auxTx.commit();
          }
        }
      };
      auxThread.start();
      auxThread.join();

      // 3. modify and commit the asset in mainTx, in the main thread
      if (!simulateConflict) {
        // only read the asset we propose to change *after* the other transaction completes
        asset = checkNotNull(mainTx.findAsset(assetId, bucket));
      }
      asset.set("foo", "secondValue");
      mainTx.commit(); // if we're simulating a conflict, this call should throw OConcurrentModificationException
      assertThat(simulateConflict, is(false));
    }
    catch (OConcurrentModificationException e) {
      assertThat(simulateConflict, is(true));
      return;
    }

    // not simulating a conflict; verify the expected state
    try (StorageTx tx = underTest.openTx()) {
      Bucket bucket = tx.getBucket();
      Asset asset = checkNotNull(tx.findAsset(assetId, bucket));

      String fooValue = asset.require("foo");
      int finalVersion = asset.require("@version", Integer.class);

      assertThat(fooValue, is("secondValue"));
      assertThat(finalVersion, is(firstVersion + 2));
    }
  }

  private void checkSize(Iterable iterable, int expectedSize) {
    assertThat(Iterators.size(iterable.iterator()), is(expectedSize));
  }
}
