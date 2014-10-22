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

import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapterRegistry;
import org.sonatype.nexus.component.services.internal.adapter.AssetEntityAdapter;
import org.sonatype.nexus.component.services.internal.adapter.ComponentEntityAdapterRegistryImpl;
import org.sonatype.nexus.internal.orient.DatabaseManagerImpl;
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import cucumber.api.java.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MetadataQueryServiceImplIT
    extends TestSupport
{
  private MinimalDatabaseServer databaseServer;

  private DatabaseManagerImpl databaseManager;

  private MapdbBlobMetadataStore blobMetadataStore;

  private MetadataQueryServiceImpl queryService;

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

    Provider<DatabaseInstance> databaseInstanceProvider = new Provider<DatabaseInstance>() {
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

    ComponentEntityAdapterRegistry adapterRegistry = new ComponentEntityAdapterRegistryImpl(databaseInstanceProvider);
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
    assertThat(queryService.componentClasses().size(), is(0));
    assertThat(countAllComponents(), is(0L));
    assertThat(queryService.countAssets(null), is(0L));
    assertThat(queryService.findAssets(null).size(), is(0));
  }

  private long countAllComponents() {
    long total = 0;
    for (Class<? extends Component> componentClass: queryService.componentClasses()) {
      total += queryService.countComponents(componentClass, null);
    }
    return total;
  }
}
