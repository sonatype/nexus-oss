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
package org.sonatype.nexus.component.assetstore.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.recordstore.Record;
import org.sonatype.nexus.component.recordstore.RecordStoreSchema;
import org.sonatype.nexus.component.recordstore.RecordStoreSession;
import org.sonatype.nexus.component.recordstore.RecordType;
import org.sonatype.nexus.component.recordstore.internal.OrientRecordStore;
import org.sonatype.nexus.internal.orient.DatabaseManagerImpl;
import org.sonatype.nexus.internal.orient.HexRecordIdObfuscator;
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Throwables;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class AssetStoreImplIT
    extends TestSupport
{
  private static final ComponentId TEST_COMPONENT_ID = new ComponentId() {
    public String asUniqueString() {
      return "TEST_COMPONENT_ID";
    }
  };

  private static final String TEST_CONTENT = "TEST_CONTENT";

  private static final String TEST_CONTENT_TYPE = "TEST_CONTENT_TYPE";

  private static final String TEST_PATH = "TEST_PATH";

  private MinimalDatabaseServer databaseServer;

  private DatabaseManagerImpl databaseManager;

  private OrientRecordStore recordStore;

  private BlobMetadataStore blobMetadataStore;

  private BlobStore blobStore;

  private AssetStoreImpl underTest;

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

    recordStore = new OrientRecordStore(databaseManager, new HexRecordIdObfuscator());
    recordStore.start();

    // drop all test classes
    try (RecordStoreSession session = recordStore.openSession()) {
      RecordStoreSchema schema = session.getSchema();
      maybeDropType(schema, AssetStoreImpl.BLOB_REF_RECORD);
      maybeDropType(schema, AssetStoreImpl.ASSET_RECORD);
      maybeDropType(schema, AssetStoreImpl.COMPONENT_RECORD);
    }

    this.blobMetadataStore = MapdbBlobMetadataStore.create(blobMetadataPath.toFile());
    blobMetadataStore.start();

    blobStore = new FileBlobStore(blobContentPath, new VolumeChapterLocationStrategy(), new SimpleFileOperations(), blobMetadataStore);

    underTest = new AssetStoreImpl(recordStore, blobStore);
  }

  private static void maybeDropType(RecordStoreSchema schema, RecordType type) {
    if (schema.hasType(type.getName())) {
      schema.dropType(type.getName());
    }
  }

  @After
  public void tearDown() throws Exception {
    if (recordStore != null) {
      recordStore.stop();
      recordStore = null;
    }

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

  @Test(expected=IllegalStateException.class)
  public void createNoSuchComponent() {
    underTest.create(TEST_COMPONENT_ID, TEST_PATH, testStream(), null);
  }

  @Test(expected=IllegalStateException.class)
  public void createAssetExists() {
    createComponentRecord(TEST_COMPONENT_ID);
    underTest.create(TEST_COMPONENT_ID, TEST_PATH, testStream(), null);

    underTest.create(TEST_COMPONENT_ID, TEST_PATH, testStream(), null);
  }

  @Test
  public void createGetAndDelete() throws Exception {
    createComponentRecord(TEST_COMPONENT_ID);

    // initially should not exist
    assertThat(underTest.get(TEST_COMPONENT_ID, TEST_PATH), is(nullValue()));
    assertThat(underTest.getAll(TEST_COMPONENT_ID).isEmpty(), is(true));

    // create it
    underTest.create(TEST_COMPONENT_ID, TEST_PATH, testStream(), TEST_CONTENT_TYPE);

    // now it should exist, and fields should be set as expected
    Asset asset = underTest.get(TEST_COMPONENT_ID, TEST_PATH);
    assertThat(asset, is(notNullValue()));
    assert(asset != null); // we know it's not null; silence warnings
    assertThat(asset.getContentLength(), is((long) TEST_CONTENT.length()));
    assertThat(asset.getContentType(), is(TEST_CONTENT_TYPE));
    assertThat(asset.getFirstCreated(), is(notNullValue()));
    assertThat(asset.getFirstCreated(), is(asset.getLastModified()));
    assertThat(asset.getPath(), is(TEST_PATH));
    assertThat(IOUtils.toString(asset.openStream()), is(TEST_CONTENT));

    // it should also be the only entry in the getAll map now
    assertThat(underTest.getAll(TEST_COMPONENT_ID).size(), is(1));
    assertThat(underTest.getAll(TEST_COMPONENT_ID).get(TEST_PATH).getPath(), is(asset.getPath()));

    // deleting it should report true and leave the component empty
    assertThat(underTest.delete(TEST_COMPONENT_ID, TEST_PATH), is(true));
    assertThat(underTest.get(TEST_COMPONENT_ID, TEST_PATH), is(nullValue()));
    assertThat(underTest.getAll(TEST_COMPONENT_ID).isEmpty(), is(true));

    // deleting it again shouldn't cause problems, but should return false
    assertThat(underTest.delete(TEST_COMPONENT_ID, TEST_PATH), is(false));
  }

  private void createComponentRecord(ComponentId id) {
    try (RecordStoreSession session = recordStore.openSession()) {
      Record record = session.create(AssetStoreImpl.COMPONENT_RECORD);
      record.set(AssetStoreImpl.ID_FIELD, id.asUniqueString());
      record.save();
    }
  }

  private static InputStream testStream() {
    try {
      return new ByteArrayInputStream(TEST_CONTENT.getBytes("UTF-8"));
    } catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }
}
