/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.blobstore.file.internal;

import java.io.File;
import java.util.List;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.file.BlobMetadata;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.BlobState;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.common.collect.AutoClosableIterable;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

/**
 * Tests for {@link MapdbBlobMetadataStore}.
 */
public class MapdbBlobMetadataStoreTest
  extends TestSupport
{
  private BlobMetadataStore underTest;

  @Before
  public void setUp() throws Exception {
    File root = util.createTempDir("databases");
    File dir = new File(root, "test");
    this.underTest = MapdbBlobMetadataStore.create(dir);
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    if (underTest != null) {
      underTest.stop();
    }
  }

  /**
   * Helper to find states and close iterable.
   */
  private Iterable<BlobId> findWithState(final BlobState state) throws Exception {
    List<BlobId> results = Lists.newArrayList();
    try (AutoClosableIterable<BlobId> iter = underTest.findWithState(state)) {
      for (BlobId id : iter) {
        results.add(id);
      }
    }
    return results;
  }

  @Test
  public void stateTracking() throws Exception {
    BlobMetadata md = new BlobMetadata(BlobState.CREATING, ImmutableMap.of("foo", "bar"));
    BlobId id = underTest.add(md);
    log("Added: {} -> {}", id, md);

    // states should only contain CREATING
    assertThat(findWithState(BlobState.CREATING), contains(id));
    assertThat(findWithState(BlobState.ALIVE), emptyIterable());
    assertThat(findWithState(BlobState.MARKED_FOR_DELETION), emptyIterable());

    md.setBlobState(BlobState.ALIVE);
    underTest.update(id, md);
    log("Updated: {} -> {}", id, md);

    // states should only contain ALIVE
    assertThat(findWithState(BlobState.CREATING), emptyIterable());
    assertThat(findWithState(BlobState.ALIVE), contains(id));
    assertThat(findWithState(BlobState.MARKED_FOR_DELETION), emptyIterable());

    md.setBlobState(BlobState.MARKED_FOR_DELETION);
    underTest.update(id, md);
    log("Updated: {} -> {}", id, md);

    // states should only contain marked for MARKED_FOR_DELETION
    assertThat(findWithState(BlobState.CREATING), emptyIterable());
    assertThat(findWithState(BlobState.ALIVE), emptyIterable());
    assertThat(findWithState(BlobState.MARKED_FOR_DELETION), contains(id));

    underTest.delete(id);
    log("Deleted: {}", id);

    // states all be empty
    assertThat(findWithState(BlobState.CREATING), emptyIterable());
    assertThat(findWithState(BlobState.ALIVE), emptyIterable());
    assertThat(findWithState(BlobState.MARKED_FOR_DELETION), emptyIterable());
  }

  @Test
  public void basic() throws Exception {
    BlobMetadata md = new BlobMetadata(BlobState.CREATING, ImmutableMap.of("foo", "bar"));
    log(md);

    // add a record
    log("add");
    BlobId id = underTest.add(md);
    log(id);

    dumpStates();

    // update a record
    log("update");
    md.setBlobState(BlobState.ALIVE);
    underTest.update(id, md);

    dumpStates();

    // fetch a record
    log("fetch");
    BlobMetadata md2 = underTest.get(id);
    log(md2);

    // delete a record
    log("delete");
    underTest.delete(id);

    dumpStates();

    // compact
    log("compact");
    underTest.compact();
  }

  private void dumpStates() throws Exception {
    for (BlobState state : BlobState.values()) {
      log(state);
      for (BlobId foundId : findWithState(state)) {
        log("  {}", foundId);
      }
    }
  }
}
