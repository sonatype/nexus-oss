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
import org.sonatype.nexus.component.model.ComponentEnvelope;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.internal.adapter.EntityAdapterRegistryImpl;
import org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter;
import org.sonatype.nexus.component.services.internal.adapter.TestComponentAdapter;
import org.sonatype.nexus.component.services.internal.id.DefaultEntityIdFactory;
import org.sonatype.nexus.component.services.model.TestAsset;
import org.sonatype.nexus.component.services.model.TestComponent;
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
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_TYPE;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_FIRST_CREATED;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_PATH;
import static org.sonatype.nexus.component.services.adapter.ComponentAdapter.P_ASSETS;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;
import static org.sonatype.nexus.component.services.internal.adapter.TestAssetAdapter.P_DOWNLOAD_COUNT;

/**
 * Supporting base class for {@link ComponentStoreImpl} integrationt tests.
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
  protected static final TestComponent TEST_COMPONENT_1 = createTestComponent(TEST_COMPONENT_ID_1, TEST_STRING_1, true);
  protected static final TestComponent TEST_COMPONENT_2 = createTestComponent(TEST_COMPONENT_ID_2, TEST_STRING_2, false);

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
    adapterRegistry.registerComponentAdapter(testComponentAdapter);
    adapterRegistry.registerAssetAdapter(testAssetAdapter);
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

  protected ComponentEnvelope<TestComponent, TestAsset> testEnvelope(int numAssets) {
    TestComponent sourceComponent = testComponent();
    Set<TestAsset> sourceAssets = Sets.newHashSet();
    for (int i = 0; i < numAssets; i++) {
      sourceAssets.add(testAsset(i + 1));
    }
    return new ComponentEnvelope<>(sourceComponent, sourceAssets);
  }

  protected TestAsset testAsset(int n) {
    TestAsset asset = new TestAsset();
    asset.setDownloadCount(n);
    asset.setContentType("text/plain");
    asset.setStreamSupplier(streamSupplier(testContent(n)));
    return asset;
  }

  protected void checkAsset(TestAsset actual, EntityId expectedComponentId, int n, boolean sameDates) {
    try {
      assertThat(actual.getComponentId().asUniqueString(), is(expectedComponentId.asUniqueString()));
      assertThat(actual.getDownloadCount(), is((long) n));
      assertThat(actual.getContentLength(), is((long) testContent(n).length()));
      assertThat(actual.getContentType(), is("text/plain"));
      assertThat(actual.getFirstCreated(), notNullValue());
      assertThat(actual.getLastModified(), notNullValue());
      if (sameDates) {
        assertThat(actual.getFirstCreated(), is(actual.getLastModified()));
      }
      else {
        assertThat(actual.getFirstCreated(), not(actual.getLastModified()));
      }
      assertThat(IOUtils.toString(actual.openStream()), is(testContent(n)));
    }
    catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }


  protected void checkAsset(TestAsset actual, EntityId expectedComponentId, int n) {
    checkAsset(actual, expectedComponentId, n, true);
  }

  protected void assertTestComponentsEqual(TestComponent actual, TestComponent expected) {
    assertThat(actual.getId().asUniqueString(), is(expected.getId().asUniqueString()));
    checkComponent(actual, expected);
  }

  protected void checkComponent(TestComponent actual, TestComponent expected) {
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

  protected void addTestComponentWithTwoAssets(ODatabaseDocumentTx db, TestComponent component) {
    // save the component initially with no assets
    ODocument componentDocument = db.newInstance("testcomponent");
    testComponentAdapter.populateDocument(component, componentDocument);
    componentDocument.field(P_ID, component.getId().asUniqueString());
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
    assets.add(addTestAsset(db, componentDocument.getIdentity(), component.getId(), n));
    assets.add(addTestAsset(db, componentDocument.getIdentity(), component.getId(), n + 1));
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

  protected static TestComponent createTestComponent(final String id, String stringValue, boolean populateOptionalProperties) {
    TestComponent component = testComponent(stringValue, populateOptionalProperties);
    component.setId(new EntityId(id));
    return component;
  }

  protected static TestComponent testComponent() {
    return testComponent(TEST_STRING_1, false);
  }

  protected static TestComponent testComponent(String stringValue, boolean populateOptionalProperties) {
    TestComponent component = new TestComponent();
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
