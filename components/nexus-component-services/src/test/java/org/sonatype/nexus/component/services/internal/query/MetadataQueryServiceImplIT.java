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
package org.sonatype.nexus.component.services.internal.query;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapterRegistry;
import org.sonatype.nexus.component.services.internal.adapter.AssetEntityAdapter;
import org.sonatype.nexus.component.services.internal.adapter.ComponentEntityAdapterRegistryImpl;
import org.sonatype.nexus.component.services.internal.adapter.TestComponentEntityAdapter;
import org.sonatype.nexus.component.services.model.TestComponent;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.internal.orient.DatabaseManagerImpl;
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.and;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.assetPropertyEquals;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.assetPropertyLike;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.componentPropertyEquals;
import static org.sonatype.nexus.component.services.query.MetadataQueryRestriction.or;

/**
 * Integration tests for {@link MetadataQueryServiceImpl}.
 */
public class MetadataQueryServiceImplIT
    extends TestSupport
{
  private static final String TEST_ID_1 = "component1";
  private static final String TEST_ID_2 = "component2";
  private static final byte[] TEST_BINARY = new byte[] { 0x01, 0x02 };
  private static final boolean TEST_BOOLEAN = true;
  private static final byte TEST_BYTE = (byte) 0x42;
  private static final DateTime TEST_DATETIME = new DateTime();
  private static final double TEST_DOUBLE = 3.0;
  private static final List<String> TEST_EMBEDDEDLIST = ImmutableList.of("item1", "item2");
  private static final Map<String, String> TEST_EMBEDDEDMAP = ImmutableMap.of("key1", "value1");
  private static final Set<String> TEST_EMBEDDEDSET = ImmutableSet.of("item1", "item2", "item3");
  private static final float TEST_FLOAT = 4.0f;
  private static final int TEST_INTEGER = 5;
  private static final long TEST_LONG = 6;
  private static final short TEST_SHORT = 7;
  private static final String TEST_STRING_1 = "String One";
  private static final String TEST_STRING_2 = "String Two";
  private static final Object TEST_UNREGISTERED = "Unregistered";
  private static final TestComponent TEST_COMPONENT_1 = createTestComponent(TEST_ID_1, TEST_STRING_1, true);
  private static final TestComponent TEST_COMPONENT_2 = createTestComponent(TEST_ID_2, TEST_STRING_2, false);

  private final TestComponentEntityAdapter testComponentEntityAdapter = new TestComponentEntityAdapter();

  private MinimalDatabaseServer databaseServer;

  private DatabaseManagerImpl databaseManager;

  private BlobMetadataStore blobMetadataStore;

  private BlobStore blobStore;

  private MetadataQueryServiceImpl queryService;

  private ComponentEntityAdapterRegistry adapterRegistry;

  private Provider<DatabaseInstance> databaseInstanceProvider;

  @Before
  public void setUp() throws Exception {
    // yeah, yeah
    OLogManager.instance().setWarnEnabled(false);
    OLogManager.instance().setInfoEnabled(false);

    // disable snappy-java, see https://github.com/sonatype/nexus-oss/commit/96889092931b326e47582012a96f504ad88adef3
    OGlobalConfiguration.STORAGE_COMPRESSION_METHOD.setValue("nothing");

    // create temp dir for test blob content, blob metadata, and component metadata storage
    File testDir = util.createTempDir();
    System.out.println(testDir);
    Path testRoot = testDir.toPath();
    Path blobContentPath = testRoot.resolve("blobContent");
    Path blobMetadataPath = testRoot.resolve("blobMetadata");
    Path componentMetadataPath = testRoot.resolve("componentMetadata");

    this.databaseServer = new MinimalDatabaseServer();
    databaseServer.start();

    this.databaseManager = new DatabaseManagerImpl(componentMetadataPath.toFile());
    databaseManager.start();

    databaseInstanceProvider = new Provider<DatabaseInstance>() {
      @Override
      public DatabaseInstance get() {
        return databaseManager.instance("componentMetadata");
      }
    };

    // drop all classes
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OSchema schema = db.getMetadata().getSchema();
      maybeDropClass(schema, AssetEntityAdapter.ORIENT_CLASS_NAME);
      maybeDropClass(schema, ComponentEntityAdapter.ORIENT_BASE_CLASS_NAME);
    }

    this.blobMetadataStore = MapdbBlobMetadataStore.create(blobMetadataPath.toFile());
    blobMetadataStore.start();

    blobStore = new FileBlobStore(blobContentPath, new VolumeChapterLocationStrategy(), new SimpleFileOperations(), blobMetadataStore);

    adapterRegistry = new ComponentEntityAdapterRegistryImpl(databaseInstanceProvider);
    queryService = new MetadataQueryServiceImpl(databaseInstanceProvider, blobStore, adapterRegistry);
  }

  private static void maybeDropClass(OSchema schema, String orientClassName) {
    if (schema.existsClass(orientClassName)) {
      schema.dropClass(orientClassName);
    }
  }

  @After
  public void tearDown() throws Exception {
    if (databaseManager != null) {
      databaseManager.stop();
      databaseManager = null;
    }

    if (databaseServer != null) {
      databaseServer.stop();
      databaseServer = null;
    }

    blobMetadataStore.stop();
  }

  @Test
  public void initialState() {
    assertThat(adapterRegistry.componentClasses().size(), is(0));
    assertThat(queryService.componentClasses().size(), is(0));
    assertThat(queryService.countAssets(null), is(0L));
    assertThat(queryService.findAssets(null).size(), is(0));
  }

  @Test
  public void registerComponentAdapter() {
    adapterRegistry.registerAdapter(testComponentEntityAdapter);

    assertThat(adapterRegistry.componentClasses().size(), is(1));
    assertThat(queryService.componentClasses().size(), is(1));
    assertThat(queryService.countAssets(null), is(0L));
    assertThat(queryService.findAssets(null).size(), is(0));
  }

  @Test
  public void totalComponentCount() {
    addTwoTestComponentsWithTwoAssetsEach();
    long total = 0;
    for (Class<? extends Component> componentClass: queryService.componentClasses()) {
      total += queryService.countComponents(componentClass, null);
    }
    assertThat(total, is(2L));
  }

  @Test
  public void totalAssetCount() {
    addTwoTestComponentsWithTwoAssetsEach();

    assertThat(queryService.countAssets(null), is(4L));
  }

  @Test
  public void queryComponentsWithNoRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent

    // count should be 2
    assertThat(queryService.countComponents(TestComponent.class, null), is(2L));

    MetadataQuery query = new MetadataQuery().orderBy(ComponentEntityAdapter.P_ID, true);
    List<TestComponent> results = queryService.findComponents(TestComponent.class, query);

    // query should return component1 then component2
    assertThat(results.size(), is(2));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
    assertTestComponentsEqual(results.get(1), TEST_COMPONENT_2);
  }

  @Test
  public void queryComponentsWithSimpleComponentRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE id = 'component1'
    MetadataQueryRestriction restriction = componentPropertyEquals(ComponentEntityAdapter.P_ID, TEST_ID_1);

    // count should be 1
    assertThat(queryService.countComponents(TestComponent.class, restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<TestComponent> results = queryService.findComponents(TestComponent.class, query);

    // query should return component1 only
    assertThat(results.size(), is(1));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
  }

  @Test
  public void queryComponentsWithCompoundComponentRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE (id = 'component1' OR id = 'component2')
    MetadataQueryRestriction restriction = or(
        componentPropertyEquals(ComponentEntityAdapter.P_ID, TEST_ID_1),
        componentPropertyEquals(ComponentEntityAdapter.P_ID, TEST_ID_2));

    // count should be 2
    assertThat(queryService.countComponents(TestComponent.class, restriction), is(2L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(ComponentEntityAdapter.P_ID,
        true);
    List<TestComponent> results = queryService.findComponents(TestComponent.class, query);

    // query should return component1 then component2
    assertThat(results.size(), is(2));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
    assertTestComponentsEqual(results.get(1), TEST_COMPONENT_2);
  }

  @Test
  public void queryComponentsWithSimpleAssetRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE assets contains ( path = '/test/path/1' )
    MetadataQueryRestriction restriction = assetPropertyEquals(AssetEntityAdapter.P_PATH, testPath(1));

    // count should be 1
    assertThat(queryService.countComponents(TestComponent.class, restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<TestComponent> results = queryService.findComponents(TestComponent.class, query);

    // query should return component1 only
    assertThat(results.size(), is(1));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_1);
  }

  @Test
  public void queryComponentsWithCompoundAssetRestriction() {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM testcomponent WHERE (assets contains ( path = '/test/path/1' ) OR assets contains ( contentType = 'text/plain' ))
    MetadataQueryRestriction restriction = or(
        assetPropertyEquals(AssetEntityAdapter.P_PATH, testPath(1)),
        assetPropertyEquals(AssetEntityAdapter.P_CONTENT_TYPE, "text/plain"));

    // count should be 2
    assertThat(queryService.countComponents(TestComponent.class, restriction), is(2L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(ComponentEntityAdapter.P_ID,
        false);
    List<TestComponent> results = queryService.findComponents(TestComponent.class, query);

    // query should return component2 then component1 (since we ordered results DESCending this time)
    assertThat(results.size(), is(2));
    assertTestComponentsEqual(results.get(0), TEST_COMPONENT_2);
    assertTestComponentsEqual(results.get(1), TEST_COMPONENT_1);
  }

  @Test
  public void queryAssetsWithNoRestrictionOrderByTwoPropertys() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM asset

    // count should be 4
    assertThat(queryService.countAssets(null), is(4L));

    MetadataQuery query = new MetadataQuery()
        .orderBy(AssetEntityAdapter.P_COMPONENT, true)
        .orderBy(AssetEntityAdapter.P_PATH, false);
    List<Asset> results = queryService.findAssets(query);

    // query should return all four assets in order of component ascending, then path descending
    assertThat(results.size(), is(4));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 2);
    checkAsset(results.get(1), TEST_COMPONENT_1.getId(), 1);
    checkAsset(results.get(2), TEST_COMPONENT_2.getId(), 4);
    checkAsset(results.get(3), TEST_COMPONENT_2.getId(), 3);
  }

  @Test
  public void queryAssetsWithSimpleAssetRestriction() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM asset WHERE path = 'test/path/1'
    MetadataQueryRestriction restriction = assetPropertyEquals(AssetEntityAdapter.P_PATH, testPath(1));

    // count should be 1
    assertThat(queryService.countAssets(restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<Asset> results = queryService.findAssets(query);

    // query should return component1's first asset only
    assertThat(results.size(), is(1));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
  }

  @Test
  public void queryAssetsWithCompoundAssetRestriction() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM asset WHERE (path = '/test/path/1' OR contentType = 'text/plain')
    MetadataQueryRestriction restriction = or(
        assetPropertyEquals(AssetEntityAdapter.P_PATH, testPath(1)),
        assetPropertyEquals(AssetEntityAdapter.P_CONTENT_TYPE, "text/plain"));

    // count should be 2
    assertThat(queryService.countAssets(restriction), is(4L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(AssetEntityAdapter.P_ID, true);
    List<Asset> results = queryService.findAssets(query);

    // query should return all four assets in ascending order of componentId:path
    assertThat(results.size(), is(4));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
    checkAsset(results.get(1), TEST_COMPONENT_1.getId(), 2);
    checkAsset(results.get(2), TEST_COMPONENT_2.getId(), 3);
    checkAsset(results.get(3), TEST_COMPONENT_2.getId(), 4);
  }

  @Test
  public void queryAssetsWithSimpleComponentRestriction() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM asset WHERE component.id = 'component1'
    MetadataQueryRestriction restriction = componentPropertyEquals(ComponentEntityAdapter.P_ID, TEST_ID_1);

    // count should be 2
    assertThat(queryService.countAssets(restriction), is(2L));

    MetadataQuery query = new MetadataQuery().restriction(restriction).orderBy(AssetEntityAdapter.P_PATH, true);
    List<Asset> results = queryService.findAssets(query);

    // query should return component1's assets in ascending order of path
    assertThat(results.size(), is(2));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
    checkAsset(results.get(1), TEST_COMPONENT_1.getId(), 2);
  }

  @Test
  public void queryAssetsWithCompoundComponentAndAssetRestrictionUsingLike() throws IOException {
    addTwoTestComponentsWithTwoAssetsEach();

    // SELECT FROM asset WHERE (component.id = 'component1' AND path LIKE '%1')
    MetadataQueryRestriction restriction = and(
        componentPropertyEquals(ComponentEntityAdapter.P_ID, TEST_ID_1),
        assetPropertyLike(AssetEntityAdapter.P_PATH, "%1"));

    // count should be 1
    assertThat(queryService.countAssets(restriction), is(1L));

    MetadataQuery query = new MetadataQuery().restriction(restriction);
    List<Asset> results = queryService.findAssets(query);

    // query should return component1's first asset only
    assertThat(results.size(), is(1));
    checkAsset(results.get(0), TEST_COMPONENT_1.getId(), 1);
  }

  @Test
  public void pageAssetsUsingSkipLimit() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(2);

    List<Asset> page1 = queryService.findAssets(query);
    assertThat(page1.size(), is(2));

    List<Asset> page2 = queryService.findAssets(query.skip(2));
    assertThat(page2.size(), is(2));

    List<Asset> page3 = queryService.findAssets(query.skip(4));
    assertThat(page3.size(), is(0));
  }

  @Test
  public void pageAssetsUsingSkipComponentIdAndSkipAssetPath() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(2);

    List<Asset> page1 = queryService.findAssets(query);
    assertThat(page1.size(), is(2));

    query.skipComponentId(page1.get(1).getComponentId()).skipAssetPath(page1.get(1).getPath());
    List<Asset> page2 = queryService.findAssets(query);
    assertThat(page2.size(), is(2));

    query.skipComponentId(page2.get(1).getComponentId()).skipAssetPath(page2.get(1).getPath());
    List<Asset> page3 = queryService.findAssets(query);
    assertThat(page3.size(), is(0));
  }

  @Test
  public void pageComponentsUsingSkipLimit() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(1);

    List<TestComponent> page1 = queryService.findComponents(TestComponent.class, query);
    assertThat(page1.size(), is(1));

    List<TestComponent> page2 = queryService.findComponents(TestComponent.class, query.skip(1));
    assertThat(page2.size(), is(1));

    List<TestComponent> page3 = queryService.findComponents(TestComponent.class, query.skip(2));
    assertThat(page3.size(), is(0));
  }

  @Test
  public void pageComponentsUsingSkipComponentId() {
    addTwoTestComponentsWithTwoAssetsEach();

    MetadataQuery query = new MetadataQuery().limit(1);

    List<TestComponent> page1 = queryService.findComponents(TestComponent.class, query);
    assertThat(page1.size(), is(1));

    query.skipComponentId(page1.get(0).getId());
    List<TestComponent> page2 = queryService.findComponents(TestComponent.class, query);
    assertThat(page2.size(), is(1));

    query.skipComponentId(page2.get(0).getId());
    List<TestComponent> page3 = queryService.findComponents(TestComponent.class, query);
    assertThat(page3.size(), is(0));
  }

  private void addTwoTestComponentsWithTwoAssetsEach() {
    adapterRegistry.registerAdapter(testComponentEntityAdapter);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      // add test components and assets in a transaction
      db.begin();
      try {
        addTestComponentWithTwoAssets(db, TEST_COMPONENT_1);
        addTestComponentWithTwoAssets(db, TEST_COMPONENT_2);
        db.commit();
      } finally {
        if (db.getTransaction().isActive()) {
          db.rollback();
        }
      }
    }
  }

  private void checkAsset(Asset actual, ComponentId expectedComponentId, int n) throws IOException {
    assertThat(actual.getComponentId().asUniqueString(), is(expectedComponentId.asUniqueString()));
    assertThat(actual.getPath(), is(testPath(n)));
    assertThat(actual.getContentLength(), is((long) testContent(n).length()));
    assertThat(actual.getContentType(), is("text/plain"));
    assertThat(actual.getFirstCreated(), notNullValue());
    assertThat(actual.getFirstCreated(), is(actual.getLastModified()));
    assertThat(IOUtils.toString(actual.openStream()), is(testContent(n)));
  }

  private void assertTestComponentsEqual(TestComponent actual, TestComponent expected) {
    assertThat(actual.getId().asUniqueString(), is(expected.getId().asUniqueString()));
    assertThat(actual.getBinaryProp(), is(expected.getBinaryProp()));
    assertThat(actual.getBooleanProp(), is(expected.getBooleanProp()));
    assertThat(actual.getByteProp(), is(expected.getByteProp()));
    assertThat(actual.getDatetimeProp(), is(expected.getDatetimeProp()));
    assertThat(actual.getDoubleProp(), is(expected.getDoubleProp()));
    assertThat(actual.getEmbeddedListProp(), is(expected.getEmbeddedListProp()));
    assertThat(actual.getEmbeddedMapProp(), is(expected.getEmbeddedMapProp()));
    assertThat(actual.getEmbeddedSetProp(), is(expected.getEmbeddedSetProp()));
    assertThat(actual.getFloatProp(), is(expected.getFloatProp()));
    assertThat(actual.getIntegerProp(), is(expected.getIntegerProp()));
    assertThat(actual.getLongProp(), is(expected.getLongProp()));
    assertThat(actual.getShortProp(), is(expected.getShortProp()));
    assertThat(actual.getStringProp(), is(expected.getStringProp()));
    assertThat(actual.getUnregisteredProp(), is(expected.getUnregisteredProp()));
  }

  private void addTestComponentWithTwoAssets(ODatabaseDocumentTx db, TestComponent component) {
    // save the component initially with no assets
    ODocument componentDocument = db.newInstance(TestComponentEntityAdapter.ORIENT_CLASS_NAME);
    testComponentEntityAdapter.convertToDocument(component, componentDocument);
    Set<ORID> assets = Sets.newHashSet();
    componentDocument.field(ComponentEntityAdapter.P_ASSETS, assets);
    componentDocument.save();

    // then add a couple asset documents and re-save it with asset rids populated
    int n;
    if (component == TEST_COMPONENT_1) {
      n = 1;
    }
    else {
      n = 3;
    }
    assets.add(addTestAsset(db, componentDocument.getIdentity(), component.getId(), n));
    assets.add(addTestAsset(db, componentDocument.getIdentity(), component.getId(), n + 1));
    componentDocument.field(ComponentEntityAdapter.P_ASSETS, assets);
    componentDocument.save();
  }

  private ORID addTestAsset(ODatabaseDocumentTx db, ORID componentDocumentRid, ComponentId componentId, int n) {
    // create the blob
    String assetPath = testPath(n);
    String assetId = AssetEntityAdapter.assetId(componentId, assetPath);
    Blob blob = blobStore.create(toStream(testContent(n)), ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, assetId,
        BlobStore.CREATED_BY_HEADER, "Test"));

    // create the asset document
    ODocument assetDocument = db.newInstance(AssetEntityAdapter.ORIENT_CLASS_NAME);
    assetDocument.field(AssetEntityAdapter.P_ID, assetId);
    assetDocument.field(AssetEntityAdapter.P_COMPONENT, componentDocumentRid);
    assetDocument.field(AssetEntityAdapter.P_PATH, assetPath);
    assetDocument.field(AssetEntityAdapter.P_FIRST_CREATED, blob.getMetrics().getCreationTime().toDate());
    assetDocument.field(AssetEntityAdapter.P_CONTENT_TYPE, "text/plain");
    Map<String, String> blobRefs = ImmutableMap.of("someBlobStoreId", blob.getId().asUniqueString());
    assetDocument.field(AssetEntityAdapter.P_BLOB_REFS, blobRefs);
    assetDocument.save();

    // return the id of the saved asset document
    return assetDocument.getIdentity();
  }

  private static String testContent(int n) {
    return "Test Content " + n;
  }

  private static String testPath(int n) {
    return "test/path/" + n;
  }

  private static TestComponent createTestComponent(final String id, String stringValue, boolean populateOptionalProperties) {
    TestComponent component = new TestComponent();
    component.setId(new ComponentId()
    { // TODO: Switch to use ComponentId class when available
      @Override
      public String asUniqueString() {
        return id;
      }
    });
    if (populateOptionalProperties) {
      component.setBinaryProp(TEST_BINARY);
      component.setBooleanProp(TEST_BOOLEAN);
      component.setByteProp(TEST_BYTE);
      component.setDatetimeProp(TEST_DATETIME);
      component.setDoubleProp(TEST_DOUBLE);
      component.setEmbeddedListProp(TEST_EMBEDDEDLIST);
      component.setEmbeddedMapProp(TEST_EMBEDDEDMAP);
      component.setEmbeddedSetProp(TEST_EMBEDDEDSET);
      component.setFloatProp(TEST_FLOAT);
      component.setIntegerProp(TEST_INTEGER);
      component.setLongProp(TEST_LONG);
      component.setShortProp(TEST_SHORT);
      component.setUnregisteredProp(TEST_UNREGISTERED);
    }
    component.setStringProp(stringValue);
    return component;
  }

  private static InputStream toStream(String string) {
    try {
      return new ByteArrayInputStream(string.getBytes("UTF-8"));
    } catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }
}
