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

import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.model.Envelope;
import org.sonatype.nexus.component.services.adapter.AssetAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.internal.adapter.EntityAdapterRegistryImpl;
import org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter;
import org.sonatype.nexus.component.services.internal.adapter.TestComponentAdapter;
import org.sonatype.nexus.component.services.internal.id.DefaultEntityIdFactory;
import org.sonatype.nexus.component.services.storage.ComponentStore;
import org.sonatype.nexus.internal.orient.DatabaseManagerImpl;
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Supplier;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_BLOB_REFS;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_COMPONENT;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_LENGTH;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_TYPE;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_FIRST_CREATED;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_LAST_MODIFIED;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_PATH;
import static org.sonatype.nexus.component.services.adapter.ComponentAdapter.P_ASSETS;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;
import static org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter.P_DOWNLOAD_COUNT;
import static org.sonatype.nexus.component.services.internal.adapter.TestComponentAdapter.*;

/**
 * Supporting base class for {@link ComponentStoreImpl} integration tests.
 */
public abstract class ComponentStoreImplITSupport
    extends TestSupport
{
  protected static final String TEST_COMPONENT_ID_1 = "component1";
  protected static final String TEST_COMPONENT_ID_2 = "component2";
  protected static final byte[] TEST_BINARY = new byte[] { 0x01, 0x02 };
  protected static final boolean TEST_BOOLEAN = true;
  protected static final byte TEST_BYTE = (byte) 0x42;
  protected static final DateTime TEST_DATETIME = new DateTime();
  protected static final double TEST_DOUBLE = 3.0;
  protected static final List<String> TEST_EMBEDDEDLIST = ImmutableList.of("item1", "item2");
  protected static final Map<String, String> TEST_EMBEDDEDMAP = ImmutableMap.of("key1", "value1");
  protected static final Set<String> TEST_EMBEDDEDSET = ImmutableSet.of("item1", "item2", "item3");
  protected static final float TEST_FLOAT = 4.0f;
  protected static final int TEST_INTEGER = 5;
  protected static final long TEST_LONG = 6;
  protected static final short TEST_SHORT = 7;
  protected static final String TEST_STRING_1 = "String One";
  protected static final String TEST_STRING_2 = "String Two";
  protected static final Object TEST_UNREGISTERED = "Unregistered";
  protected static final Entity TEST_COMPONENT_1 = createTestComponent(TEST_COMPONENT_ID_1, TEST_STRING_1, true);
  protected static final Entity TEST_COMPONENT_2 = createTestComponent(TEST_COMPONENT_ID_2, TEST_STRING_2, false);

  protected final TestAssetAdapter testAssetAdapter = new TestAssetAdapter();

  protected final TestComponentAdapter testComponentAdapter = new TestComponentAdapter();

  protected MinimalDatabaseServer databaseServer;

  protected DatabaseManagerImpl databaseManager;

  protected BlobMetadataStore blobMetadataStore;

  protected BlobStore blobStore;

  protected ComponentStore componentStore;

  protected EntityAdapterRegistry adapterRegistry;

  protected Provider<DatabaseInstance> databaseInstanceProvider;

  @Before
  public void setUp() throws Exception {
    // yeah, yeah
    OLogManager.instance().setWarnEnabled(false);
    OLogManager.instance().setInfoEnabled(false);

    // disable snappy-java, see https://github.com/sonatype/nexus-oss/commit/96889092931b326e47582012a96f504ad88adef3
    OGlobalConfiguration.STORAGE_COMPRESSION_METHOD.setValue("nothing");

    // create temp dir for test blob content, blob metadata, and component metadata storage
    File testDir = util.createTempDir();
    Path testRoot = testDir.toPath();
    Path blobContentPath = testRoot.resolve("blobContent");
    Path blobMetadataPath = testRoot.resolve("blobMetadata");
    Path componentMetadataPath = testRoot.resolve("componentMetadata");
    log("NOTE: To examine the state of this database after the test ends, enter the "
        + "following in the OrientDB console:\nuse plocal:" + componentMetadataPath + " admin admin");

    this.databaseServer = new MinimalDatabaseServer();
    databaseServer.start();

    this.databaseManager = new DatabaseManagerImpl(componentMetadataPath.getParent().toFile());
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
      maybeDropClass(schema, "testcomponent");
      maybeDropClass(schema, "component");
      maybeDropClass(schema, "testasset");
      maybeDropClass(schema, "asset");
    }

    this.blobMetadataStore = MapdbBlobMetadataStore.create(blobMetadataPath.toFile());
    blobMetadataStore.start();

    blobStore = new FileBlobStore(blobContentPath, new VolumeChapterLocationStrategy(), new SimpleFileOperations(), blobMetadataStore);

    adapterRegistry = new EntityAdapterRegistryImpl(databaseInstanceProvider);
    componentStore = new ComponentStoreImpl(databaseInstanceProvider, blobStore, adapterRegistry, new DefaultEntityIdFactory());
  }

  protected static void maybeDropClass(OSchema schema, String orientClassName) {
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

  protected void registerTestAdapters() {
    adapterRegistry.registerAdapter(new EntityAdapter());
    adapterRegistry.registerAdapter(new ComponentAdapter());
    adapterRegistry.registerAdapter(new AssetAdapter());
    adapterRegistry.registerAdapter(testComponentAdapter);
    adapterRegistry.registerAdapter(testAssetAdapter);
    componentStore.prepareStorage(TestComponentAdapter.CLASS_NAME, TestAssetAdapter.CLASS_NAME);
  }

  protected void addTwoTestComponentsWithTwoAssetsEach() {
    registerTestAdapters();
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

  protected Envelope testEnvelope(int numAssets) {
    Component sourceComponent = testComponent();
    Set<Asset> sourceAssets = Sets.newHashSet();
    for (int i = 0; i < numAssets; i++) {
      sourceAssets.add(testAsset(i + 1));
    }
    return new Envelope(sourceComponent, sourceAssets);
  }

  protected Asset testAsset(int n) {
    Asset asset = new Asset(TestAssetAdapter.CLASS_NAME);
    asset.put(P_DOWNLOAD_COUNT, n);
    asset.put(P_CONTENT_TYPE, "text/plain");
    asset.setStreamSupplier(streamSupplier(testContent(n)));
    return asset;
  }

  protected void checkAsset(Asset actual, EntityId expectedComponentId, int n, boolean sameDates) {
    try {
      assertThat(actual.get(P_COMPONENT, EntityId.class).asUniqueString(),
          is(expectedComponentId.asUniqueString()));
      assertThat(actual.get(P_DOWNLOAD_COUNT, Long.class), is((long) n));
      assertThat(actual.get(P_CONTENT_LENGTH, Long.class), is((long) testContent(n).length()));
      assertThat(actual.get(P_CONTENT_TYPE, String.class), is("text/plain"));
      assertThat(actual.get(P_FIRST_CREATED, DateTime.class), notNullValue());
      assertThat(actual.get(P_LAST_MODIFIED, DateTime.class), notNullValue());
      if (sameDates) {
        assertThat(actual.get(P_FIRST_CREATED, DateTime.class), is(actual.get(P_LAST_MODIFIED, DateTime.class)));
      }
      else {
        assertThat(actual.get(P_FIRST_CREATED, DateTime.class), not(actual.get(P_LAST_MODIFIED, DateTime.class)));
      }
      assertThat(actual.get(P_BLOB_REFS, Map.class).size(), is(1));
      assertThat(IOUtils.toString(actual.openStream()), is(testContent(n)));
    }
    catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }


  protected void checkAsset(Asset actual, EntityId expectedComponentId, int n) {
    checkAsset(actual, expectedComponentId, n, true);
  }

  protected void assertTestComponentsEqual(Entity actual, Entity expected) {
    assertThat(actual.get(P_ID, EntityId.class).asUniqueString(), is(expected.get(P_ID, EntityId.class).asUniqueString()));
    checkComponent(actual, expected);
  }

  protected void checkComponent(Entity actual, Entity expected) {
    assertThat(actual.get(P_BINARY, byte[].class), is(expected.get(P_BINARY, byte[].class)));
    assertThat(actual.get(P_BOOLEAN, Boolean.class), is(expected.get(P_BOOLEAN, Boolean.class)));
    assertThat(actual.get(P_BYTE, Byte.class), is(expected.get(P_BYTE, Byte.class)));
    assertThat(actual.get(P_DATETIME, DateTime.class), is(expected.get(P_DATETIME, DateTime.class)));
    assertThat(actual.get(P_DOUBLE, Double.class), is(expected.get(P_DOUBLE, Double.class)));
    assertThat(actual.get(P_EMBEDDEDLIST, List.class), is(expected.get(P_EMBEDDEDLIST, List.class)));
    assertThat(actual.get(P_EMBEDDEDMAP, Map.class), is(expected.get(P_EMBEDDEDMAP, Map.class)));
    assertThat(actual.get(P_EMBEDDEDSET, Set.class), is(expected.get(P_EMBEDDEDSET, Set.class)));
    assertThat(actual.get(P_FLOAT, Float.class), is(expected.get(P_FLOAT, Float.class)));
    assertThat(actual.get(P_INTEGER, Integer.class), is(expected.get(P_INTEGER, Integer.class)));
    assertThat(actual.get(P_LONG, Long.class), is(expected.get(P_LONG, Long.class)));
    assertThat(actual.get(P_SHORT, Short.class), is(expected.get(P_SHORT, Short.class)));
    assertThat(actual.get(P_STRING, String.class), is(expected.get(P_STRING, String.class)));
    assertThat(actual.get(P_UNREGISTERED, Object.class), is(expected.get(P_UNREGISTERED, Object.class)));
  }

  protected Object toStorageType(Object o) {
    if (o instanceof DateTime) {
      return ((DateTime) o).toDate();
    }
    else if (o instanceof EntityId) {
      return ((EntityId) o).asUniqueString();
    }
    return o;
  }

  protected void addTestComponentWithTwoAssets(ODatabaseDocumentTx db, Entity component) {
    // save the component initially with no assets
    ODocument componentDocument = db.newInstance("testcomponent");

    Set<String> ignoreProps = ImmutableSet.of(P_ID, P_ASSETS);
    Map<String, Object> map = component.toMap(false);
    for (String name: map.keySet()) {
      if (!ignoreProps.contains(name)) {
        Object o = map.get(name);
        if (o instanceof DateTime) {
          o = ((DateTime) o).toDate();
        }
        componentDocument.field(name, o);
      }
    }

    componentDocument.field(P_ID, component.get(P_ID, EntityId.class).asUniqueString());
    Set<ORID> assets = Sets.newHashSet();
    componentDocument.field(P_ASSETS, assets);
    componentDocument.save();

    // then add a couple asset documents and re-save it with asset rids populated
    int n;
    if (component == TEST_COMPONENT_1) {
      n = 1;
    }
    else {
      n = 3;
    }
    assets.add(addTestAsset(db, componentDocument.getIdentity(), component.get(P_ID, EntityId.class), n));
    assets.add(addTestAsset(db, componentDocument.getIdentity(), component.get(P_ID, EntityId.class), n + 1));
    componentDocument.field(P_ASSETS, assets);
    componentDocument.save();
  }

  protected ORID addTestAsset(ODatabaseDocumentTx db, ORID componentDocumentRid, EntityId componentId, int n) {
    // create the blob
    String assetId = "asset" + n;
    Blob blob = blobStore.create(toStream(testContent(n)), ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, assetId,
        BlobStore.CREATED_BY_HEADER, "Test"));

    // create the asset document
    ODocument assetDocument = db.newInstance("testasset");
    assetDocument.field(P_ID, assetId);
    assetDocument.field(P_COMPONENT, componentDocumentRid);
    assetDocument.field(P_DOWNLOAD_COUNT, n);
    assetDocument.field(P_FIRST_CREATED, blob.getMetrics().getCreationTime().toDate());
    assetDocument.field(P_CONTENT_TYPE, "text/plain");
    if (n % 2 == 1) {
      assetDocument.field(P_PATH, "" + n); // for variance, only set path if n is odd
    }
    Map<String, String> blobRefs = ImmutableMap.of("someBlobStoreId", blob.getId().asUniqueString());
    assetDocument.field(P_BLOB_REFS, blobRefs);
    assetDocument.save();

    // return the id of the saved asset document
    return assetDocument.getIdentity();
  }

  protected String testContent(int n) {
    return "Test Content " + n;
  }

  protected static Component createTestComponent(final String id, String stringValue, boolean populateOptionalProperties) {
    Component component = testComponent(stringValue, populateOptionalProperties);
    component.put(P_ID, new EntityId(id));
    return component;
  }

  protected static Component testComponent() {
    return testComponent(TEST_STRING_1, false);
  }

  protected static Component testComponent(String stringValue, boolean populateOptionalProperties) {
    Component component = new Component(TestComponentAdapter.CLASS_NAME);
    if (populateOptionalProperties) {
      component.put(P_BINARY, TEST_BINARY);
      component.put(P_BOOLEAN, TEST_BOOLEAN);
      component.put(P_BYTE, TEST_BYTE);
      component.put(P_DATETIME, TEST_DATETIME);
      component.put(P_DOUBLE, TEST_DOUBLE);
      component.put(P_EMBEDDEDLIST, TEST_EMBEDDEDLIST);
      component.put(P_EMBEDDEDMAP, TEST_EMBEDDEDMAP);
      component.put(P_EMBEDDEDSET, TEST_EMBEDDEDSET);
      component.put(P_FLOAT, TEST_FLOAT);
      component.put(P_INTEGER, TEST_INTEGER);
      component.put(P_LONG, TEST_LONG);
      component.put(P_SHORT, TEST_SHORT);
      component.put(P_UNREGISTERED, TEST_UNREGISTERED);
    }
    component.put(P_STRING, stringValue);
    return component;
  }

  protected InputStream toStream(String string) {
    try {
      return new ByteArrayInputStream(string.getBytes("UTF-8"));
    } catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }

  protected Supplier<InputStream> streamSupplier(final String string) {
    return new Supplier<InputStream>() {
      @Override
      public InputStream get() {
        return toStream(string);
      }
    };
  }
}
