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

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.orient.DatabaseInstanceRule;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.inject.util.Providers;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for {@link StorageFacetImpl}.
 */
public class StorageFacetImplIT
    extends TestSupport {

  @Rule
  public DatabaseInstanceRule database = new DatabaseInstanceRule("test");

  protected BlobStore blobStore = mock(BlobStore.class);

  protected StorageFacet underTest;

  @Before
  public void setUp() throws Exception {
    blobStore = mock(BlobStore.class);
    underTest = new StorageFacetImpl(mock(EventBus.class), mock(BlobStore.class), Providers.of(database.getInstance()));
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    underTest.stop();
  }

  @Test
  public void initialState() {
    OrientGraph graph = underTest.getGraphTx();
    try {
      checkSize(underTest.browseAssets(graph), 0);
      checkSize(underTest.browseBuckets(graph), 0);
      checkSize(underTest.browseComponents(graph), 0);
      checkSize(underTest.browseLabels(graph), 0);
    }
    finally {
      graph.shutdown();
    }
  }

  @Test
  public void findNonExisting() {
    OrientGraph graph = underTest.getGraphTx();
    try {
      assertNull(underTest.findById(graph, "nonExistingId"));
      assertNull(underTest.findByPath(graph, "nonExistingPath"));
    }
    finally {
      graph.shutdown();
    }
  }

  @Test
  public void createFindBrowseDeleteAsset() {
    OrientGraph graph = underTest.getGraphTx();
    try {
      // NOTE: A transaction is automatically started.

      // CREATE
      Vertex createdVertex = underTest.createAsset(graph);
      createdVertex.setProperty(StorageFacet.P_PATH, "path");
      graph.commit();

      // FIND BY ID
      Vertex foundVertex = underTest.findById(graph, createdVertex.getId());
      assertThat(foundVertex.getId(), is(createdVertex.getId()));
      String storedPath = foundVertex.getProperty(StorageFacet.P_PATH);
      assertThat(storedPath, is("path"));

      // FIND BY PATH
      foundVertex = underTest.findByPath(graph, "path");
      assertThat(foundVertex.getId(), is(createdVertex.getId()));
      storedPath = foundVertex.getProperty(StorageFacet.P_PATH);
      assertThat(storedPath, is("path"));

      // BROWSE
      checkSize(underTest.browseAssets(graph), 1);

      // DELETE
      underTest.delete(graph, foundVertex);
      checkSize(underTest.browseAssets(graph), 0);
      assertNull(underTest.findById(graph, createdVertex.getId()));
      assertNull(underTest.findByPath(graph, "path"));

      // NOTE: You should commit when finished with one or more writes
      graph.commit();
    }
    catch (Throwable t) {
      // NOTE: If something bad happens, roll back the transaction
      graph.rollback();
      Throwables.propagate(t);
    }
    finally {
      // NOTE: This releases the transaction to the pool
      graph.shutdown();
    }
  }

  private void checkSize(Iterable iterable, int expectedSize) {
    assertThat(Iterators.size(iterable.iterator()), is(expectedSize));
  }
}
