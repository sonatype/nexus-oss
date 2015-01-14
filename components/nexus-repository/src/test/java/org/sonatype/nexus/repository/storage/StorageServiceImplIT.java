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

import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.orient.DatabaseInstanceRule;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Iterators;
import com.google.inject.util.Providers;
import com.tinkerpop.blueprints.Vertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonatype.nexus.repository.storage.StorageService.P_PATH;
import static org.sonatype.nexus.repository.storage.StorageService.V_ASSET;
import static org.sonatype.nexus.repository.storage.StorageService.V_BUCKET;
import static org.sonatype.nexus.repository.storage.StorageService.V_COMPONENT;

/**
 * Integration tests for {@link StorageServiceImpl}.
 */
public class StorageServiceImplIT
    extends TestSupport
{
  @Rule
  public DatabaseInstanceRule database = new DatabaseInstanceRule("test");

  protected StorageService underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new StorageServiceImpl(
        mock(EventBus.class),
        mock(BlobStoreManager.class),
        Providers.of(database.getInstance())
    );
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    underTest.stop();
  }

  @Test
  public void initialState() {
    try (GraphTx graph = underTest.getGraphTx()) {
      checkSize(underTest.browseVertices(graph, null), 0);
    }
  }

  @Test
  public void roundTripTest() {
    try (GraphTx graph = underTest.getGraphTx()) {
      // NOTE: A transaction is automatically started.

      // Make two buckets and verify state with browse and find
      Vertex bucket1 = underTest.createVertex(graph, V_BUCKET);
      Vertex bucket2 = underTest.createVertex(graph, V_BUCKET);

      checkSize(underTest.browseVertices(graph, null), 2);
      checkSize(underTest.browseVertices(graph, V_BUCKET), 2);
      checkSize(underTest.browseVertices(graph, V_ASSET), 0);
      checkSize(underTest.browseVertices(graph, V_COMPONENT), 0);

      assertNotNull(underTest.findVertex(graph, bucket1.getId(), null));
      assertNotNull(underTest.findVertex(graph, bucket2.getId(), null));
      assertNotNull(underTest.findVertex(graph, bucket1.getId(), V_BUCKET));
      assertNotNull(underTest.findVertex(graph, bucket2.getId(), V_BUCKET));
      assertNull(underTest.findVertex(graph, bucket1.getId(), V_ASSET));
      assertNull(underTest.findVertex(graph, bucket2.getId(), V_ASSET));

      // Create an asset and component, one in each bucket, and verify state with browse and find
      Vertex asset = underTest.createAssetOwnedBy(graph, bucket1);
      asset.setProperty(P_PATH, "path");
      Vertex component = underTest.createComponentOwnedBy(graph, bucket2);
      component.setProperty("foo", "bar");

      checkSize(underTest.browseVertices(graph, V_ASSET), 1);
      checkSize(underTest.browseVertices(graph, V_COMPONENT), 1);
      checkSize(underTest.browseVertices(graph, V_ASSET), 1);
      checkSize(underTest.browseAssetsOwnedBy(bucket1), 1);
      checkSize(underTest.browseAssetsOwnedBy(bucket2), 0);
      checkSize(underTest.browseComponentsOwnedBy(bucket1), 0);
      checkSize(underTest.browseComponentsOwnedBy(bucket2), 1);

      assertNotNull(underTest.findVertex(graph, asset.getId(), V_ASSET));
      assertNotNull(underTest.findVertex(graph, component.getId(), V_COMPONENT));

      assertNull(underTest.findVertexWithProperty(graph, P_PATH, "nomatch", null));
      assertNotNull(underTest.findVertexWithProperty(graph, P_PATH, "path", null));
      assertNotNull(underTest.findVertexWithProperty(graph, P_PATH, "path", V_ASSET));
      assertNotNull(underTest.findAssetWithPropertyOwnedBy(graph, P_PATH, "path", bucket1));
      assertNull(underTest.findAssetWithPropertyOwnedBy(graph, P_PATH, "nomatch", bucket1));
      assertNull(underTest.findAssetWithPropertyOwnedBy(graph, P_PATH, "path", bucket2));
      assertNull(underTest.findVertex(graph, bucket2.getId(), V_ASSET));

      assertNull(underTest.findVertexWithProperty(graph, "foo", "nomatch", null));
      assertNotNull(underTest.findVertexWithProperty(graph, "foo", "bar", null));
      assertNotNull(underTest.findVertexWithProperty(graph, "foo", "bar", V_COMPONENT));
      assertNotNull(underTest.findComponentWithPropertyOwnedBy(graph, "foo", "bar", bucket2));
      assertNull(underTest.findComponentWithPropertyOwnedBy(graph, "foo", "nomatch", bucket2));
      assertNull(underTest.findComponentWithPropertyOwnedBy(graph, "foo", "bar", bucket1));
      assertNull(underTest.findVertex(graph, bucket2.getId(), V_COMPONENT));

      // Delete both and make sure browse and find behave as expected
      underTest.deleteVertex(graph, asset);
      underTest.deleteVertex(graph, component);

      checkSize(underTest.browseVertices(graph, V_ASSET), 0);
      checkSize(underTest.browseVertices(graph, V_COMPONENT), 0);
      assertNull(underTest.findVertex(graph, asset.getId(), V_ASSET));
      assertNull(underTest.findVertex(graph, component.getId(), V_COMPONENT));

      // NOTE: It doesn't matter for this test, but you should commit when finished with one or more writes
      //       If you don't, your changes will be automatically rolled back.
      graph.commit();
    }
  }

  private void checkSize(Iterable iterable, int expectedSize) {
    assertThat(Iterators.size(iterable.iterator()), is(expectedSize));
  }
}
