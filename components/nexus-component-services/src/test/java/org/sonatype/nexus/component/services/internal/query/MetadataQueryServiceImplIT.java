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

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapterRegistry;
import org.sonatype.nexus.component.services.internal.adapter.AssetEntityAdapter;
import org.sonatype.nexus.component.services.internal.adapter.ComponentEntityAdapterRegistryImpl;
import org.sonatype.nexus.component.services.internal.adapter.TestComponentEntityAdapter;
import org.sonatype.nexus.component.services.model.TestComponent;
import org.sonatype.nexus.internal.orient.DatabaseManagerImpl;
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import cucumber.api.java.After;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MetadataQueryServiceImplIT
    extends TestSupport
{
  private final TestComponentEntityAdapter testComponentEntityAdapter = new TestComponentEntityAdapter();

  private MinimalDatabaseServer databaseServer;

  private DatabaseManagerImpl databaseManager;

  private MapdbBlobMetadataStore blobMetadataStore;

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

    this.blobMetadataStore = new MapdbBlobMetadataStore(blobMetadataPath.toFile());
    blobMetadataStore.start();

    BlobStore blobStore = new FileBlobStore(blobContentPath, new VolumeChapterLocationStrategy(), new SimpleFileOperations(), blobMetadataStore);

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
    assertThat(countAllComponents(), is(0L));
    assertThat(queryService.countAssets(null), is(0L));
    assertThat(queryService.findAssets(null).size(), is(0));
  }

  @Test
  public void registerComponentAdapter() {
    adapterRegistry.registerAdapter(testComponentEntityAdapter);

    assertThat(adapterRegistry.componentClasses().size(), is(1));
    assertThat(queryService.componentClasses().size(), is(1));
    assertThat(countAllComponents(), is(0L));
    assertThat(queryService.countAssets(null), is(0L));
    assertThat(queryService.findAssets(null).size(), is(0));
  }

  @Test
  public void queryComponentsWithNoRestriction() {
    querySetup();
  }

  @Test
  public void queryComponentsWithSimpleComponentRestriction() {
  }

  @Test
  public void queryComponentsWithCompoundComponentRestriction() {
  }

  @Test
  public void queryComponentsWithSimpleAssetRestriction() {
  }

  @Test
  public void queryComponentsWithCompoundAssetRestriction() {
  }

  @Test
  public void queryAssetsWithNoRestriction() {
  }

  @Test
  public void queryAssetsWithSimpleComponentRestriction() {
  }

  @Test
  public void queryAssetsWithCompoundComponentRestriction() {
  }

  @Test
  public void queryAssetsWithSimpleAssetRestriction() {
  }

  @Test
  public void queryAssetsWithCompoundAssetRestriction() {
  }

  private static final String TEST_ID1 = "component1";
  private static final String TEST_ID2 = "component2";
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
  private static final String TEST_STRING1 = "String One";
  private static final String TEST_STRING2 = "String Two";
  private static final Object TEST_UNREGISTERED = "Unregistered";


  private void querySetup() {
    adapterRegistry.registerAdapter(new TestComponentEntityAdapter());

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      addTestComponent(db, TEST_ID1, TEST_STRING1, true);
      addTestComponent(db, TEST_ID2, TEST_STRING2, false);
    }
  }

  private void addTestComponent(ODatabaseDocumentTx db, final String id, String stringValue, boolean populateOptionalFields) {
    TestComponent component = new TestComponent();
    component.setId(new ComponentId() { // TODO: Switch to use ComponentId class when available
      @Override
      public String asUniqueString() {
        return id;
      }
    });
    if (populateOptionalFields) {
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

    ODocument document = db.newInstance(TestComponentEntityAdapter.ORIENT_CLASS_NAME);
    testComponentEntityAdapter.convertToDocument(component, document);
    document.save();
  }

  private long countAllComponents() {
    long total = 0;
    for (Class<? extends Component> componentClass: queryService.componentClasses()) {
      total += queryService.countComponents(componentClass, null);
    }
    return total;
  }
}
